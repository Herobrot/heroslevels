package com.herobrot.heroslevels.level;

import net.minecraft.nbt.CompoundTag;

public class PlayerSkill {

    private final int id;
    private int level;

    public PlayerSkill(int id, int level) {
        this.id = id;
        this.level = level;
    }

    public PlayerSkill(CompoundTag tag) {
        this.id = tag.getInt("Id");
        this.level = tag.getInt("Level");
    }

    public CompoundTag writeDataToNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Id", this.id);
        tag.putInt("Level", this.level);
        return tag;
    }

    public int getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    // Sin uso en el proyecto
    public void increaseLevel(int level) {
        Skill skill = LevelManager.SKILLS.get(this.id);
        if (skill == null) return;

        if ((this.level + level) <= skill.maxLevel()) {
            this.level += level;
        } else {
            this.level = skill.maxLevel();
        }
    }

    // Sin uso en el proyecto
    public void decreaseLevel(int level) {
        if ((this.level - level) >= 0) {
            this.level -= level;
        } else {
            this.level = 0;
        }
    }
}