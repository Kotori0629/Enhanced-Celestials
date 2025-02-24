package dev.corgitaco.enhancedcelestials.entity;


import com.google.common.annotations.VisibleForTesting;
import dev.corgitaco.enhancedcelestials.core.ECEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

public class MeteorStrikeEntity extends Entity {
    private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(MeteorStrikeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> READY = SynchedEntityData.defineId(MeteorStrikeEntity.class, EntityDataSerializers.BOOLEAN);

    private int life;


    public MeteorStrikeEntity(EntityType<? extends MeteorStrikeEntity> $$0, Level level) {
        super($$0, level);
    }

    public MeteorStrikeEntity(Level level, Vec3 position, @Nullable Entity spawner) {
        super(ECEntities.METEOR_STRIKE.get(), level);
        setPos(position);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SIZE, 1F);
        builder.define(READY, true);
    }

    @Override
    public void tick() {
        super.tick();


        Vec3 position = position();

        if (!level().isClientSide) {
            if (isReady()) {
                if (level().random.nextFloat() < 0.01) {
                    float angle = this.level().random.nextFloat() * Mth.TWO_PI;

                    float addedDistance = Mth.nextFloat(this.random, 2F, getBbWidth() / 2F);
                    double addX = Mth.sin(angle) * addedDistance;
                    double addZ = Mth.cos(angle) * addedDistance;

                    Vec3 meteorSpawnPos = position.add(addX, Mth.nextInt(random, 450, 500), addZ);

                    MeteorEntity meteorEntity = new MeteorEntity(level());
                    meteorEntity.setPos(meteorSpawnPos);
                    meteorEntity.setSize(Mth.nextFloat(level().random, 0.3F, 3F));
                    meteorEntity.setDeltaMovement(0, -0.6, 0);
                    level().addFreshEntity(meteorEntity);
                }
                if (life > 400) {
                    discard();
                }
                life++;
            }
        }

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.life = compoundTag.getInt("life");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("life", life);
    }

    public boolean isReady() {
        return entityData.get(READY);
    }

    public void setReady(boolean ready) {
        entityData.set(READY, ready);
    }

    public float getSize() {
        return Math.max(entityData.get(SIZE), 1);
    }

    @VisibleForTesting
    public void setSize(float pSize) {
        float i = Mth.clamp(pSize, 1F, Byte.MAX_VALUE);
        this.entityData.set(SIZE, i);
        this.reapplyPosition();
        this.refreshDimensions();
    }

    public void refreshDimensions() {
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();
        super.refreshDimensions();
        this.setPos(d0, d1, d2);
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        if (SIZE.equals(pKey)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(pKey);
    }

    public EntityDimensions getDimensions(Pose pPose) {
        return super.getDimensions(pPose).scale(getSize() * 0.5F);
    }
}
