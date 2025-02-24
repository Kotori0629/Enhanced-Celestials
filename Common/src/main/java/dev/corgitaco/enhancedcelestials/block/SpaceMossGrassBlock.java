package dev.corgitaco.enhancedcelestials.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class SpaceMossGrassBlock extends BushBlock implements SpaceMossGrowthBlock {
    private static final VoxelShape SHAPE = box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D);

    public static final MapCodec<SpaceMossGrassBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    propertiesCodec()
            ).apply(instance, SpaceMossGrassBlock::new)
    );


    public SpaceMossGrassBlock(Properties $$0) {
        super($$0);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    public void entityInside(BlockState $$0, Level $$1, BlockPos $$2, Entity $$3) {
        SpaceMossGrowthBlock.super.entityInside($$3, $$1);
    }

    @Override
    public boolean canSurvive(BlockState $$0, LevelReader $$1, BlockPos $$2) {
        return SpaceMossGrowthBlock.super.canSurvive($$0, $$1, $$2);
    }

    @Override
    public VoxelShape getShape(BlockState $$0, BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
        var offset = $$0.getOffset($$1, $$2);
        return SHAPE.move(offset.x, offset.y, offset.z);
    }
}
