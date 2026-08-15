package com.herobrot.heroslevels.network.packet;

import com.herobrot.heroslevels.HerosLevels;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record LevelPacket(int overallLevel, int skillPoints, int totalLevelExperience, float levelProgress) implements CustomPacketPayload {

    public static final Type<LevelPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, "level_packet"));

    public static final StreamCodec<FriendlyByteBuf, LevelPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, LevelPacket::overallLevel,
            ByteBufCodecs.INT, LevelPacket::skillPoints,
            ByteBufCodecs.INT, LevelPacket::totalLevelExperience,
            ByteBufCodecs.FLOAT, LevelPacket::levelProgress,
            LevelPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}