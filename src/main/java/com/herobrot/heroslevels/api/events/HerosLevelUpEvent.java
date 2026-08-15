package com.herobrot.heroslevels.api.events;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

/**
 * Este evento se dispara en el servidor después de que un jugador sube de nivel general.
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