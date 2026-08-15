package com.herobrot.heroslevels.compat.jade;

import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.util.TooltipUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.ArrayList;
import java.util.List;

public class EntityComponentProvider implements IEntityComponentProvider {

    @Override
    public ResourceLocation getUid() {
        return TooltipUtil.JADE_ENTITY_UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        Player player = accessor.getPlayer();
        if (player == null || player.isCreative() || player.isSpectator()) return;
        LevelManager levelManager = player.getData(AttachmentInit.LEVEL_MANAGER);
        List<Component> lines = new ArrayList<>();
        TooltipUtil.appendEntityTooltipForJade(lines, levelManager, player.isCreative(), accessor.getEntity());
        if (!lines.isEmpty()) for (Component line : lines) tooltip.add(line);
    }
}