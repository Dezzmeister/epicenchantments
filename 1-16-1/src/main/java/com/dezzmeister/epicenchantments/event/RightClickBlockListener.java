package com.dezzmeister.epicenchantments.event;

import java.util.concurrent.atomic.AtomicBoolean;

import com.dezzmeister.epicenchantments.bindings.Activation.RightClick;
import com.dezzmeister.epicenchantments.bindings.NBTKeys;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RightClickBlockListener {
	private final AtomicBoolean handled;
	
	public RightClickBlockListener(final AtomicBoolean _handled) {
		handled = _handled;
	}
	
	@SubscribeEvent
	public void receive(final PlayerInteractEvent.RightClickBlock event) {
		if (event.getWorld().isRemote()) {
			return;	// Don't do anything on client-side
		}
		
		final PlayerEntity actor = event.getPlayer();
		final BlockPos target = event.getPos();
		final MinecraftServer server = event.getWorld().getServer();
		
		if (event.getHand() == Hand.OFF_HAND) {
			return;
		}
		// System.out.println("BLOCK EVENT");
		handled.set(true);
		
		final ItemStack itemStackInUse = actor.getHeldItem(actor.getActiveHand());
		final Item itemInUse = itemStackInUse.getItem();
		
		if (itemInUse.equals(Items.AIR)) {
			return;
		}
		
		final CompoundNBT tag = itemStackInUse.getTag();
		final INBT bindingsTag = (tag == null) ? null : tag.get("RightBindings");
		
		if (!(bindingsTag instanceof ListNBT)) {
			return;
		}
		
		final CommandSource source = actor.getCommandSource().withPermissionLevel(4).withWorld((ServerWorld)event.getWorld()).withPos(new Vector3d(target.getX() + 0.5, target.getY(), target.getZ() + 0.5));
		
		final ListNBT bindingsList = (ListNBT) bindingsTag;
		
		for (int i = 0; i < bindingsList.size(); i++) {
			final CompoundNBT cmdObject = bindingsList.getCompound(i);
			if (cmdObject == null) continue;
			
			final String activationStr = cmdObject.getString(NBTKeys.ACTIVATION);
			final RightClick activation = RightClick.getEnumForName(activationStr);
			
			if (activation == null || activation == RightClick.AIR || activation == RightClick.ENTITY) {
				continue;
			}
			
			final INBT commandsTag = cmdObject.get(NBTKeys.COMMANDS);
			if (!(commandsTag instanceof ListNBT)) continue;
			
			final ListNBT commandsList = (ListNBT) commandsTag;
			
			for (int j = 0; j < commandsList.size(); j++) {
				final String command = commandsList.getString(j);
				if (command == null) continue;
				
				server.getCommandManager().handleCommand(source, command);
				// System.out.println("BLOCK: HANDLING");
			}
		}
	}
}
