package com.herobrot.heroslevels.network.packet;

import com.herobrot.heroslevels.HerosLevels;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record RestrictionsSyncPacket(
        List<RestrictionRecord> block,
        List<RestrictionRecord> crafting,
        List<RestrictionRecord> entity,
        List<RestrictionRecord> item,
        List<RestrictionRecord> mining,
        List<EnchantmentRestrictionRecord> enchantments,
        List<RestrictionRecord> brewing
) implements CustomPacketPayload {

    public static final Type<RestrictionsSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(HerosLevels.MOD_ID, "restrictions_sync_packet"));

    public record SkillLevelRecord(int skillId, int level) {}

    public record RestrictionRecord(String targetId, List<SkillLevelRecord> skills) {}

    public record EnchantmentRestrictionRecord(String enchantmentId, int level, List<SkillLevelRecord> skills) {}

    public static final StreamCodec<FriendlyByteBuf, RestrictionsSyncPacket> STREAM_CODEC = StreamCodec.of(
            RestrictionsSyncPacket::write, RestrictionsSyncPacket::read
    );

    private static void write(FriendlyByteBuf buf, RestrictionsSyncPacket packet) {
        writeRecords(buf, packet.block);
        writeRecords(buf, packet.crafting);
        writeRecords(buf, packet.entity);
        writeRecords(buf, packet.item);
        writeRecords(buf, packet.mining);
        writeEnchantmentRecords(buf, packet.enchantments);
        writeRecords(buf, packet.brewing);
    }

    private static RestrictionsSyncPacket read(FriendlyByteBuf buf) {
        return new RestrictionsSyncPacket(
                readRecords(buf), readRecords(buf), readRecords(buf), readRecords(buf), readRecords(buf),
                readEnchantmentRecords(buf), readRecords(buf)
        );
    }

    private static void writeRecords(FriendlyByteBuf buf, List<RestrictionRecord> records) {
        buf.writeInt(records.size());
        for (RestrictionRecord record : records) {
            buf.writeUtf(record.targetId());
            writeSkills(buf, record.skills());
        }
    }

    private static List<RestrictionRecord> readRecords(FriendlyByteBuf buf) {
        List<RestrictionRecord> records = new ArrayList<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) records.add(new RestrictionRecord(buf.readUtf(), readSkills(buf)));
        return records;
    }

    private static void writeEnchantmentRecords(FriendlyByteBuf buf, List<EnchantmentRestrictionRecord> records) {
        buf.writeInt(records.size());
        for (EnchantmentRestrictionRecord record : records) {
            buf.writeUtf(record.enchantmentId());
            buf.writeInt(record.level());
            writeSkills(buf, record.skills());
        }
    }

    private static List<EnchantmentRestrictionRecord> readEnchantmentRecords(FriendlyByteBuf buf) {
        List<EnchantmentRestrictionRecord> records = new ArrayList<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++)
            records.add(new EnchantmentRestrictionRecord(buf.readUtf(), buf.readInt(), readSkills(buf)));
        return records;
    }

    private static void writeSkills(FriendlyByteBuf buf, List<SkillLevelRecord> skills) {
        buf.writeInt(skills.size());
        for (SkillLevelRecord skill : skills) {
            buf.writeInt(skill.skillId());
            buf.writeInt(skill.level());
        }
    }

    private static List<SkillLevelRecord> readSkills(FriendlyByteBuf buf) {
        List<SkillLevelRecord> skills = new ArrayList<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) skills.add(new SkillLevelRecord(buf.readInt(), buf.readInt()));
        return skills;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}