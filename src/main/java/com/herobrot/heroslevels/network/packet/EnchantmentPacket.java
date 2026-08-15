package com.herobrot.heroslevels.network.packet;

import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.registry.EnchantmentRegistry.EnchantmentKey;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record EnchantmentPacket(Map<EnchantmentKey, Integer> indexed, List<Integer> keys, List<String> ids, List<Integer> levels) implements CustomPacketPayload {

    public static final Type<EnchantmentPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, "heros_enchantment_packet"));

    private static final StreamCodec<RegistryFriendlyByteBuf, EnchantmentKey> ENCHANTMENT_KEY_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, EnchantmentKey::id,
            ByteBufCodecs.INT, EnchantmentKey::level,
            EnchantmentKey::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantmentPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ENCHANTMENT_KEY_CODEC, ByteBufCodecs.INT), EnchantmentPacket::indexed,
            ByteBufCodecs.INT.apply(ByteBufCodecs.list()), EnchantmentPacket::keys,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), EnchantmentPacket::ids,
            ByteBufCodecs.INT.apply(ByteBufCodecs.list()), EnchantmentPacket::levels,
            EnchantmentPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}