package untamedwilds.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockStateMatcher;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import untamedwilds.entity.ComplexMobTerrestrial;

import java.util.EnumSet;
import java.util.function.Predicate;

public class GrazeGoal extends Goal
{
    private static final Predicate<BlockState> IS_GRASS = BlockStateMatcher.forBlock(Blocks.GRASS);
    private final ComplexMobTerrestrial taskOwner;
    private final World entityWorld;
    private int eatingGrassTimer;
    private final int executionChance;

    public GrazeGoal(ComplexMobTerrestrial taskOwner, int executionChance) {
        this.taskOwner = taskOwner;
        this.entityWorld = taskOwner.world;
        this.executionChance = executionChance;
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    public boolean shouldExecute() {
        if (this.taskOwner.isSleeping() || this.taskOwner.isSitting() || this.taskOwner.isChild() || this.taskOwner.getHunger() > 100) {
            return false;
        }
        if (this.taskOwner.getAttackTarget() != null) {
            return false;
        }
        if (this.taskOwner.getRNG().nextInt(executionChance) != 0) {
            return false;
        }
        else {
            BlockPos blockpos = new BlockPos(this.taskOwner);
            if (IS_GRASS.test(this.entityWorld.getBlockState(blockpos))) {
                return true;
            } else {
                return this.entityWorld.getBlockState(blockpos.down()).getBlock() == Blocks.GRASS_BLOCK;
            }
        }
    }

    public void startExecuting() {
        this.eatingGrassTimer = 40;
        this.entityWorld.setEntityState(this.taskOwner, (byte)10);
        this.taskOwner.getNavigator().clearPath();
        this.taskOwner.setAnimation(this.taskOwner.getAnimationEat());
    }

    public void resetTask()
    {
        this.eatingGrassTimer = 0;
    }

    public boolean shouldContinueExecuting()
    {
        return this.eatingGrassTimer > 0;
    }

    public void tick() {
        this.eatingGrassTimer = Math.max(0, this.eatingGrassTimer - 1);
        if (this.eatingGrassTimer == 4) {
            BlockPos blockpos = new BlockPos(this.taskOwner);
            if (IS_GRASS.test(this.entityWorld.getBlockState(blockpos))) {
                if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.entityWorld, this.taskOwner)) {
                    this.entityWorld.destroyBlock(blockpos, false);
                }
                this.taskOwner.addHunger(16);
                this.taskOwner.eatGrassBonus();
            } else {
                BlockPos blockpos1 = blockpos.down();
                if (this.entityWorld.getBlockState(blockpos1).getBlock() == Blocks.GRASS_BLOCK) {
                    if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.entityWorld, this.taskOwner)) {
                        this.entityWorld.playEvent(2001, blockpos1, Block.getStateId(Blocks.GRASS_BLOCK.getDefaultState()));
                        this.entityWorld.setBlockState(blockpos1, Blocks.DIRT.getDefaultState(), 2);
                    }
                    this.taskOwner.addHunger(16);
                    this.taskOwner.eatGrassBonus();
                }
            }
        }
    }
}