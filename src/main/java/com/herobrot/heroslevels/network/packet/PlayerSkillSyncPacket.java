package com.herobrot.heroslevels.network.packet;

import com.herobrot.heroslevels.HerosLevels;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record PlayerSkillSyncPacket(List<PlayerSkillRecord> skills) implements CustomPacketPayload {

    public static final Type<PlayerSkillSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, "player_skill_sync_packet"));

    public static final StreamCodec<FriendlyByteBuf, PlayerSkillSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.<FriendlyByteBuf, PlayerSkillRecord>list().apply(PlayerSkillRecord.STREAM_CODEC), PlayerSkillSyncPacket::skills,
            PlayerSkillSyncPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record PlayerSkillRecord(int id, int level) {
        public static final StreamCodec<FriendlyByteBuf, PlayerSkillRecord> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, PlayerSkillRecord::id,
                ByteBufCodecs.INT, PlayerSkillRecord::level,
                PlayerSkillRecord::new
        );
    }
}