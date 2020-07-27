package untamedwilds.entity.mammal.bear;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.OwnerHurtByTargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import untamedwilds.config.ConfigGamerules;
import untamedwilds.entity.ai.*;
import untamedwilds.entity.ai.target.SmartOwnerHurtTargetGoal;
import untamedwilds.entity.ai.unique.PandaBreakBamboo;
import untamedwilds.entity.ai.unique.PandaEatBamboo;
import untamedwilds.init.ModEntity;
import untamedwilds.init.ModLootTables;

import javax.annotation.Nullable;
import java.util.List;

public class PandaBear extends AbstractBear {

    private static final ResourceLocation TEXTURE = new ResourceLocation("untamedwilds:textures/entity/bear/panda.png");
    private static final float SIZE = 0.7f;
    private static final String BREEDING = "MID_SPRING";
    private static final int GESTATION = 6 * ConfigGamerules.cycleLength.get();
    private static final int GROWING = 8 * ConfigGamerules.cycleLength.get();
    private static final int RARITY = 0;

    public PandaBear(EntityType<? extends AbstractBear> type, World worldIn) {
        super(type, worldIn);
        this.ecoLevel = 6;
    }

    public void registerGoals() {
        this.goalSelector.addGoal(1, new SmartSwimGoal(this));
        this.goalSelector.addGoal(2, new SmartMeleeAttackGoal(this, 2.3D, false, 1));
        this.goalSelector.addGoal(2, new PandaEatBamboo(this, 40, 16));
        this.goalSelector.addGoal(3, new SmartFollowOwnerGoal(this, 2.3D, 16.0F, 3.0F));
        this.goalSelector.addGoal(3, new SmartAvoidGoal<>(this, LivingEntity.class, 16, 1.2D, 1.6D, input -> this.getEcoLevel(input) > 6));
        this.goalSelector.addGoal(3, new PandaBreakBamboo(this, 40));
        this.goalSelector.addGoal(4, new SmartMateGoal(this, 1D));
        this.goalSelector.addGoal(4, new GotoSleepGoal(this, 1D, true));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25D));
        this.goalSelector.addGoal(5, new SmartWanderGoal(this, 1D, true));
        this.goalSelector.addGoal(6, new SmartLookAtGoal(this, LivingEntity.class, 10.0F));
        //this.goalSelector.addGoal(7, new SmartLookRandomlyGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new SmartOwnerHurtTargetGoal(this));
    }

    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(25D);
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.13D);
        this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(4D);
        this.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D);
        this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(24.0D);
    }

    /* Crepuscular-Nocturnal: Active between 6:00 and 9:00 and between 20:00 and 2:00 */
    public boolean isActive() {
        super.isActive();
        long time = this.world.getDayTime();
        return time < 3000 || (time > 14000 && time < 20000);
    }

    /* Breeding conditions for the Black Bear are:
     * Warm Biome (T between than 0.7 and 1.0)
     * No other entities nearby */
    public boolean wantsToBreed() {
        super.wantsToBreed();
        if (ConfigGamerules.naturalBreeding.get() && !this.isSleeping() && this.getGrowingAge() == 0 && this.getHealth() == this.getMaxHealth() && this.getHunger() >= 80) {
            if (ConfigGamerules.hardcoreBreeding.get()) {
                List<LivingEntity> list = this.world.getEntitiesWithinAABB(LivingEntity.class, this.getBoundingBox().grow(6.0D, 4.0D, 6.0D));
                float i = this.world.getBiome(this.getPosition()).getTemperature(this.getPosition());
                return i >= 0.7 && i <= 1 && list.size() < 3;
            }
            return true;
        }
        return false;
    }

    public void breed() {
        for (int i = 0; i <= 1 + this.rand.nextInt(1); i++) {
            PandaBear child = this.createChild(this);
            child.setLocationAndAngles(this.getPosX(), this.getPosY(), this.getPosZ(), 0.0F, 0.0F);
            if (this.getOwner() != null) {
                child.setTamedBy((PlayerEntity) this.getOwner());
            }
            this.world.addEntity(child);
        }
    }

    @Nullable
    public PandaBear createChild(AgeableEntity ageable) {
        PandaBear bear = new PandaBear(ModEntity.PANDA_BEAR, this.world);
        bear.setGender(this.rand.nextInt(2));
        bear.setMobSize(this.rand.nextFloat());
        bear.setGrowingAge(this.getAdulthoodTime() * -2);
        return bear;
    }

    @Override
    protected ResourceLocation getLootTable() {
        return ModLootTables.BEAR_LOOT_PANDA;
    }
    public boolean isFavouriteFood(ItemStack stack) { return stack.getItem() == Items.BAMBOO; }
    public String getBreedingSeason() { return BREEDING; }
    public static int getRarity() { return RARITY; }
    public int getAdulthoodTime() { return GROWING; }
    public int getPregnancyTime() { return GESTATION; }
    public float getModelScale() { return SIZE; }
    public ResourceLocation getTexture() { return TEXTURE; }
    public boolean hasShortSnout() { return true; }
}