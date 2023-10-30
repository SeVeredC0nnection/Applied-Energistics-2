/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.worldgen.meteorite.fallout;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import var;
import appeng.worldgen.meteorite.MeteoriteBlockPutter;

public class FalloutCopy extends Fallout {
    private static final float SPECIFIED_BLOCK_THRESHOLD = 0.9f;
    private static final float AIR_BLOCK_THRESHOLD = 0.8f;
    private static final float BLOCK_THRESHOLD_STEP = 0.1f;

    private final BlockState block;
    private final MeteoriteBlockPutter putter;

    public FalloutCopy(LevelAccessor level, BlockPos pos, MeteoriteBlockPutter putter,
            BlockState skyStone, RandomSource random) {
        super(putter, skyStone, random);
        this.putter = putter;
        var biome = level.getBiome(pos);
        if (biome.is(BiomeTags.IS_BADLANDS)) {
            block = Blocks.TERRACOTTA.defaultBlockState();
        } else if (biome.is(Tags.Biomes.IS_SNOWY)) {
            block = Blocks.SNOW_BLOCK.defaultBlockState();
        } else if (biome.is(BiomeTags.IS_BEACH) || biome.is(Tags.Biomes.IS_SANDY)) {
            block = Blocks.SAND.defaultBlockState();
        } else if (biome.is(Tags.Biomes.IS_PLAINS) || biome.is(BiomeTags.IS_FOREST)) {
            block = Blocks.DIRT.defaultBlockState();
        } else {
            block = Blocks.COBBLESTONE.defaultBlockState();
        }
    }

    @Override
    public void getRandomFall(LevelAccessor level, BlockPos pos) {
        var a = random.nextFloat();
        if (a > SPECIFIED_BLOCK_THRESHOLD) {
            this.putter.put(level, pos, this.block);
        } else {
            this.getOther(level, pos, a);
        }
    }

    public void getOther(LevelAccessor level, BlockPos pos, float a) {

    }

    @Override
    public void getRandomInset(LevelAccessor level, BlockPos pos) {
        var a = random.nextFloat();
        if (a > SPECIFIED_BLOCK_THRESHOLD) {
            this.putter.put(level, pos, this.block);
        } else if (a > AIR_BLOCK_THRESHOLD) {
            this.putter.put(level, pos, Blocks.AIR.defaultBlockState());
        } else {
            this.getOther(level, pos, a - BLOCK_THRESHOLD_STEP);
        }
    }
}
