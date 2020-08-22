package untamedwilds.init;

import com.google.common.collect.Lists;
import net.minecraft.item.*;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import untamedwilds.UntamedWilds;
import untamedwilds.config.ConfigMobControl;
import untamedwilds.entity.arthropod.Tarantula;
import untamedwilds.entity.fish.Sunfish;
import untamedwilds.entity.mollusk.GiantClam;
import untamedwilds.entity.reptile.EntitySnake;
import untamedwilds.entity.reptile.EntitySoftshellTurtle;
import untamedwilds.item.ItemMobEgg;
import untamedwilds.item.ItemMobSpawn;
import untamedwilds.item.ItemOwnershipDeed;
import untamedwilds.item.debug.*;
import untamedwilds.util.ItemGroupUT;

import java.util.List;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = UntamedWilds.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {

    public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, UntamedWilds.MOD_ID);
    public static final List<RegistryObject<Item>> SPAWN_EGGS = Lists.newArrayList();

    // Wild World Item instances
    // Materials
    public static RegistryObject<Item> MATERIAL_FAT = createItem("material_fat", () -> new Item(new Item.Properties().group(ItemGroup.MISC)));
    public static RegistryObject<Item> MATERIAL_PEARL = createItem("material_pearl", () -> new Item(new Item.Properties().group(ItemGroup.MISC)));
    public static RegistryObject<Item> RARE_GIANT_PEARL = createItem("material_giant_pearl", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON).group(ItemGroup.MISC)));

    // Food
    public static RegistryObject<Item> MEAT_BEAR_RAW = createItem("food_bear_raw", () -> new Item(new Item.Properties().food((new Food.Builder()).hunger(3).saturation(0.6F).meat().build()).group(ItemGroup.FOOD)));
    public static RegistryObject<Item> MEAT_BEAR_COOKED = createItem("food_bear_cooked", () -> new Item(new Item.Properties().food((new Food.Builder()).hunger(7).saturation(1F).meat().build()).group(ItemGroup.FOOD)));
    public static RegistryObject<Item> MEAT_TURTLE_RAW = createItem("food_turtle_raw", () -> new Item(new Item.Properties().food((new Food.Builder()).hunger(2).saturation(0.3F).meat().build()).group(ItemGroup.FOOD)));
    public static RegistryObject<Item> MEAT_TURTLE_COOKED = createItem("food_turtle_cooked", () -> new Item(new Item.Properties().food((new Food.Builder()).hunger(6).saturation(0.6F).meat().build()).group(ItemGroup.FOOD)));
    public static RegistryObject<Item> MEAT_HIPPO_RAW = createItem("food_hippo_raw", () -> new Item(new Item.Properties().food((new Food.Builder()).hunger(3).saturation(0.7F).meat().build()).group(ItemGroup.FOOD)));
    public static RegistryObject<Item> MEAT_HIPPO_COOKED = createItem("food_hippo_cooked", () -> new Item(new Item.Properties().food((new Food.Builder()).hunger(7).saturation(1.1F).meat().build()).group(ItemGroup.FOOD)));
    public static RegistryObject<Item> FOOD_TURTLE_SOUP = createItem("food_turtle_soup", () -> new SoupItem(new Item.Properties().food((new Food.Builder()).hunger(8).saturation(0.6F).build()).group(ItemGroup.FOOD)));

    // Hides
    public static RegistryObject<Item> HIDE_BEAR_ASHEN = createItem("hide_bear_ashen", () -> new Item(new Item.Properties().group(ItemGroup.MISC)));
    public static RegistryObject<Item> HIDE_BEAR_BLACK = createItem("hide_bear_black", () -> new Item(new Item.Properties().group(ItemGroup.MISC)));
    public static RegistryObject<Item> HIDE_BEAR_BROWN = createItem("hide_bear_brown", () -> new Item(new Item.Properties().group(ItemGroup.MISC)));
    public static RegistryObject<Item> HIDE_BEAR_WHITE = createItem("hide_bear_white", () -> new Item(new Item.Properties().group(ItemGroup.MISC)));
    public static RegistryObject<Item> HIDE_BIGCAT_JAGUAR = createItem("hide_bigcat_jaguar", () -> new Item(new Item.Properties().group(ItemGroup.MISC)));
    public static RegistryObject<Item> HIDE_BIGCAT_LEOPARD = createItem("hide_bigcat_leopard", () -> new Item(new Item.Properties().group(ItemGroup.MISC)));
    public static RegistryObject<Item> HIDE_BIGCAT_LION = createItem("hide_bigcat_lion", () -> new Item(new Item.Properties().group(ItemGroup.MISC)));
    public static RegistryObject<Item> HIDE_BIGCAT_PANTHER = createItem("hide_bigcat_panther", () -> new Item(new Item.Properties().group(ItemGroup.MISC)));
    public static RegistryObject<Item> HIDE_BIGCAT_PUMA = createItem("hide_bigcat_puma", () -> new Item(new Item.Properties().group(ItemGroup.MISC)));
    public static RegistryObject<Item> HIDE_BIGCAT_SNOW_LEOPARD = createItem("hide_bigcat_snow_leopard", () -> new Item(new Item.Properties().group(ItemGroup.MISC)));
    public static RegistryObject<Item> HIDE_BIGCAT_TIGER = createItem("hide_bigcat_tiger", () -> new Item(new Item.Properties().group(ItemGroup.MISC)));

    // Debug Tools
    public static RegistryObject<Item> OWNERSHIP_DEED = createItem("ownership_deed", () -> new ItemOwnershipDeed(new Item.Properties().maxStackSize(1).group(ItemGroupUT.untamedwilds_items)));
    public static RegistryObject<Item> DEBUG_ANALYZER = createItem("debug_analyzer", () -> new ItemAnalizer(new Item.Properties().maxStackSize(1).group(ItemGroupUT.untamedwilds_items).rarity(Rarity.EPIC)));
    public static RegistryObject<Item> DEBUG_IPECAC = createItem("debug_ipecac", () -> new ItemIpecac(new Item.Properties().maxStackSize(1).group(ItemGroupUT.untamedwilds_items).rarity(Rarity.EPIC)));
    public static RegistryObject<Item> DEBUG_LOVE_POTION = createItem("debug_love_potion", () -> new ItemLovePotion(new Item.Properties().maxStackSize(1).group(ItemGroupUT.untamedwilds_items).rarity(Rarity.EPIC)));
    public static RegistryObject<Item> DEBUG_ERASER = createItem("debug_eraser", () -> new ItemEraser(new Item.Properties().maxStackSize(1).group(ItemGroupUT.untamedwilds_items).rarity(Rarity.EPIC)));
    public static RegistryObject<Item> DEBUG_GROWTH_TONIC = createItem("debug_growth_tonic", () -> new ItemGrowthTonic(new Item.Properties().maxStackSize(1).group(ItemGroupUT.untamedwilds_items).rarity(Rarity.EPIC)));

    public static <I extends Item> RegistryObject<I> createItem(String name, Supplier<? extends I> supplier) {
        return ModItems.ITEMS.register(name, supplier);
    }

    public static void registerSpawnItems(DeferredRegister<Item> registry) {
        // These items have no associated objects, as they are not supposed to be accessed, and I do not want to independently register each of them
        // Tarantula Items
        if (ConfigMobControl.addTarantula.get()) {
            for (int i = 0; i < Tarantula.SpeciesTarantula.values().length; i++) {
                int tarantulaSpecies = i;
                ModItems.ITEMS.register("tarantula_" + Tarantula.SpeciesTarantula.values()[i].name().toLowerCase(), () -> new ItemMobSpawn(ModEntity.TARANTULA, tarantulaSpecies, Tarantula.SpeciesTarantula.values()[tarantulaSpecies].name().toLowerCase(), new Item.Properties().group(ItemGroup.MISC)));
                ModItems.ITEMS.register("egg_tarantula_" + Tarantula.SpeciesTarantula.values()[i].name().toLowerCase(), () -> new ItemMobEgg(ModEntity.TARANTULA, tarantulaSpecies, new Item.Properties().group(ItemGroup.MISC)));
            }
        }
        // Small Snake Items
        if (ConfigMobControl.addSnake.get()) {
            for (int i = 0; i < EntitySnake.SpeciesSnake.values().length; i++) {
                int snake = i;
                ModItems.ITEMS.register("snake_" + EntitySnake.SpeciesSnake.values()[i].name().toLowerCase(), () -> new ItemMobSpawn(ModEntity.SNAKE, snake, EntitySnake.SpeciesSnake.values()[snake].name().toLowerCase(), new Item.Properties().group(ItemGroup.MISC)));
                ModItems.ITEMS.register("egg_snake_" + EntitySnake.SpeciesSnake.values()[i].name().toLowerCase(), () -> new ItemMobEgg(ModEntity.SNAKE, snake, new Item.Properties().group(ItemGroup.MISC)));
            }
        }
        // Softshell Turtle Items
        if (ConfigMobControl.addSoftshellTurtle.get()) {
            for (int i = 0; i < EntitySoftshellTurtle.SpeciesSoftshellTurtle.values().length; i++) {
                int softshell_turtleSpecies = i;
                ModItems.ITEMS.register("softshell_turtle_" + EntitySoftshellTurtle.SpeciesSoftshellTurtle.values()[i].name().toLowerCase(), () -> new ItemMobSpawn(ModEntity.SOFTSHELL_TURTLE, softshell_turtleSpecies, EntitySoftshellTurtle.SpeciesSoftshellTurtle.values()[softshell_turtleSpecies].name().toLowerCase(), new Item.Properties().group(ItemGroup.MISC)));
                ModItems.ITEMS.register("egg_softshell_turtle_" + EntitySoftshellTurtle.SpeciesSoftshellTurtle.values()[i].name().toLowerCase(), () -> new ItemMobEgg(ModEntity.SOFTSHELL_TURTLE, softshell_turtleSpecies, new Item.Properties().group(ItemGroup.MISC)));
            }
        }
        // Giant Clam Items
        if (ConfigMobControl.addGiantClam.get()) {
            for (int i = 0; i < GiantClam.SpeciesGiantClam.values().length; i++) {
                int giant_clamSpecies = i;
                ModItems.ITEMS.register("giant_clam_" + GiantClam.SpeciesGiantClam.values()[i].name().toLowerCase(), () -> new ItemMobSpawn(ModEntity.GIANT_CLAM, giant_clamSpecies, GiantClam.SpeciesGiantClam.values()[giant_clamSpecies].name().toLowerCase(), new Item.Properties().group(ItemGroup.MISC)));
                ModItems.ITEMS.register("egg_giant_clam_" + GiantClam.SpeciesGiantClam.values()[i].name().toLowerCase(), () -> new ItemMobEgg(ModEntity.GIANT_CLAM, giant_clamSpecies, new Item.Properties().group(ItemGroup.MISC)));
            }
        }
        // Sunfish Items
        if (ConfigMobControl.addSunfish.get()) {
            for (int i = 0; i < Sunfish.SpeciesSunfish.values().length; i++) {
                int sunfishSpecies = i;
                ModItems.ITEMS.register("egg_sunfish_" + Sunfish.SpeciesSunfish.values()[i].name().toLowerCase(), () -> new ItemMobEgg(ModEntity.SUNFISH, sunfishSpecies, new Item.Properties().group(ItemGroup.MISC)));
            }
        }
    }
}
