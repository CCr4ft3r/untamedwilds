package untamedwilds.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import untamedwilds.UntamedWilds;
import untamedwilds.config.ConfigGamerules;
import untamedwilds.entity.ComplexMob;
import untamedwilds.entity.ISpecies;
import untamedwilds.entity.relict.EntitySpitter;
import untamedwilds.init.ModAdvancementTriggers;

@Mod.EventBusSubscriber(modid = UntamedWilds.MOD_ID)
public class LookThroughSpyglassEvent {

    @SubscribeEvent
    public static void lookAtEntityThroughSpyglassEvent(LivingEntityUseItemEvent event) {
        ItemStack usedItem = event.getItem();
        Entity entity = event.getEntity();
        if (ConfigGamerules.spyglassBehaviorChange.get() && !entity.getLevel().isClientSide && entity instanceof Player playerIn && playerIn.tickCount % 20 == 0 && usedItem.getItem().equals(Items.SPYGLASS)) {
            HitResult hitresult = raycast(playerIn, ConfigGamerules.spyglassCheckRange.get(), true);
            if (hitresult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHitResult = (EntityHitResult) hitresult;
                if (entityHitResult.getEntity() instanceof LivingEntity livingEntityHitResult) {
                    displayEntityData(livingEntityHitResult, playerIn, playerIn.getLevel());

                    // TODO: Hardcoded list of "observing" advancements
                    if (entityHitResult.getEntity() instanceof EntitySpitter)
                        ModAdvancementTriggers.DISCOVERED_SPITTER.trigger((ServerPlayer) playerIn);
                }
            }
        }
    }

    public static HitResult raycast(Entity origin, double maxDistance, boolean hitsEntities) {
        Vec3 startPos = origin.getEyePosition(1F);
        Vec3 rotation = origin.getViewVector(1F);
        Vec3 endPos = startPos.add(rotation.x * maxDistance, rotation.y * maxDistance, rotation.z * maxDistance);
        HitResult hitResult = origin.level.clip(new ClipContext(startPos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, origin));

        if (hitResult.getType() != HitResult.Type.MISS)
            endPos = hitResult.getLocation();

        maxDistance *= 5;
        HitResult entityHitResult = ProjectileUtil.getEntityHitResult(origin, startPos, endPos, origin.getBoundingBox().expandTowards(rotation.scale(maxDistance)).inflate(1.0D, 1.0D, 1.0D), entity -> !entity.isSpectator(), maxDistance);

        if (hitsEntities && entityHitResult != null)
            hitResult = entityHitResult;

        return hitResult;
    }

    private static void displayEntityData(LivingEntity target, Player playerIn, Level world) {
        MutableComponent name = MutableComponent.create(new LiteralContents(""));
        if (target instanceof ComplexMob entity) {
            String entityName = entity instanceof ISpecies ? ((ISpecies) entity).getSpeciesName() : entity.getName().getString();
            name.append((entity.isBaby() ? "Young " : "") + (ConfigGamerules.genderedBreeding.get() ? entity.getGenderString() + " " : "") + entityName + " ");

            if (ConfigGamerules.scientificNames.get()) {
                String useVarName = entity instanceof ISpecies ? "_" + ((ISpecies) entity).getRawSpeciesName(entity.getVariant()) : "";
                name.append("(");
                name.append(MutableComponent.create(new TranslatableContents(entity.getType().getDescriptionId() + useVarName + ".sciname")).withStyle(ChatFormatting.ITALIC));
                name.append(") ");
            }
            if (!entity.isMale() && entity.getAge() > 0 && !ConfigGamerules.easyBreeding.get()) {
                name.append("This female is pregnant ");
            }
        } else {
            name.append(target.isBaby() ? "Young " : "" + target.getName().getString() + " ");
        }
        if (true) {
            int health = (int) (10 * target.getHealth() / target.getMaxHealth());
            MutableComponent state = getHealthState(health);
            name.append("(");
            name.append(state);
            name.append(") ");
        }
        if (true) {
            name.append("(");
            name.append(getThreatLevel(target, playerIn));
            name.append(")");
        }
        playerIn.displayClientMessage(name, true);
    }

    private static MutableComponent getHealthState(int health) {
        switch (health) {
            case 10, 9, 8 -> {
                return MutableComponent.create( new LiteralContents("Healthy")).withStyle(ChatFormatting.GREEN);
            }
            case 7, 6, 5 -> {
                return MutableComponent.create( new LiteralContents("Injured")).withStyle(ChatFormatting.YELLOW);
            }
            case 4, 3, 2 -> {
                return MutableComponent.create( new LiteralContents("Wounded")).withStyle(ChatFormatting.RED);
            }
            case 1, 0 -> {
                return MutableComponent.create( new LiteralContents("Almost Dead")).withStyle(ChatFormatting.DARK_RED);
            }
        }
        return MutableComponent.create(LiteralContents.EMPTY);
    }

    private static MutableComponent getThreatLevel(LivingEntity target, Player player) {
        int val = ComplexMob.getEcoLevel(player) - ComplexMob.getEcoLevel(target);
        if (val > 4)
            return MutableComponent.create( new LiteralContents("Harmless")).withStyle(ChatFormatting.GREEN);
        else if (val > 2)
            return MutableComponent.create( new LiteralContents("Mild threat")).withStyle(ChatFormatting.YELLOW);
        else if (val > 0)
            return MutableComponent.create( new LiteralContents("Caution")).withStyle(ChatFormatting.YELLOW);
        else if (val > -4)
            return MutableComponent.create( new LiteralContents("Dangerous")).withStyle(ChatFormatting.RED);
        else
            return MutableComponent.create( new LiteralContents("Deadly")).withStyle(ChatFormatting.DARK_RED);
    }
}