package com.herobrot.heroslevels.init;

import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslib.attachment.AttachmentRegistryHelper;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class AttachmentInit {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = AttachmentRegistryHelper.createRegister(HerosLevels.MOD_ID);

    public static final Supplier<AttachmentType<LevelManager>> LEVEL_MANAGER = AttachmentRegistryHelper.registerNBTAttachment(
            ATTACHMENTS,
            "level_manager",
            Player.class,
            LevelManager::new,
            true
    );
}