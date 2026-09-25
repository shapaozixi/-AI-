package cn.autoforged.xiuxian_cultivation_mod_1789994530.client.hud;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.client.ClientCultivationData;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.cultivation.CultivationHelper;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.skill.SkillType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 在饥饿/饱和度条上方绘制真气条与修炼进度条。
 */
public class CultivationHudOverlay implements LayeredDraw.Layer {
    public static final ResourceLocation HUD_ID =
            ResourceLocation.fromNamespaceAndPath(XiuxianCultivationMod.MODID, "cultivation_hud");
    public static final CultivationHudOverlay INSTANCE = new CultivationHudOverlay();

    private static final int BAR_WIDTH = 81;
    private static final int BAR_HEIGHT = 5;
    private static final int QI_COLOR = 0xFF33E5FF;
    private static final int QI_BACK = 0x99003040;
    private static final int EXP_COLOR = 0xFFB060FF;
    private static final int EXP_BACK = 0x99301050;

    private CultivationHudOverlay() {
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.options.hideGui) {
            return;
        }

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        int x = screenWidth / 2 + 10;
        int qiY = screenHeight - 51;
        int expY = screenHeight - 60;

        int mortalTier = ClientCultivationData.getMortalTier();
        int bodyTier = ClientCultivationData.getBodyTier();
        int qi = ClientCultivationData.getQi();
        boolean gatePassed = ClientCultivationData.isGatePassed();
        // 最大真气 = 最大生命×10 + 护甲×50 + 击杀敌对生物累积加成（与服务端 getMaxQi 保持一致）
        int maxQi = (int) Math.round(player.getMaxHealth() * 10.0D
                + player.getArmorValue() * 50.0D + ClientCultivationData.getBonusMaxQi());

        // 真气条
        float qiRatio = maxQi > 0 ? (float) qi / (float) maxQi : 0.0F;
        renderBar(guiGraphics, x, qiY, qiRatio, QI_COLOR, QI_BACK);

        // 修炼进度条
        float expRatio;
        int currentExp;
        int neededExp;
        if (!gatePassed && mortalTier < 3) {
            currentExp = ClientCultivationData.getMortalExp();
            neededExp = CultivationHelper.MORTAL_EXP_REQUIRED[mortalTier + 1];
            expRatio = neededExp > 0 ? (float) currentExp / (float) neededExp : 0.0F;
        } else if (gatePassed && bodyTier < 3) {
            currentExp = ClientCultivationData.getBodyExp();
            neededExp = CultivationHelper.BODY_EXP_REQUIRED[bodyTier + 1];
            expRatio = neededExp > 0 ? (float) currentExp / (float) neededExp : 0.0F;
        } else {
            currentExp = 0;
            neededExp = 0;
            expRatio = gatePassed ? 1.0F : 0.0F;
        }
        renderBar(guiGraphics, x, expY, expRatio, EXP_COLOR, EXP_BACK);

        // 境界文字
        Component realmText;
        if (!gatePassed) {
            if (mortalTier <= 0) {
                realmText = Component.translatable(CultivationHelper.key("realm.mortal"));
            } else if (mortalTier < 3) {
                realmText = Component.translatable(CultivationHelper.key("hud.realm_tier"),
                        Component.translatable(CultivationHelper.key("realm.mortal")), mortalTier);
            } else {
                realmText = Component.translatable(CultivationHelper.key("hud.trial"),
                        Component.translatable(CultivationHelper.key("realm.mortal")));
            }
        } else if (bodyTier <= 0) {
            realmText = Component.translatable(CultivationHelper.key("realm.body"));
        } else {
            realmText = Component.translatable(CultivationHelper.key("hud.realm_tier"),
                    Component.translatable(CultivationHelper.key("realm.body")), bodyTier);
        }
        guiGraphics.drawString(minecraft.font, realmText, x, expY - 11, 0xFFD700, true);

        // 真气数值
        Component qiText = Component.translatable(CultivationHelper.key("hud.qi"), qi, maxQi);
        guiGraphics.drawString(minecraft.font, qiText, x, qiY + 7, 0x66E5FF, true);

        // 攻击增强模式状态
        if (ClientCultivationData.isAttackEnhanceActive()) {
            Component enhanceText = Component.translatable(CultivationHelper.key("hud.enhance"),
                    ClientCultivationData.getAttackEnhancePercent());
            guiGraphics.drawString(minecraft.font, enhanceText, x, expY - 33, 0xFFFF66, true);
        }

        // 功法技能装配（Z / X / C）
        Component skillText = Component.translatable(CultivationHelper.key("hud.skills"),
                skillName(0), skillName(1), skillName(2));
        guiGraphics.drawString(minecraft.font, skillText, x, qiY + 17, 0x66E5FF, true);

        // 经验数值
        if (neededExp > 0) {
            Component expText = Component.translatable(CultivationHelper.key("hud.exp"), currentExp, neededExp)
                    .copy().withStyle(ChatFormatting.LIGHT_PURPLE);
            guiGraphics.drawString(minecraft.font, expText, x, expY - 22, 0xB060FF, true);
        }
    }

    private static Component skillName(int slot) {
        SkillType skill = SkillType.byId(ClientCultivationData.getSkillSlot(slot));
        if (skill == null) {
            return Component.literal("-");
        }
        return skill.displayName();
    }

    private static void renderBar(GuiGraphics guiGraphics, int x, int y, float ratio, int fillColor, int backColor) {
        guiGraphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, backColor);
        float clamped = Math.max(0.0F, Math.min(1.0F, ratio));
        int fillWidth = (int) (BAR_WIDTH * clamped);
        if (fillWidth > 0) {
            guiGraphics.fill(x, y, x + fillWidth, y + BAR_HEIGHT, fillColor);
        }
        guiGraphics.renderOutline(x - 1, y - 1, BAR_WIDTH + 2, BAR_HEIGHT + 2, 0xFF101010);
    }
}
