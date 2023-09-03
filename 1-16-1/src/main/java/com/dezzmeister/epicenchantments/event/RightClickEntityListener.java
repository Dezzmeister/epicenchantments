package com.dezzmeister.epicenchantments.event;

import com.dezzmeister.epicenchantments.bindings.Activation.RightClick;
import com.dezzmeister.epicenchantments.bindings.NBTKeys;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Hand;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * The player has right clicked on an entity within reach distance. Commands should only be executed if the activation
 * is "all" or "entity".
 * 
 * @author Joe Desmond
 */
public class RightClickEntityListener {
	
	@SubscribeEvent
	public void receive(final PlayerInteractEvent.EntityInteract event) {
		if (event.getWorld().isRemote()) {
			return;	// Don't do anything on client-side
		}
		
		final LivingEntity actor = event.getEntityLiving();
		final Entity target = event.getTarget();
		final MinecraftServer server = event.getWorld().getServer();
		
		if (event.getHand() == Hand.OFF_HAND) {
			return;
		}
		
		final ItemStack itemStackInUse = actor.getHeldItem(actor.getActiveHand());
		final Item itemInUse = itemStackInUse.getItem();
		
		if (itemInUse.equals(Items.AIR)) {
			return;
		}
		
		final CompoundNBT tag = itemStackInUse.getTag();
		
		final INBT bindingsTag = (tag == null) ? null : tag.get(NBTKeys.RIGHT_BINDINGS);
		
		if (!(bindingsTag instanceof ListNBT)) {
			return;
		}
		
		final ListNBT bindingsList = (ListNBT) bindingsTag;
		final int numBindings = bindingsList.size();
		
		for (int i = 0; i < numBindings; i++) {
			final CompoundNBT cmdObject = bindingsList.getCompound(i);
			if (cmdObject == null) continue;
			
			final String activationStr = cmdObject.getString(NBTKeys.ACTIVATION);
			final RightClick activation = RightClick.getEnumForName(activationStr);
			
			if (activation == null || activation == RightClick.AIR || activation == RightClick.BLOCK) {
				continue;
			}
			
			final boolean asPlayer = cmdObject.getBoolean(NBTKeys.AS_PLAYER);
			final CommandSource cmdSource = asPlayer ? actor.getCommandSource() : target.getCommandSource();
			final CommandSource source = cmdSource.withPermissionLevel(4).withWorld((ServerWorld)target.getEntityWorld());
			
			final INBT commandsTag = cmdObject.get(NBTKeys.COMMANDS);
			if (!(commandsTag instanceof ListNBT)) continue;
			
			final ListNBT commandsList = (ListNBT) commandsTag;
			
			for (int j = 0; j < commandsList.size(); j++) {
				final String command = commandsList.getString(j);
				if (command == null) continue;				
				
				server.getCommandManager().handleCommand(source, command);
			}
		}
	}
}
