package untamedwilds.item;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import untamedwilds.UntamedWilds;
import untamedwilds.entity.ComplexMob;
import untamedwilds.entity.INeedsPostUpdate;
import untamedwilds.util.EntityUtils;
import untamedwilds.util.ItemGroupUT;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class MobBottledItem extends Item {
    private final EntityType<? extends ComplexMob> entity;

    public MobBottledItem(EntityType<? extends ComplexMob> typeIn, Properties properties) {
        super(properties);
        this.entity = typeIn;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        EntityUtils.buildTooltipData(stack, tooltip, this.entity, ComplexMob.ENTITY_DATA_HASH.get(this.entity).getSpeciesData().get(this.getSpecies(stack)).getName());
    }

    public String getTranslationKey(ItemStack stack) {
        return new TranslationTextComponent("item.untamedwilds.bottle_" + this.entity.getRegistryName().getPath()).getString() + "_" + ComplexMob.ENTITY_DATA_HASH.get(this.entity).getSpeciesData().get(this.getSpecies(stack)).getName();
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext useContext) {

        World worldIn = useContext.getWorld();
        if (!(worldIn instanceof ServerWorld)) {
            return ActionResultType.SUCCESS;
        } else {
            ItemStack itemStack = useContext.getItem();
            BlockPos pos = useContext.getPos();
            Direction facing = useContext.getFace();
            BlockState blockState = worldIn.getBlockState(pos);
            BlockPos spawnPos = blockState.getCollisionShape(worldIn, pos).isEmpty() ? pos : pos.offset(facing);

            EntityType<?> entity = EntityUtils.getEntityTypeFromTag(itemStack.getTag(), this.entity);
            boolean doVerticalOffset = !Objects.equals(pos, spawnPos) && facing == Direction.UP;
            if (useContext.getItem().getTag() != null && useContext.getItem().getTag().contains("EntityTag")) {
                EntityUtils.createMobFromItem((ServerWorld) worldIn, itemStack, entity, 0, spawnPos, useContext.getPlayer(), doVerticalOffset);
            }
            else {
                Entity spawn = entity.create((ServerWorld) worldIn, itemStack.getTag(), null, useContext.getPlayer(), spawnPos, SpawnReason.BUCKET, true, doVerticalOffset);
                if (spawn instanceof ComplexMob) {
                    ComplexMob entitySpawn = (ComplexMob) spawn;
                    entitySpawn.setVariant(this.getSpecies(itemStack));
                    entitySpawn.chooseSkinForSpecies(entitySpawn, true);
                    entitySpawn.setGrowingAge(entitySpawn.getAdulthoodTime() * -1);
                    if (spawn instanceof INeedsPostUpdate) {
                        ((INeedsPostUpdate) spawn).updateAttributes();
                    }
                    worldIn.addEntity(spawn);
                }
            }
            if (useContext.getPlayer() != null) {
                if (!useContext.getPlayer().isCreative()) {
                    itemStack.shrink(1);
                    useContext.getPlayer().inventory.addItemStackToInventory(new ItemStack(Items.GLASS_BOTTLE));
                }
            }
        }
        return ActionResultType.CONSUME;
    }

    private int getSpecies(ItemStack itemIn) {
        if (itemIn.getTag() != null && itemIn.getTag().contains("CustomModelData")) {
            return itemIn.getTag().getInt("CustomModelData");
        }
        UntamedWilds.LOGGER.error("No variant found in this itemstack NBT data");
        return 0;
    }

    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (group == ItemGroupUT.untamedwilds_items) {
            for(int i = 0; i < ComplexMob.ENTITY_DATA_HASH.get(this.entity).getSpeciesData().size(); i++) {
                CompoundNBT baseTag = new CompoundNBT();
                ItemStack item = new ItemStack(this);
                baseTag.putInt("variant", i);
                baseTag.putInt("CustomModelData", i);
                item.setTag(baseTag);
                items.add(item);
            }
        }
    }
}