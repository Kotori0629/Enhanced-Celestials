package dev.corgitaco.enhancedcelestials.core;

import dev.corgitaco.enhancedcelestials.EnhancedCelestials;
import dev.corgitaco.enhancedcelestials.entity.MeteorEntity;
import dev.corgitaco.enhancedcelestials.entity.MeteorStrikeEntity;
import dev.corgitaco.enhancedcelestials.entity.SpaceMossBugEntity;
import dev.corgitaco.enhancedcelestials.platform.services.RegistrationService;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Formatter;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class ECEntities {
    private static final StringBuilder BUILDER = new StringBuilder();

    private static final Formatter FORMATTER = new Formatter(BUILDER);

    public static final Supplier<EntityType<MeteorEntity>> METEOR = createEntity("meteor", EntityType.Builder.<MeteorEntity>of(MeteorEntity::new, MobCategory.MISC).sized(3, 3).clientTrackingRange(512).updateInterval(Integer.MAX_VALUE));
    public static final Supplier<EntityType<SpaceMossBugEntity>> SPACE_MOSS_BUG = createEntity("space_moss_bug", EntityType.Builder.of(SpaceMossBugEntity::new, MobCategory.AMBIENT).sized(1.275F, 0.5625F));
    public static final Supplier<EntityType<MeteorStrikeEntity>> METEOR_STRIKE = createEntity("meteor_strike", EntityType.Builder.<MeteorStrikeEntity>of(MeteorStrikeEntity::new, MobCategory.MISC).sized(5, 5));

    private ECEntities() {
    }

    public static void loadClass() {
    }

    public static void registerAttributes(BiConsumer<EntityType<? extends LivingEntity>, AttributeSupplier.Builder> consumer) {
        consumer.accept(SPACE_MOSS_BUG.get(), Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.2));
    }

    private static <T extends Entity> Supplier<EntityType<T>> createEntity(String name, EntityType.Builder<T> builder) {
        BUILDER.setLength(0);

        Supplier entityTypeSupplier = () -> builder.build(FORMATTER.format("%s:%s", EnhancedCelestials.MOD_ID, name).toString());
        return RegistrationService.INSTANCE.register(BuiltInRegistries.ENTITY_TYPE, name, entityTypeSupplier);
    }
}
