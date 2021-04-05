package untamedwilds.entity.fish;

import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.AnimationHandler;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.server.ServerWorld;
import untamedwilds.config.ConfigGamerules;
import untamedwilds.entity.ComplexMob;
import untamedwilds.entity.ComplexMobAquatic;
import untamedwilds.entity.ISpecies;
import untamedwilds.entity.ai.MeleeAttackCircle;
import untamedwilds.entity.ai.SmartMateGoal;
import untamedwilds.entity.ai.target.HuntMobTarget;
import untamedwilds.entity.ai.target.HuntWeakerTarget;
import untamedwilds.entity.ai.unique.SharkSwimmingGoal;
import untamedwilds.init.ModEntity;
import untamedwilds.util.EntityUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EntityShark extends ComplexMobAquatic implements ISpecies, IAnimatedEntity {

    public static Animation ATTACK_THRASH;
    private static final String BREEDING = "MID_SUMMER";
    private static final int GROWING = 6 * ConfigGamerules.cycleLength.get();
    private static final int GESTATION = 8 * ConfigGamerules.cycleLength.get();
    private int animationTick;
    private Animation currentAnimation;

    public EntityShark(EntityType<? extends ComplexMob> type, World worldIn) {
        super(type, worldIn);
        ATTACK_THRASH = Animation.create(15);
        this.experienceValue = 10;
    }

    public static AttributeModifierMap.MutableAttribute registerAttributes() {
        return MobEntity.func_233666_p_()
                .createMutableAttribute(Attributes.ATTACK_DAMAGE, 12.0D)
                .createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.8D)
                .createMutableAttribute(Attributes.FOLLOW_RANGE, 32.0D)
                .createMutableAttribute(Attributes.KNOCKBACK_RESISTANCE, 1D)
                .createMutableAttribute(Attributes.MAX_HEALTH, 50.0D);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(2, new MeleeAttackCircle(this, 2.3D, false, 2));
        this.goalSelector.addGoal(3, new SmartMateGoal(this, 1D));
        this.goalSelector.addGoal(4, new SharkSwimmingGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new HuntMobTarget<>(this, LivingEntity.class, true, 30, false, false, input -> this.getEcoLevel(input) > 7));
        this.targetSelector.addGoal(3, new HuntWeakerTarget<>(this, LivingEntity.class, true, true));
    }

    public void livingTick() {
        AnimationHandler.INSTANCE.updateAnimations(this);
        if (!this.world.isRemote) {
            if (this.ticksExisted % 1000 == 0) {
                if (this.wantsToBreed() && !this.isMale()) {
                    this.setGrowingAge(this.getPregnancyTime());
                }
            }
            if (this.world.getGameTime() % 4000 == 0) {
                this.heal(1.0F);
            }
        }
        super.livingTick();
    }

    /* Breeding conditions for the Shark are:
     * A nearby Shark of different gender */
    public boolean wantsToBreed() {
        if (ConfigGamerules.naturalBreeding.get() && this.getGrowingAge() == 0 && EntityUtils.hasFullHealth(this)) {
            List<EntityShark> list = this.world.getEntitiesWithinAABB(EntityShark.class, this.getBoundingBox().grow(12.0D, 8.0D, 12.0D));
            list.removeIf(input -> (input.getGender() == this.getGender()) || (input.getVariant() != this.getVariant()) || input.getGrowingAge() != 0);
            if (list.size() >= 1) {
                this.setGrowingAge(GROWING);
                list.get(0).setGrowingAge(GROWING);
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public AgeableEntity func_241840_a(ServerWorld serverWorld, AgeableEntity ageableEntity) {
        return create_offspring(new EntityShark(ModEntity.SHARK, this.world));
    }

    protected <T extends ComplexMob> T create_offspring(T entity) {
        entity.setGender(this.rand.nextInt(2));
        entity.setMobSize(this.rand.nextFloat());
        entity.setGrowingAge(entity.getAdulthoodTime() * -2);
        entity.setVariant(this.getVariant());
        return entity;
    }

    public boolean isBottomDweller() { return SpeciesShark.values()[this.getVariant()].bottomDweller; }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.ENTITY_COD_FLOP;
    }
    public int getAdulthoodTime() { return GROWING; }
    public int getPregnancyTime() { return GESTATION; }
    public String getBreedingSeason() { return BREEDING; }
    protected int getOffspring() { return 3; }

    public ActionResultType func_230254_b_(PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getHeldItem(Hand.MAIN_HAND);
        if (hand == Hand.MAIN_HAND && !this.world.isRemote()) {
            if (itemstack.getItem() == Items.BLAZE_ROD) {
                this.setAnimation(ATTACK_THRASH);
            }
        }
        return super.func_230254_b_(player, hand);
    }

    @Override
    public int setSpeciesByBiome(RegistryKey<Biome> biomekey, Biome biome, SpawnReason reason) {
        if (reason == SpawnReason.SPAWN_EGG || ConfigGamerules.randomSpecies.get()) {
            return this.rand.nextInt(EntityShark.SpeciesShark.values().length);
        }
        return EntityShark.SpeciesShark.getSpeciesByBiome(biomekey);
    }

    public boolean attackEntityAsMob(Entity entityIn) {
        boolean flag = super.attackEntityAsMob(entityIn);
        if (flag && this.getAnimation() == NO_ANIMATION && !this.isChild()) {
            this.setAnimation(ATTACK_THRASH);
            this.setAnimationTick(0);
        }
        return flag;
    }

    public String getSpeciesName(int i) { return new TranslationTextComponent("entity.untamedwilds.shark_" + getRawSpeciesName(i)).getString(); }
    public String getRawSpeciesName(int i) { return SpeciesShark.values()[i].name().toLowerCase(); }

    public int getAnimationTick() { return animationTick; }
    public void setAnimationTick(int tick) { animationTick = tick; }
    public Animation getAnimation() { return currentAnimation; }
    public void setAnimation(Animation animation) { currentAnimation = animation; }
    public Animation[] getAnimations() { return new Animation[]{NO_ANIMATION, ATTACK_THRASH}; }

    // Model Parameters
    public boolean hasShortFins() { return SpeciesShark.values()[this.getVariant()].shortFins; }

    public enum SpeciesShark implements IStringSerializable {

        BIGEYE	    (0, 1.2F, 1, false, true, Biomes.DEEP_WARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.DEEP_OCEAN, Biomes.DEEP_COLD_OCEAN),
        BLUNTNOSE	(1, 1.6F, 2, true, true, Biomes.DEEP_WARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.DEEP_OCEAN, Biomes.DEEP_COLD_OCEAN),
        BULL    	(2, 1.0F, 4, false, false, Biomes.OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.WARM_OCEAN),
        GOBLIN  	(3, 1.0F, 1, true, true, Biomes.DEEP_WARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.DEEP_OCEAN, Biomes.DEEP_COLD_OCEAN),
        GREAT_WHITE	(4, 1.8F, 2, false, false, Biomes.DEEP_OCEAN, Biomes.DEEP_COLD_OCEAN),
        GREENLAND	(5, 1.8F, 1, true,false, Biomes.DEEP_COLD_OCEAN, Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN),
        HAMMERHEAD	(6, 1.3F, 2, false, false, Biomes.OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.WARM_OCEAN),
        LEMON   	(7, 0.9F, 1, false, false, Biomes.WARM_OCEAN),
        MAKO    	(8, 1.1F, 4, false, false, Biomes.DEEP_OCEAN, Biomes.DEEP_COLD_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.DEEP_WARM_OCEAN),
        TIGER	    (9, 1.3F, 2, false, false, Biomes.WARM_OCEAN, Biomes.DEEP_WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN);

        public Float scale;
        public int species;
        public int rolls;
        public boolean shortFins;
        public boolean bottomDweller;
        public RegistryKey<Biome>[] spawnBiomes;

        @SafeVarargs
        SpeciesShark(int species, Float scale, int rolls, boolean shortFins, boolean bottomDweller, RegistryKey<Biome>... biomes) {
            this.species = species;
            this.scale = scale;
            this.rolls = rolls;
            this.shortFins = shortFins;
            this.bottomDweller = bottomDweller;
            this.spawnBiomes = biomes;
        }

        public int getSpecies() { return this.species; }

        public String getString() {
            return I18n.format("entity.shark." + this.name().toLowerCase());
        }

        public static int getSpeciesByBiome(RegistryKey<Biome> biomekey) {
            List<EntityShark.SpeciesShark> types = new ArrayList<>();

            for (EntityShark.SpeciesShark type : values()) {
                for(RegistryKey<Biome> biomeTypes : type.spawnBiomes) {
                    if(biomekey.equals(biomeTypes)){
                        for (int i=0; i < type.rolls; i++) {
                            types.add(type);
                        }
                    }
                }
            }
            if (types.isEmpty()) {
                return 99;
            } else {
                return types.get(new Random().nextInt(types.size())).getSpecies();
            }
        }
    }
}