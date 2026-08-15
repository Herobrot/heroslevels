package com.herobrot.heroslevels.network.packet;

import com.herobrot.heroslevels.HerosLevels;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record StatPacket(int id, int level) implements CustomPacketPayload {

    public static final Type<StatPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, "stat_packet"));

    public static final StreamCodec<FriendlyByteBuf, StatPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, StatPacket::id,
            ByteBufCodecs.INT, StatPacket::level,
            StatPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}