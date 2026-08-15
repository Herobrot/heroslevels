package com.herobrot.heroslevels.network.packet;

import com.herobrot.heroslevels.HerosLevels;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record AttributeSyncPacket() implements CustomPacketPayload {

    public static final Type<AttributeSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, "attribute_sync_packet"));

    public static final StreamCodec<FriendlyByteBuf, AttributeSyncPacket> STREAM_CODEC = StreamCodec.unit(new AttributeSyncPacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}