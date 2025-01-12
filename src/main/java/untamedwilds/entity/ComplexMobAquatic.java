package untamedwilds.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.DolphinLookController;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.PanicGoal;
import net.minecraft.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathType;
import net.minecraft.pathfinding.SwimmerPathNavigator;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;

import javax.annotation.Nullable;

public abstract class ComplexMobAquatic extends ComplexMob {

    public ComplexMobAquatic(EntityType<? extends ComplexMob> entity, World worldIn) {
        super(entity, worldIn);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        this.moveController = new ComplexMobAquatic.MoveHelperController(this);
        this.lookController = new DolphinLookController(this, 10);
    }

    @Override
    public void onEnterBubbleColumn(boolean downwards) {}

    @Override
    public void onEnterBubbleColumnWithAirAbove(boolean downwards) {}

    public boolean canBreatheUnderwater() {
        return true;
    }

    public CreatureAttribute getCreatureAttribute() {
        return CreatureAttribute.WATER;
    }

    public boolean isNotColliding(IWorldReader worldIn) {
        return worldIn.checkNoEntityCollision(this); // Suspect, may be inverted
    }

    protected float getStandingEyeHeight(Pose p_213348_1_, EntitySize p_213348_2_) {
        return p_213348_2_.height * 0.2F;
    }

    public void baseTick() {
        int i = this.getAir();
        super.baseTick();
        this.updateAir(i);
    }

    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new PanicGoal(this, 1.25D));
    }

    protected PathNavigator createNavigator(World worldIn) {
        return new SwimmerPathNavigator(this, worldIn);
    }

    public void livingTick() {
        if (!this.isInWater() && this.isOnGround() && this.collidedVertically) {
            this.setMotion(this.getMotion().add(((this.rand.nextFloat() * 2.0F - 1.0F) * 0.05F), 0.4000000059604645D, ((this.rand.nextFloat() * 2.0F - 1.0F) * 0.05F)));
            this.setOnGround(false);
            this.isAirBorne = true;
            this.playSound(this.getFlopSound(), this.getSoundVolume(), this.getSoundPitch());
        }

        super.livingTick();
    }

    protected void updateAir(int air) {
        if (this.isAlive() && !this.isInWaterOrBubbleColumn()) {
            this.setAir(air - 1);
            if (this.getAir() == -20) {
                this.setAir(0);
                this.attackEntityFrom(DamageSource.DROWN, 2.0F);
            }
        } else {
            this.setAir(300);
        }
    }

    public boolean isPushedByWater() {
        return false;
    }

    protected abstract SoundEvent getFlopSound();

    protected SoundEvent getSwimSound() {
        return SoundEvents.ENTITY_FISH_SWIM;
    }

    public void travel(Vector3d movement) {
        if (this.isServerWorld() && this.isInWater()) {
            this.moveRelative(this.getAIMoveSpeed(), movement);
            this.move(MoverType.SELF, this.getMotion());
            this.setMotion(this.getMotion().scale(0.9D));
            if (this.getAttackTarget() == null) {
                this.setMotion(this.getMotion().add(0.0D, -0.005D, 0.0D));
            }
        } else {
            super.travel(movement);
        }
    }

    public static class MoveHelperController extends MovementController {
        private final ComplexMob entity;
        private final float landMoveFactor;

        public MoveHelperController(ComplexMob entity) {
            super(entity);
            this.entity = entity;
            this.landMoveFactor = 0.1F;
        }

        public MoveHelperController(ComplexMob entity, float landMoveFactor) {
            super(entity);
            this.entity = entity;
            this.landMoveFactor = landMoveFactor;
        }

        public void tick() {
            if (this.entity.isInWater()) {
                this.entity.setMotion(this.entity.getMotion().add(0.0D, 0.0045D, 0.0D));
            }

            if (this.action == MovementController.Action.MOVE_TO && !this.entity.getNavigator().noPath()) {
                double d0 = this.getX() - this.entity.getPosX();
                double d1 = this.getY() - this.entity.getPosY();
                double d2 = this.getZ() - this.entity.getPosZ();
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                if (d3 < (double)2.5000003E-7F) {
                    this.mob.setMoveForward(0.0F);
                } else {
                    float f = (float)(MathHelper.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
                    this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, f, this.entity.turn_speed * 10);
                    this.entity.renderYawOffset = this.entity.rotationYaw;
                    this.entity.rotationYawHead = this.entity.rotationYaw;
                    float f1 = (float)(this.speed * this.entity.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
                    if (this.entity.isInWater()) {
                        this.entity.setAIMoveSpeed(f1 * 0.02F);
                        float f2 = -((float)(MathHelper.atan2(d1, MathHelper.sqrt(d0 * d0 + d2 * d2)) * (double)(180F / (float)Math.PI)));
                        f2 = MathHelper.clamp(MathHelper.wrapDegrees(f2), -85.0F, 85.0F);
                        this.entity.rotationPitch = this.limitAngle(this.entity.rotationPitch, f2, 5.0F);
                        float f3 = MathHelper.cos(this.entity.rotationPitch * ((float)Math.PI / 180F));
                        float f4 = MathHelper.sin(this.entity.rotationPitch * ((float)Math.PI / 180F));
                        this.entity.moveForward = f3 * f1;
                        this.entity.moveVertical = -f4 * f1;
                    } else {
                        this.entity.setAIMoveSpeed(f1 * landMoveFactor);
                    }
                }
            } else {
                this.entity.setAIMoveSpeed(0.0F);
                this.entity.setMoveStrafing(0.0F);
                this.entity.setMoveVertical(0.0F);
                this.entity.setMoveForward(0.0F);
            }
        }
    }

    protected static class SwimGoal extends RandomSwimmingGoal {

        public int heightFromBottom;

        public SwimGoal(ComplexMobAquatic entity) {
            super(entity, 1.0D, 20);
            this.heightFromBottom = -1;
        }

        public SwimGoal(ComplexMobAquatic entity, int offset) {
            super(entity, 1.0D, 20);
            this.heightFromBottom = offset;
        }

        public boolean shouldExecute() {
            return super.shouldExecute();
        }

        @Nullable
        protected Vector3d getPosition() {
            Vector3d vector3d = RandomPositionGenerator.findRandomTarget(this.creature, 10, 7);

            for(int i = 0; vector3d != null && !this.creature.world.getBlockState(new BlockPos(vector3d)).allowsMovement(this.creature.world, new BlockPos(vector3d), PathType.WATER) && i++ < 10; vector3d = RandomPositionGenerator.findRandomTarget(this.creature, 10, 7)) {
            }

            if (vector3d != null && this.heightFromBottom > 0 && this.creature.world.canBlockSeeSky(this.creature.getPosition())) {
                int offset = this.heightFromBottom + this.creature.getRNG().nextInt(7) - 4;
                return new Vector3d(vector3d.getX(), this.creature.world.getHeight(Heightmap.Type.OCEAN_FLOOR, (int)vector3d.getX(), (int)vector3d.getZ()) + offset, vector3d.getZ());
            }
            return vector3d;
        }
    }
}
