package dev.corgitaco.enhancedcelestials.block;

import dev.corgitaco.enhancedcelestials.core.EnhancedCelestialsBlockTags;
import dev.corgitaco.enhancedcelestials.entity.SpaceMossBugEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface SpaceMossGrowthBlock {

    default boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return levelReader.getBlockState(blockPos.below()).is(EnhancedCelestialsBlockTags.SPACE_MOSS_GROWS_ON);
    }

    default void entityInside(Entity entity, Level level) {
        var randomSource = level.random;

        if (entity instanceof SpaceMossBugEntity spaceMossBug && spaceMossBug.getSporeDelay() == 0 && !spaceMossBug.isCoveredInSpores() && randomSource.nextInt(48) == 0) {
            spaceMossBug.setCoveredInSpores(true);
        }
    }
}
