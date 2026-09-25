package cn.autoforged.xiuxian_cultivation_mod_1789994530.client;

/** 纯客户端缓存：由服务端同步包更新，供 HUD 渲染读取。不引用任何客户端专属类。 */
public final class ClientCultivationData {
    private static int mortalTier = 0;
    private static int mortalExp = 0;
    private static int bodyTier = 0;
    private static int bodyExp = 0;
    private static int qi = 0;
    private static boolean gatePassed = false;
    private static int bonusMaxQi = 0;
    private static boolean attackEnhanceActive = false;
    private static int attackEnhancePercent = 1;
    private static int learnedSkills = 0b1110;
    private static final int[] skillSlots = {2, 3, 1};

    private ClientCultivationData() {
    }

    public static void update(int mortalTier, int mortalExp, int bodyTier, int bodyExp, int qi,
                              boolean gatePassed, int bonusMaxQi, int learnedSkills,
                              int skillSlot0, int skillSlot1, int skillSlot2) {
        ClientCultivationData.mortalTier = mortalTier;
        ClientCultivationData.mortalExp = mortalExp;
        ClientCultivationData.bodyTier = bodyTier;
        ClientCultivationData.bodyExp = bodyExp;
        ClientCultivationData.qi = qi;
        ClientCultivationData.gatePassed = gatePassed;
        ClientCultivationData.bonusMaxQi = bonusMaxQi;
        ClientCultivationData.learnedSkills = learnedSkills;
        skillSlots[0] = skillSlot0;
        skillSlots[1] = skillSlot1;
        skillSlots[2] = skillSlot2;
    }

    /** percentBasisPoints 为万分比：100 = 1%。 */
    public static void updateEnhance(boolean active, int percentBasisPoints) {
        ClientCultivationData.attackEnhanceActive = active;
        ClientCultivationData.attackEnhancePercent = Math.max(1, (int) Math.round(percentBasisPoints / 100.0D));
    }

    public static void reset() {
        update(0, 0, 0, 0, 0, false, 0, 0b1110, 2, 3, 1);
        updateEnhance(false, 100);
    }

    public static int getMortalTier() {
        return mortalTier;
    }

    public static int getMortalExp() {
        return mortalExp;
    }

    public static int getBodyTier() {
        return bodyTier;
    }

    public static int getBodyExp() {
        return bodyExp;
    }

    public static int getQi() {
        return qi;
    }

    public static boolean isGatePassed() {
        return gatePassed;
    }

    public static int getBonusMaxQi() {
        return bonusMaxQi;
    }

    public static boolean isAttackEnhanceActive() {
        return attackEnhanceActive;
    }

    /** 返回百分比整数，例如 1 表示 1%。 */
    public static int getAttackEnhancePercent() {
        return attackEnhancePercent;
    }

    public static int getLearnedSkills() {
        return learnedSkills;
    }

    /** 技能槽 0/1/2 对应的技能 id（0 表示未装配）。 */
    public static int getSkillSlot(int index) {
        if (index < 0 || index >= skillSlots.length) {
            return 0;
        }
        return skillSlots[index];
    }

    /** 客户端本地更新技能槽（服务端同步回来前先乐观更新界面）。 */
    public static void setSkillSlot(int index, int skillId) {
        if (index >= 0 && index < skillSlots.length) {
            skillSlots[index] = skillId;
        }
    }
}
