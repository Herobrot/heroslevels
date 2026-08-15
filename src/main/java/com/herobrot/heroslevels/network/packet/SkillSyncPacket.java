package com.herobrot.heroslevels.network.packet;

import com.herobrot.heroslevels.HerosLevels;
import com.herobrot.heroslevels.level.SkillAttribute;
import com.herobrot.heroslevels.level.SkillBonus;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record SkillSyncPacket(List<Integer> skillIds, List<String> skillKeys, List<Integer> skillMaxLevels,
                              List<SkillAttributesRecord> skillAttributes,
                              SkillBonusesRecord skillBonuses) implements CustomPacketPayload {

    public static final Type<SkillSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, "skill_sync_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT.apply(ByteBufCodecs.list()), SkillSyncPacket::skillIds,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), SkillSyncPacket::skillKeys,
            ByteBufCodecs.INT.apply(ByteBufCodecs.list()), SkillSyncPacket::skillMaxLevels,
            SkillAttributesRecord.STREAM_CODEC.apply(ByteBufCodecs.list()), SkillSyncPacket::skillAttributes,
            SkillBonusesRecord.STREAM_CODEC, SkillSyncPacket::skillBonuses,
            SkillSyncPacket::new
    );

    public record SkillAttributesRecord(List<SkillAttribute> skillAttributes) {

        public static final StreamCodec<RegistryFriendlyByteBuf, SkillAttributesRecord> STREAM_CODEC = StreamCodec.of(
                (buf, record) -> {
                    buf.writeInt(record.skillAttributes().size());
                    for (SkillAttribute attr : record.skillAttributes()) {
                        buf.writeInt(attr.id());
                        buf.writeInt(attr.displayGroupId());
                        buf.writeUtf(attr.attribute().unwrapKey().get().location().toString());
                        buf.writeFloat(attr.baseValue());
                        buf.writeFloat(attr.levelValue());
                        buf.writeEnum(attr.operation());
                    }
                },
                buf -> {
                    List<SkillAttribute> attrs = new ArrayList<>();
                    int size = buf.readInt();
                    for (int i = 0; i < size; i++) {
                        int id = buf.readInt();
                        int displayGroupId = buf.readInt();
                        ResourceLocation attrId = ResourceLocation.parse(buf.readUtf());
                        Holder<Attribute> attribute = BuiltInRegistries.ATTRIBUTE
                                .getHolder(ResourceKey.create(Registries.ATTRIBUTE, attrId))
                                .orElseThrow(() -> new IllegalStateException("Atributo perdido en la red: " + attrId));
                        float baseValue = buf.readFloat();
                        float levelValue = buf.readFloat();
                        AttributeModifier.Operation operation = buf.readEnum(AttributeModifier.Operation.class);
                        attrs.add(new SkillAttribute(id, displayGroupId, attribute, baseValue, levelValue, operation));
                    }
                    return new SkillAttributesRecord(attrs);
                }
        );
    }

    public record SkillBonusesRecord(List<SkillBonus> skillBonuses) {

        public static final StreamCodec<RegistryFriendlyByteBuf, SkillBonusesRecord> STREAM_CODEC = StreamCodec.of(
                (buf, record) -> {
                    buf.writeInt(record.skillBonuses().size());
                    for (SkillBonus bonus : record.skillBonuses()) {
                        buf.writeUtf(bonus.key());
                        buf.writeInt(bonus.id());
                        buf.writeInt(bonus.level());
                    }
                },
                buf -> {
                    List<SkillBonus> bonuses = new ArrayList<>();
                    int size = buf.readInt();
                    for (int i = 0; i < size; i++) {
                        String key = buf.readUtf();
                        int id = buf.readInt();
                        int level = buf.readInt();
                        bonuses.add(new SkillBonus(key, id, level));
                    }
                    return new SkillBonusesRecord(bonuses);
                }
        );
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}