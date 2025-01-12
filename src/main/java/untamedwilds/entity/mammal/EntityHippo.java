package untamedwilds.entity.mammal;

import com.github.alexthe666.citadel.animation.Animation;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import untamedwilds.config.ConfigGamerules;
import untamedwilds.entity.ComplexMob;
import untamedwilds.entity.ComplexMobAmphibious;
import untamedwilds.entity.INewSkins;
import untamedwilds.entity.ISpecies;
import untamedwilds.entity.ai.*;
import untamedwilds.entity.ai.unique.HippoTerritoryTargetGoal;
import untamedwilds.init.ModEntity;
import untamedwilds.util.EntityUtils;

import javax.annotation.Nullable;

public class EntityHippo extends ComplexMobAmphibious implements INewSkins, ISpecies {

    public static Animation EAT;
    public static Animation IDLE_YAWN;
    public static Animation IDLE_LOOK;
    public static Animation ATTACK;
    public static Animation IDLE_TALK;
    public int angryProgress;

    public EntityHippo(EntityType<? extends ComplexMob> type, World worldIn) {
        super(type, worldIn);
        IDLE_YAWN = Animation.create(36);
        IDLE_LOOK = Animation.create(128);
        IDLE_TALK = Animation.create(20);
        EAT = Animation.create(48);
        ATTACK = Animation.create(24);
        this.stepHeight = 1F;
        this.experienceValue = 10;
        this.isAmphibious = true;
        this.buoyancy = 0.998F;
        this.turn_speed = 0.3F;
    }

    public void registerGoals() {
        this.goalSelector.addGoal(2, new SmartMeleeAttackGoal(this, 1.4D, false));
        this.goalSelector.addGoal(3, new SmartMateGoal(this, 0.8D));
        this.goalSelector.addGoal(3, new GrazeGoal(this, 10));
        this.goalSelector.addGoal(4, new AmphibiousTransition(this, 1.1D));
        this.goalSelector.addGoal(4, new GotoSleepGoal(this, 1D));
        this.goalSelector.addGoal(5, new SmartWanderGoal(this, 1D, 120, 0, false));
        this.goalSelector.addGoal(5, new AmphibiousRandomSwimGoal(this, 1, 120));
        this.goalSelector.addGoal(6, new SmartLookAtGoal(this, LivingEntity.class, 10.0F));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setCallsForHelp());
        this.targetSelector.addGoal(3, new HippoTerritoryTargetGoal<>(this, LivingEntity.class, true, false, input -> !(input instanceof EntityHippo || input instanceof ISpecies || getEcoLevel(input) > 5)));
    }

    public static AttributeModifierMap.MutableAttribute registerAttributes() {
        return MobEntity.func_233666_p_()
                .createMutableAttribute(Attributes.ATTACK_DAMAGE, 9.0D)
                .createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.2D)
                .createMutableAttribute(Attributes.FOLLOW_RANGE, 24.0D)
                .createMutableAttribute(Attributes.MAX_HEALTH, 60.0D)
                .createMutableAttribute(Attributes.KNOCKBACK_RESISTANCE, 1D)
                .createMutableAttribute(Attributes.ARMOR, 0D);
    }

    public boolean wantsToBreed() {
        if (ConfigGamerules.naturalBreeding.get() && this.growingAge == 0) {
            return this.getHunger() >= 80;
        }
        return false;
    }

    @Override
    public void livingTick() {
        if (!this.world.isRemote) {
            if (this.world.getGameTime() % 1000 == 0) {
                this.addHunger(-10);
                if (!this.isStarving()) {
                    this.heal(1.0F);
                }
            }
            int i = this.rand.nextInt(3000);
            if (i <= 8 && !this.isInWater() && !this.isAngry() && !this.isSleeping() && this.getAnimation() == NO_ANIMATION) {
                this.setAnimation(IDLE_YAWN);
            }
            if ((i > 8 && i <= 12) && !this.isAngry() && !this.isSleeping() && this.getAnimation() == NO_ANIMATION) {
                this.setAnimation(IDLE_LOOK);
            }
            if (i == 13 && !this.isInWater() && this.isNotMoving() && this.canMove() && this.getAnimation() == NO_ANIMATION) {
                this.setSitting(true);
            }
            if (i == 14 && this.isSitting()) {
                this.setSitting(false);
            }
            if (i == 15 && !this.isActive() && !this.isSleeping() && this.isInWater()){
                this.setAnimation(IDLE_YAWN);
                this.setSleeping(true);
                this.forceSleep = -800 - this.rand.nextInt(1200);
            }
            if (i > 2980 && !this.isChild()) {
                this.setAnimation(IDLE_TALK);
            }
            if (this.getAnimation() == ATTACK && this.getAttackTarget() != null && this.getBoundingBox().grow(1.2F, 1.0F, 1.2F).contains(this.getAttackTarget().getPositionVec()) && (this.getAnimationTick() > 8)) {
                LivingEntity target = this.getAttackTarget();
                this.getAttackTarget().attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getAttribute(Attributes.ATTACK_DAMAGE).getValue());
                EntityUtils.destroyBoat(this.world, target);
            }
            this.setAngry(this.getAttackTarget() != null);
        }
        if (this.getAnimation() != NO_ANIMATION) {
            if (this.getAnimation() == IDLE_TALK && this.getAnimationTick() == 1) {
                this.playSound(this.getAmbientSound(), 1.5F, 0.8F);
            }
        }
        if (this.world.isRemote && this.isAngry() && this.angryProgress < 40) {
            this.angryProgress += 4;
        } else if (this.world.isRemote && !this.isAngry() && this.angryProgress > 0) {
            this.angryProgress -= 4;
        }
        super.livingTick();
    }

    @Override
    public boolean wantsToLeaveWater(){
        return this.isActive();
    }

    @Override
    public boolean wantsToEnterWater(){
        return !this.isActive();
    }

    public boolean attackEntityAsMob(Entity entityIn) {
        boolean flag = super.attackEntityAsMob(entityIn);
        if (flag && this.getAnimation() == NO_ANIMATION && !this.isChild()) {
            Animation anim = chooseAttackAnimation();
            this.setAnimation(anim);
        }
        return flag;
    }

    private Animation chooseAttackAnimation() {
        return ATTACK;
    }

    @Nullable
    public EntityHippo func_241840_a(ServerWorld serverWorld, AgeableEntity ageable) {
        return create_offspring(new EntityHippo(ModEntity.HIPPO, this.world));
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{NO_ANIMATION, EAT, IDLE_YAWN, IDLE_LOOK, IDLE_TALK, ATTACK};
    }

    public Animation getAnimationEat() { return EAT; }
}
