package untamedwilds.entity.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.Vec3d;
import untamedwilds.entity.ComplexMob;

import java.util.List;
import java.util.function.Predicate;

public class SmartAvoidGoal <T extends LivingEntity> extends AvoidEntityGoal<T> {

    protected ComplexMob taskOwner;
    protected LivingEntity entityToAvoid;
    private final java.util.function.Predicate<? super T> targetEntitySelector;

    public SmartAvoidGoal(ComplexMob entityIn, Class<T> classToAvoidIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn) {
        super(entityIn, classToAvoidIn, avoidDistanceIn, farSpeedIn, nearSpeedIn, EntityPredicates.CAN_AI_TARGET::test);
        targetEntitySelector = null;
    }

    public SmartAvoidGoal(ComplexMob entityIn, Class<T> classToAvoidIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn, final Predicate<? super T > targetSelector) {
        super(entityIn, classToAvoidIn, avoidDistanceIn, farSpeedIn, nearSpeedIn, EntityPredicates.CAN_AI_TARGET::test);
        this.targetEntitySelector = (Predicate<T>) entity -> targetSelector != null && targetSelector.test(entity) && !entity.isSpectator();
    }

    public boolean shouldExecute() {
        if (this.entity instanceof ComplexMob) {
            this.taskOwner = (ComplexMob)this.entity;
        }
        if (this.taskOwner.getAttackTarget() != null || this.taskOwner.isSleeping()) {
            return false;
        }
        List<T> list;
        if (this.targetEntitySelector != null) {
            list = this.taskOwner.world.getEntitiesWithinAABB(classToAvoid, this.taskOwner.getBoundingBox().grow(avoidDistance, 4f, avoidDistance), this.targetEntitySelector::test);
        }
        else {
            list = this.taskOwner.world.getEntitiesWithinAABB(classToAvoid, this.taskOwner.getBoundingBox().grow(avoidDistance, 4f, avoidDistance));
        }
        list.removeIf(input -> input instanceof PlayerEntity && (((PlayerEntity) input).isCreative() || input.isSpectator() || (this.taskOwner.getOwner() == input))); // TODO: Shitty patchwork, to be changed

        if (list.isEmpty()) {
            return false;
        }
        else {
            avoidTarget = list.get(0);
        }
        path = this.taskOwner.getNavigator().getPathToEntity(avoidTarget, 0);
        if (path != null) {
            Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.taskOwner, 16, 7, new Vec3d(this.avoidTarget.getPosX(), this.avoidTarget.getPosY(), this.avoidTarget.getPosZ()));
            if (vec3d == null) {
                return false;
            } else if (this.avoidTarget.getDistanceSq(vec3d.x, vec3d.y, vec3d.z) < this.avoidTarget.getDistanceSq(this.entity)) {
                return false;
            } else {
                this.path = this.navigation.getPathToPos(vec3d.x, vec3d.y, vec3d.z, 0);
                return this.path != null;
            }
        }
        return false;
    }

    public void startExecuting() {
        super.startExecuting();
    }
}