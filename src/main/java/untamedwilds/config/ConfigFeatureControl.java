package untamedwilds.config;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ConfigFeatureControl {
    public static ForgeConfigSpec.BooleanValue addAnemones;
    public static ForgeConfigSpec.BooleanValue addReeds;
    public static ForgeConfigSpec.BooleanValue addFlora;
    public static ForgeConfigSpec.BooleanValue addAlgae;
    public static ForgeConfigSpec.BooleanValue addTreeOrchids;
    public static ForgeConfigSpec.BooleanValue addBurrows;

    public static ForgeConfigSpec.ConfigValue<List<? extends String>> reedBlacklist;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> floraBlacklist;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> algaeBlacklist;

    public static ForgeConfigSpec.IntValue freqReeds;
    public static ForgeConfigSpec.IntValue freqFlora;
    public static ForgeConfigSpec.IntValue freqAlgae;

    public static ForgeConfigSpec.IntValue freqCritter;
    public static ForgeConfigSpec.IntValue freqWater;
    public static ForgeConfigSpec.IntValue freqSessile;
    public static ForgeConfigSpec.IntValue freqOcean;
    public static ForgeConfigSpec.IntValue freqApex;
    public static ForgeConfigSpec.IntValue freqHerbivores;
    public static ForgeConfigSpec.DoubleValue probUnderground;

    public static ForgeConfigSpec.ConfigValue<List<? extends String>> dimensionFeatureBlacklist;
    public static HashMap<String, ForgeConfigSpec.BooleanValue> options = new HashMap<>();

    private final ForgeConfigSpec.Builder builder;

    ConfigFeatureControl(final ForgeConfigSpec.Builder builder) {
        //builder.push("feature_control");
        this.builder = builder;
        builder.comment("Options pertaining to blocks and their worldgen");
        addAnemones = define("gencontrol.anemone", true, "Controls whether to add Anemones and their associated items to oceans.");
        addReeds = define("gencontrol.reeds", true, "Controls whether to spawn Reeds in River/Swamp biomes");
        addFlora = define("gencontrol.bush", true, "Controls whether to spawn random Flora in the world");
        addTreeOrchids = define("gencontrol.tree_orchid", true, "Controls whether to add Tree Orchids (NOT YET IMPLEMENTED)");
        addAlgae = define("gencontrol.algae", true, "Controls whether to spawn Amazon Sword in Swamp/Jungle biomes");
        addBurrows = define("gencontrol.burrows", true, "Controls whether to use Burrows to spawn Critters, instead of having them clog up the Spawns");

        reedBlacklist = builder.comment("Prevent spawns of Reeds in these biomes").defineList("gencontrol.reed_blacklist", Lists.newArrayList(), string -> string instanceof String);
        freqReeds = builder.comment("Frequency of Reeds, 1 in N chunks will generate Reeds (0 to disable)").defineInRange("gencontrol.freqreeds", 4, 0, Integer.MAX_VALUE);
        floraBlacklist = builder.comment("Prevent spawns of Flora in these biomes").defineList("gencontrol.flora_blacklist", Lists.newArrayList(), string -> string instanceof String);
        freqFlora = builder.comment("Frequency of Flora, 1 in N chunks will generate random Flora (0 to disable)").defineInRange("gencontrol.freqflora", 4, 0, Integer.MAX_VALUE);
        algaeBlacklist = builder.comment("Prevent spawns of Algae in these biomes").defineList("gencontrol.algae_blacklist", Arrays.asList("minecraft:frozen_ocean", "minecraft:deep_frozen_ocean"), string -> string instanceof String);
        freqAlgae = builder.comment("Frequency of Algae, abstract value (0 to disable)").defineInRange("gencontrol.freqalgae", 1, 0, Integer.MAX_VALUE);

        freqCritter = builder.comment("Frequency of Critters, 1 in N chunks will generate with Critters (0 to disable)").defineInRange("gencontrol.freqcritter", 6, 0, Integer.MAX_VALUE);
        freqSessile = builder.comment("Frequency of Sessile Ocean Mobs, 1 in N chunks will generate with Sessile Mobs (0 to disable)").defineInRange("gencontrol.freqsessile", 8, 0, Integer.MAX_VALUE);
        freqOcean = builder.comment("Frequency of Ocean Mobs, 1 in N chunks will generate with Ocean Mobs (0 to disable)").defineInRange("gencontrol.freqocean", 16, 0, Integer.MAX_VALUE);
        freqApex = builder.comment("Frequency of Apex Predators, 1 in N chunks will generate with an Apex Predator (0 to disable)").defineInRange("gencontrol.freqapex", 64, 0, Integer.MAX_VALUE);
        freqHerbivores = builder.comment("Frequency of Herbivores, 1 in N chunks will generate with an Apex Predator (0 to disable)").defineInRange("gencontrol.freqherbivore", 48, 0, Integer.MAX_VALUE);
        freqWater = builder.comment("Frequency of Freshwater Mobs, 1 in N chunks will generate with Freshwater Mobs (0 to disable)").defineInRange("gencontrol.freqwater", 8, 0, Integer.MAX_VALUE);
        probUnderground = builder.comment("Probability that an Underground block in a cave will attempt to spawn a mob (0 to disable). If YUNG's Better Caves is installed, this value is further reduced to 0.33x").defineInRange("gencontrol.probunderground", 0.0004, 0, 1);

        dimensionFeatureBlacklist = builder.comment("Prevent flora and other blocks (but not Burrows) from generating in the defined dimensions.").defineList("gencontrol.dimensionFeatureBlacklist", Lists.newArrayList(), string -> string instanceof String);
        //builder.pop();
    }

    private ForgeConfigSpec.BooleanValue define(String name, boolean value, String comment) {
        ForgeConfigSpec.BooleanValue option = builder.comment(comment).define(name, value);
        options.put(name, option);
        return option;
    }
}