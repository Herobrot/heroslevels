package com.herobrot.heroslevels.api.events;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

/**
 * This event triggers on the server just before a player levels up.
 * Canceling it prevents the level (and associated skill points) from being applied: progress
 * is held just below the threshold, and the event would trigger again with the next
 * gain in experience.
 */
public class HerosLevelUpEvent extends LivingEvent implements ICancellableEvent {

    private final int oldLevel;
    private final int newLevel;

    public HerosLevelUpEvent(Player player, int oldLevel, int newLevel) {
        super(player);
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    public Player getPlayer() { return (Player) super.getEntity(); }

    public int getOldLevel() { return oldLevel; }

    public int getNewLevel() { return newLevel; }
}