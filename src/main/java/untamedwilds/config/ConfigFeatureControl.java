package untamedwilds.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigFeatureControl {
    public static ForgeConfigSpec.BooleanValue addAnemones;
    public static ForgeConfigSpec.BooleanValue addReeds;
    public static ForgeConfigSpec.BooleanValue addBushes;
    public static ForgeConfigSpec.BooleanValue addAlgae;
    public static ForgeConfigSpec.BooleanValue addTreeOrchids;

    public static ForgeConfigSpec.IntValue freqCritter;
    public static ForgeConfigSpec.IntValue freqOcean;
    public static ForgeConfigSpec.IntValue freqApex;

    ConfigFeatureControl(final ForgeConfigSpec.Builder builder) {
        //builder.push("feature_control");
        builder.comment("Options pertaining to blocks and their worldgen");
        addAnemones = builder.comment("Controls whether to add Anemones and their associated items to oceans.").define("gencontrol.anemone", true);
        addReeds = builder.comment("Controls whether to add Reeds and their associated items.").define("gencontrol.reeds", true);
        addBushes = builder.comment("Controls whether to add Bushes and their associated items to most biomes.").define("gencontrol.bush", true);
        addTreeOrchids = builder.comment("Controls whether to add Tree Orchids and their associated items. (NYI)").define("gencontrol.tree_orchid", false);
        addAlgae = builder.comment("Controls whether to add Amazon Sword and their associated items").define("gencontrol.algae", true);

        freqCritter = builder.comment("Frequency of Critters, 1 in N chunks will generate with Critters (0 to disable)").defineInRange("gencontrol.freqcritter", 3, 0, Integer.MAX_VALUE);
        freqOcean = builder.comment("Frequency of Ocean Mobs, 1 in N chunks will generate with Ocean Mobs (0 to disable)").defineInRange("gencontrol.freqocean", 16, 0, Integer.MAX_VALUE);
        freqApex = builder.comment("Frequency of Apex Predators, 1 in N chunks will generate with an Apex Predator (0 to disable)").defineInRange("gencontrol.freqapex", 24, 0, Integer.MAX_VALUE);
        //builder.pop();
    }
}
