package untamedwilds.world.gen.feature;

import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import untamedwilds.world.FaunaHandler;
import untamedwilds.world.FaunaSpawn;

import java.util.Random;
import java.util.function.Function;

public class FeatureOceanSessileSpawns extends Feature<NoFeatureConfig> {

    public FeatureOceanSessileSpawns(Function<Dynamic<?>, ? extends NoFeatureConfig> deserializer) {
        super(NoFeatureConfig::deserialize);
    }

    public boolean place(IWorld world, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos, NoFeatureConfig config) {
        if (rand.nextFloat() > 0.12) {
            Biome biome = world.getBiome(pos);
            BlockPos blockPos = new BlockPos(pos.getX(), world.getHeight(Heightmap.Type.OCEAN_FLOOR, pos.getX(), pos.getZ()), pos.getZ()); // So Sessile creatures do not spawn floating
            FaunaSpawn.performWorldGenSpawning(FaunaHandler.getSpawnableList(FaunaHandler.animalType.SESSILE), EntitySpawnPlacementRegistry.PlacementType.IN_WATER, world, biome, blockPos, rand);
            return true;
        }
        return false;
    }
}
