package untamedwilds.world;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.placement.CaveEdgeConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import untamedwilds.UntamedWilds;
import untamedwilds.compat.CompatBridge;
import untamedwilds.config.ConfigFeatureControl;
import untamedwilds.config.ConfigMobControl;
import untamedwilds.world.gen.feature.*;
import untamedwilds.world.gen.treedecorator.TreeOrchidDecorator;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = UntamedWilds.MOD_ID)
public class UntamedWildsGenerator {

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, UntamedWilds.MOD_ID);
    public static final Map<String, Float> biodiversity_levels = new java.util.HashMap<>();
    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATION = DeferredRegister.create(ForgeRegistries.TREE_DECORATOR_TYPES, UntamedWilds.MOD_ID);

    private static final RegistryObject<Feature<FeatureSpreadConfig>> SEA_ANEMONE = regFeature("sea_anemone", () -> new FeatureSeaAnemone(FeatureSpreadConfig.CODEC));
    private static final RegistryObject<Feature<NoFeatureConfig>> REEDS = regFeature("reeds", () -> new FeatureReedClusters(NoFeatureConfig.field_236558_a_));
    private static final RegistryObject<Feature<NoFeatureConfig>> ALGAE = regFeature("algae", () -> new FeatureUnderwaterAlgae(NoFeatureConfig.field_236558_a_));
    private static final RegistryObject<Feature<FeatureSpreadConfig>> VEGETATION = regFeature("vegetation", () -> new FeatureVegetation(FeatureSpreadConfig.CODEC));
    private static final RegistryObject<Feature<NoFeatureConfig>> FLOATING_VEGETATION = regFeature("floating_vegetation", () -> new FeatureFloatingPlants(NoFeatureConfig.field_236558_a_));

    // TODO: Unused because can't attach decorators to vanilla features. If I ever implement trees, this will go there
    public static final RegistryObject<TreeDecoratorType<?>> TREE_ORCHID = TREE_DECORATION.register("orchid", () -> new TreeDecoratorType<>(TreeOrchidDecorator.CODEC));

    private static final RegistryObject<Feature<NoFeatureConfig>> UNDERGROUND = regFeature("underground", () -> new FeatureUndergroundFaunaLarge(NoFeatureConfig.field_236558_a_));
    private static final RegistryObject<Feature<NoFeatureConfig>> APEX = regFeature("apex_predator", () -> new FeatureApexPredators(NoFeatureConfig.field_236558_a_));
    private static final RegistryObject<Feature<NoFeatureConfig>> CRITTERS = regFeature("critter", () -> new FeatureCritters(NoFeatureConfig.field_236558_a_));
    private static final RegistryObject<Feature<NoFeatureConfig>> SESSILE = regFeature("sessile", () -> new FeatureOceanSessileSpawns(NoFeatureConfig.field_236558_a_));

    private static final RegistryObject<Feature<NoFeatureConfig>> OCEAN = regFeature("ocean_rare", () -> new FeatureOceanSwimming(NoFeatureConfig.field_236558_a_));
    private static final RegistryObject<Feature<NoFeatureConfig>> DENSE_WATER = regFeature("dense_water", () -> new FeatureDenseWater(NoFeatureConfig.field_236558_a_));

    private static final RegistryObject<Feature<NoFeatureConfig>> CRITTER_BURROW = regFeature("burrow", () -> new FeatureCritterBurrow(NoFeatureConfig.field_236558_a_));

    private static <B extends Feature<?>> RegistryObject<B> regFeature(String name, Supplier<? extends B> supplier) {
        return FEATURES.register(name, supplier);
    }

    @SubscribeEvent
    public static void onBiomesLoad(BiomeLoadingEvent event) {
        // Thanks Mojang, very cool 😎
        // event.getSpawns().withSpawner()
        //Features.JUNGLE_TREE.getConfig().decorators.add(new TreeOrchidDecorator());
        if (event.getCategory() == Biome.Category.OCEAN) {
            if (!FaunaHandler.getSpawnableList(FaunaHandler.animalType.SESSILE).isEmpty())
                registerFeatureWithFreq(event, GenerationStage.Decoration.TOP_LAYER_MODIFICATION, SESSILE.get().withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG), ConfigFeatureControl.freqSessile.get());
            if (!FaunaHandler.getSpawnableList(FaunaHandler.animalType.LARGE_OCEAN).isEmpty())
                registerFeatureWithFreq(event, GenerationStage.Decoration.TOP_LAYER_MODIFICATION, OCEAN.get().withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG), ConfigFeatureControl.freqOcean.get());

            if (!event.getName().toString().equals("minecraft:frozen_ocean") && !event.getName().toString().equals("minecraft:deep_frozen_ocean")) {
                if (ConfigFeatureControl.addAnemones.get()) {
                    registerFeature(event, GenerationStage.Decoration.VEGETAL_DECORATION, SEA_ANEMONE.get().withConfiguration(new FeatureSpreadConfig(4)).withPlacement(Features.Placements.PATCH_PLACEMENT).chance(16), SEA_ANEMONE.get().getRegistryName());
                }
            }
        }

        if ((event.getCategory() == Biome.Category.JUNGLE))
            registerFeatureWithFreq(event, GenerationStage.Decoration.VEGETAL_DECORATION, FLOATING_VEGETATION.get().withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG).withPlacement(Features.Placements.KELP_PLACEMENT), 1, ConfigFeatureControl.addAlgae.get());
        if ((event.getCategory() == Biome.Category.RIVER || event.getCategory() == Biome.Category.JUNGLE || event.getCategory() == Biome.Category.SWAMP) && !ConfigFeatureControl.reedBlacklist.get().contains(event.getName().toString()))
            registerFeatureWithFreq(event, GenerationStage.Decoration.VEGETAL_DECORATION, REEDS.get().withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG).withPlacement(Features.Placements.KELP_PLACEMENT), ConfigFeatureControl.freqReeds.get(), ConfigFeatureControl.addReeds.get());
        if (!ConfigFeatureControl.algaeBlacklist.get().contains(event.getName().toString()))
            registerFeatureWithFreq(event, GenerationStage.Decoration.VEGETAL_DECORATION, ALGAE.get().withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG).withPlacement(Features.Placements.SEAGRASS_DISK_PLACEMENT), ConfigFeatureControl.freqAlgae.get(), ConfigFeatureControl.addAlgae.get());
        if (!ConfigFeatureControl.floraBlacklist.get().contains(event.getName().toString()))
            registerFeatureWithFreq(event, GenerationStage.Decoration.VEGETAL_DECORATION, VEGETATION.get().withConfiguration(new FeatureSpreadConfig(4)).withPlacement(Features.Placements.KELP_PLACEMENT), ConfigFeatureControl.freqFlora.get(), ConfigFeatureControl.addFlora.get());


        if (!FaunaHandler.getSpawnableList(FaunaHandler.animalType.DENSE_WATER).isEmpty())
            registerFeatureWithFreq(event, GenerationStage.Decoration.TOP_LAYER_MODIFICATION, DENSE_WATER.get().withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG), ConfigFeatureControl.freqWater.get());

        if (!FaunaHandler.getSpawnableList(FaunaHandler.animalType.CRITTER).isEmpty()) {
            if (ConfigFeatureControl.addBurrows.get()) {
                registerFeatureWithFreq(event, GenerationStage.Decoration.TOP_LAYER_MODIFICATION, CRITTER_BURROW.get().withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG), ConfigFeatureControl.freqCritter.get());
            } else {
                registerFeatureWithFreq(event, GenerationStage.Decoration.TOP_LAYER_MODIFICATION, CRITTERS.get().withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG), ConfigFeatureControl.freqCritter.get());
            }
        }

        if (!FaunaHandler.getSpawnableList(FaunaHandler.animalType.APEX_PRED).isEmpty())
            registerFeatureWithFreq(event, GenerationStage.Decoration.TOP_LAYER_MODIFICATION, APEX.get().withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG), ConfigFeatureControl.freqApex.get());

        if (ConfigFeatureControl.probUnderground.get() != 0 && /*!FaunaHandler.getSpawnableList(FaunaHandler.animalType.LARGE_UNDERGROUND).isEmpty() &&*/ ConfigMobControl.masterSpawner.get()) {
            float prob = ConfigFeatureControl.probUnderground.get().floatValue() / (CompatBridge.betterCaves || CompatBridge.cavesAndCliffs ? 3 : 1);
            event.getGeneration().withFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, UNDERGROUND.get().withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG).withPlacement(Placement.CARVING_MASK.configure(new CaveEdgeConfig(GenerationStage.Carving.AIR, prob)).chance(2)));
        }
    }

    private static void registerFeatureWithFreq(BiomeLoadingEvent event, GenerationStage.Decoration decoration, ConfiguredFeature<?, ?> feature, int freq) {
        registerFeatureWithFreq(event, decoration, feature, freq, ConfigMobControl.masterSpawner.get());
    }

    private static void registerFeatureWithFreq(BiomeLoadingEvent event, GenerationStage.Decoration decoration, ConfiguredFeature<?, ?> feature, int freq, boolean enable) {
        if (freq > 0 && enable) {
            registerFeature(event, decoration, feature.chance(freq), feature.feature.getRegistryName());
        }
    }

    private static void registerFeature(BiomeLoadingEvent event, GenerationStage.Decoration decoration, ConfiguredFeature<?, ?> feature, ResourceLocation name) {
        if (UntamedWilds.DEBUG) {
            UntamedWilds.LOGGER.info("Adding feature " + name + " to biome " + event.getName());
        }
        event.getGeneration().withFeature(decoration, feature);
    }

    public static void readBioDiversityLevels() {
        try (InputStream inputstream = LanguageMap.class.getResourceAsStream("/data/untamedwilds/tags/biodiversity_levels.json")) {
            JsonObject jsonobject = new Gson().fromJson(new InputStreamReader(inputstream, StandardCharsets.UTF_8), JsonObject.class);

            for(Map.Entry<String, JsonElement> entry : jsonobject.entrySet()) {
                biodiversity_levels.put(entry.getKey(), entry.getValue().getAsFloat());
            }
        } catch (JsonParseException | IOException ioexception) {
            UntamedWilds.LOGGER.error("Couldn't read data from /data/untamedwilds/tags/biodiversity_levels.json", ioexception);
        }
    }

    // Returns the biodiversity level of a biome. Values are data-driven, defaulting to 0.6 if no key is found.
    public static float getBioDiversityLevel(ResourceLocation biome) {
        String key = biome.toString();
        if (biodiversity_levels.containsKey(key)) {
            return biodiversity_levels.get(key);
        }
        return 0.6F;
    }
}