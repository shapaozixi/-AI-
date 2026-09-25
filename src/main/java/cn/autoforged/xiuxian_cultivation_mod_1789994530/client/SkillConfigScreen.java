package cn.autoforged.xiuxian_cultivation_mod_1789994530.client;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.XiuxianCultivationMod;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.network.payload.ServerboundSkillAssignPayload;
import cn.autoforged.xiuxian_cultivation_mod_1789994530.skill.SkillType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Shift+L 打开的技能配置界面：把已习得的功法技能装配到 Z / X / C 三个技能槽。
 * 点击某一行的按钮会在「空 / 已习得的各个功法」之间循环。
 */
public class SkillConfigScreen extends Screen {
    private static final String[] SLOT_LABELS = {"Z", "X", "C"};
    private final Button[] slotButtons = new Button[3];

    public SkillConfigScreen() {
        super(Component.translatable("screen." + XiuxianCultivationMod.MODID + ".skill_config"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        for (int slot = 0; slot < 3; slot++) {
            final int slotIndex = slot;
            slotButtons[slot] = Button.builder(Component.empty(), button -> cycle(slotIndex))
                    .bounds(centerX - 110, 60 + slot * 28, 220, 20)
                    .build();
            this.addRenderableWidget(slotButtons[slot]);
        }
        this.addRenderableWidget(Button.builder(
                        Component.translatable("screen." + XiuxianCultivationMod.MODID + ".skill_config.done"),
                        button -> this.onClose())
                .bounds(centerX - 50, 60 + 3 * 28 + 10, 100, 20)
                .build());
        updateLabels();
    }

    private void cycle(int slot) {
        List<Integer> options = buildOptions();
        int current = ClientCultivationData.getSkillSlot(slot);
        int index = options.indexOf(current);
        int next = options.get((index + 1) % options.size());
        ClientCultivationData.setSkillSlot(slot, next);
        PacketDistributor.sendToServer(new ServerboundSkillAssignPayload(slot, next));
        updateLabels();
    }

    /** 可选项：0（空） + 所有已习得的功法技能。 */
    private static List<Integer> buildOptions() {
        List<Integer> options = new ArrayList<>();
        options.add(0);
        int learned = ClientCultivationData.getLearnedSkills();
        for (SkillType skill : SkillType.values()) {
            if ((learned & skill.bit()) != 0) {
                options.add(skill.id());
            }
        }
        return options;
    }

    private void updateLabels() {
        for (int slot = 0; slot < 3; slot++) {
            int id = ClientCultivationData.getSkillSlot(slot);
            SkillType skill = SkillType.byId(id);
            Component name = skill == null
                    ? Component.translatable("screen." + XiuxianCultivationMod.MODID + ".skill_config.empty")
                    : skill.displayName();
            slotButtons[slot].setMessage(Component.literal(SLOT_LABELS[slot] + "  ").append(name));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 25, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font,
                Component.translatable("screen." + XiuxianCultivationMod.MODID + ".skill_config.hint"),
                this.width / 2, 42, 0xA0A0A0);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
