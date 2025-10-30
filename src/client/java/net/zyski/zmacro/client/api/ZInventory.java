package net.zyski.zmacro.client.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ZInventory {


    public boolean hasSpaceFor(Block block) {
        return hasSpaceFor(new ItemStack(block.asItem()));
    }

    public boolean hasSpaceFor(BlockPos pos) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return false;

        BlockState state = player.level().getBlockState(pos);
        return state.isAir() || hasSpaceFor(state.getBlock());
    }

    public boolean hasSpaceFor(ItemStack stack) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || stack.isEmpty()) return false;

        Inventory inventory = player.getInventory();
        return inventory.getFreeSlot() != -1 ||
                inventory.getSlotWithRemainingSpace(stack) != -1;
    }
}
