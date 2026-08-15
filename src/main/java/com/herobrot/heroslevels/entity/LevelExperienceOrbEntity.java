package com.herobrot.heroslevels.entity;

import com.herobrot.heroslevels.init.AttachmentInit;
import com.herobrot.heroslevels.init.EntityInit;
import com.herobrot.heroslevels.level.LevelManager;
import com.herobrot.heroslevels.util.PacketHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LevelExperienceOrbEntity extends Entity implements IEntityWithComplexSpawn {

    private static final int[] ORB_TIERS = {3, 7, 17, 37, 73, 149, 307, 617, 1237, 2477};

    private int orbAge;
    private int health = 5;
    private int amount;
    private int pickingCount = 1;
    private Player target;
    private Map<Integer, Integer> clumpedMap;

    public LevelExperienceOrbEntity(Level level, double x, double y, double z, int amount) {
        this(EntityInit.LEVEL_EXPERIENCE_ORB.get(), level);
        this.setPos(x, y, z);
        this.setYRot((float) (this.random.nextDouble() * 360.0));
        this.setDeltaMovement((this.random.nextDouble() * 0.2f - 0.1f) * 2.0, this.random.nextDouble() * 0.2 * 2.0, (this.random.nextDouble() * 0.2f - 0.1f) * 2.0);
        this.amount = amount;
    }

    public LevelExperienceOrbEntity(EntityType<? extends LevelExperienceOrbEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {}

    @Override
    @SuppressWarnings("resource")
    public void tick() {
        super.tick();
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        if (this.getEyeInFluidType() == NeoForgeMod.WATER_TYPE.value())
            this.applyWaterMovement();
        else if (!this.isNoGravity())
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.03, 0.0));
        if (this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA))
            this.setDeltaMovement((this.random.nextFloat() - this.random.nextFloat()) * 0.2f, 0.2f,
                    (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        if (!this.level().noCollision(this.getBoundingBox()))
            this.moveTowardsClosestSpace(this.getX(),
                    (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
        if (this.tickCount % 20 == 1) this.expensiveUpdate();
        if (this.target != null && (this.target.isSpectator() || !this.target.isAlive()))
            this.target = null;
        if (this.target != null) {
            Vec3 vec3 = new Vec3(this.target.getX() - this.getX(), this.target.getY() + (double) this.target.getEyeHeight() / 2.0 - this.getY(), this.target.getZ() - this.getZ());
            double d = vec3.lengthSqr();
            if (d < 64.0) {
                double e = 1.0 - Math.sqrt(d) / 8.0;
                this.setDeltaMovement(this.getDeltaMovement().add(vec3.normalize().scale(e * e * 0.1)));
            }
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        float friction = 0.98f;
        if (this.onGround())
            friction = this.level().getBlockState(this.blockPosition().below()).getBlock().getFriction() * 0.98f;
        this.setDeltaMovement(this.getDeltaMovement().multiply(friction, 0.98, friction));
        if (this.onGround())
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, -0.9, 1.0));
        ++this.orbAge;
        if (this.orbAge >= 6000) this.discard();
    }

    @SuppressWarnings("resource")
    private void expensiveUpdate() {
        if (this.target == null || this.target.distanceToSqr(this) > 64.0)
            this.target = this.level().getNearestPlayer(this, 8.0);
        if (this.level() instanceof ServerLevel serverLevel) {
            List<LevelExperienceOrbEntity> list = serverLevel.getEntitiesOfClass(LevelExperienceOrbEntity.class, this.getBoundingBox().inflate(0.5), this::isMergeable);
            for (LevelExperienceOrbEntity experienceOrbEntity : list)
                this.merge(experienceOrbEntity);
        }
    }

    public static void spawnCustomOrb(ServerLevel level, Vec3 pos, int amountToSpawn) {
        while (amountToSpawn > 0) {
            int currentOrbSize = LevelExperienceOrbEntity.roundToOrbSize(amountToSpawn);
            amountToSpawn -= currentOrbSize;
            if (LevelExperienceOrbEntity.wasMergedIntoExistingOrb(level, pos, currentOrbSize))
                continue;
            level.addFreshEntity(new LevelExperienceOrbEntity(level, pos.x(), pos.y(), pos.z(), currentOrbSize));
        }
    }

    private static boolean wasMergedIntoExistingOrb(ServerLevel level, Vec3 pos, int size) {
        AABB box = new AABB(pos.x() - 0.5, pos.y() - 0.5, pos.z() - 0.5,
                pos.x() + 0.5, pos.y() + 0.5, pos.z() + 0.5);
        List<LevelExperienceOrbEntity> list = level.getEntitiesOfClass
                (LevelExperienceOrbEntity.class, box, Entity::isAlive);
        if (!list.isEmpty()) {
            LevelExperienceOrbEntity experienceOrbEntity = list.getFirst();
            Map<Integer, Integer> previousMap = experienceOrbEntity.getClumpedMap();
            experienceOrbEntity.setClumpedMap(Stream.of(previousMap, Collections.singletonMap(size, 1))
                    .flatMap(map -> map.entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum)));
            experienceOrbEntity.pickingCount = experienceOrbEntity.getClumpedMap().values().stream()
                    .reduce(Integer::sum).orElse(1);
            return true;
        }
        return false;
    }

    private boolean isMergeable(LevelExperienceOrbEntity other) {
        return other.isAlive() && other != this;
    }

    private void merge(LevelExperienceOrbEntity other) {
        Map<Integer, Integer> otherMap = other.getClumpedMap();
        setClumpedMap(Stream.of(getClumpedMap(), otherMap).flatMap(map -> map.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum)));
        this.pickingCount = getClumpedMap().values().stream().reduce(Integer::sum).orElse(1);
        other.discard();
    }

    private void applyWaterMovement() {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x * 0.99f, Math.min(vec3.y + 5.0E-4f, 0.06f), vec3.z * 0.99f);
    }

    @Override
    @SuppressWarnings("resource")
    public boolean hurt(@NotNull DamageSource source, float amountReceived) {
        if (this.isInvulnerableTo(source) || this.level().isClientSide()) return false;
        this.markHurt();
        this.health = (int) ((float) this.health - amountReceived);
        if (this.health <= 0) this.discard();
        return true;
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(this.amount);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
        this.amount = additionalData.readInt();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putShort("Health", (short) this.health);
        tag.putShort("Age", (short) this.orbAge);
        tag.putInt("Value", this.amount);
        tag.putInt("Count", this.pickingCount);
        CompoundTag map = new CompoundTag();
        getClumpedMap().forEach((value, count) -> map.putInt(String.valueOf(value), count));
        tag.put("clumpedMap", map);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.health = tag.getShort("Health");
        this.orbAge = tag.getShort("Age");
        this.amount = tag.getInt("Value");
        this.pickingCount = Math.max(tag.getInt("Count"), 1);
        Map<Integer, Integer> map = new HashMap<>();
        if (tag.contains("clumpedMap")) {
            CompoundTag clumpedMap = tag.getCompound("clumpedMap");
            for (String s : clumpedMap.getAllKeys())
                map.put(Integer.parseInt(s), clumpedMap.getInt(s));
        }
        else map.put(this.amount, this.pickingCount);
        setClumpedMap(map);
    }

    @Override
    @SuppressWarnings("resource")
    public void playerTouch(@NotNull Player player) {
        if (!this.level().isClientSide() && player.takeXpDelay == 0 && this.orbAge > 20) {
            player.takeXpDelay = 2;
            player.take(this, 1);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.1F,
                    (this.random.nextFloat() - this.random.nextFloat()) * 0.35F + 0.9F);
            if (player instanceof ServerPlayer serverPlayer) {
                LevelManager levelManager = serverPlayer.getData(AttachmentInit.LEVEL_MANAGER);
                int totalXp = getClumpedMap().entrySet().stream()
                        .mapToInt(e -> e.getKey() * e.getValue()).sum();
                levelManager.addExperience(totalXp);
                serverPlayer.giveExperiencePoints(totalXp);
                PacketHelper.updateLevels(serverPlayer);
            }
            this.discard();
        }
    }

    public static int roundToOrbSize(int value) {
        for (int i = ORB_TIERS.length - 1; i >= 0; i--)
            if (value >= ORB_TIERS[i]) return ORB_TIERS[i];
        return 1;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    public int getOrbSize() {
        for (int i = ORB_TIERS.length - 1; i >= 0; i--) {
            if (this.amount >= ORB_TIERS[i]) return i + 1;
        }
        return 0;
    }

    // Método sin uso en el proyecto
    public int getExperienceAmount() {
        return this.amount;
    }

    @Override
    public @NotNull SoundSource getSoundSource() {
        return SoundSource.AMBIENT;
    }

    private Map<Integer, Integer> getClumpedMap() {
        if (this.clumpedMap == null) {
            this.clumpedMap = new HashMap<>();
            this.clumpedMap.put(this.amount, 1);
        }
        return this.clumpedMap;
    }

    private void setClumpedMap(Map<Integer, Integer> map) {
        this.clumpedMap = map;
        this.amount = getClumpedMap().entrySet().stream().mapToInt(entry -> entry.getKey() * entry.getValue()).sum();
    }
}