package com.dezzmeister.epicenchantments.event;

import com.dezzmeister.epicenchantments.bindings.Activation.RightClick;
import com.dezzmeister.epicenchantments.bindings.NBTKeys;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
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
		if (event.getLevel().isClientSide()) {
			return;	// Don't do anything on client-side
		}
		
		final Player actor = event.getEntity();
		final Entity target = event.getTarget();
		final MinecraftServer server = event.getLevel().getServer();

		if (server == null) {
			return;
		}
		
		final InteractionHand hand = event.getHand();
		
		final ItemStack itemStackInUse = 
			hand == InteractionHand.MAIN_HAND ? actor.getMainHandItem() : actor.getOffhandItem();
		final Item itemInUse = itemStackInUse.getItem();
		
		if (itemInUse.equals(Items.AIR)) {
			return;
		}
		
		final CompoundTag tag = itemStackInUse.getTag();
		
		final Tag bindingsTag = (tag == null) ? null : tag.get(NBTKeys.RIGHT_BINDINGS);
		
		if (!(bindingsTag instanceof ListTag)) {
			return;
		}
		
		final ListTag bindingsList = (ListTag) bindingsTag;
		final int numBindings = bindingsList.size();
		
		for (int i = 0; i < numBindings; i++) {
			final CompoundTag cmdObject = bindingsList.getCompound(i);
			if (cmdObject == null) continue;
			
			final String activationStr = cmdObject.getString(NBTKeys.ACTIVATION);
			final RightClick activation = RightClick.getEnumForName(activationStr);
			
			if (activation == null || activation == RightClick.AIR || activation == RightClick.BLOCK) {
				continue;
			}
			
			final boolean asPlayer = cmdObject.getBoolean(NBTKeys.AS_PLAYER);
			final CommandSourceStack cmdSource = asPlayer ? actor.createCommandSourceStack() : target.createCommandSourceStack();
			final CommandSourceStack source = cmdSource.withPermission(4).withLevel((ServerLevel)target.level()).withSuppressedOutput();
			
			final Tag commandsTag = cmdObject.get(NBTKeys.COMMANDS);
			if (!(commandsTag instanceof ListTag)) continue;
			
			final ListTag commandsList = (ListTag) commandsTag;
			
			for (int j = 0; j < commandsList.size(); j++) {
				final String command = commandsList.getString(j);
				if (command == null) continue;				
				
				server.getCommands().performPrefixedCommand(source, command);
			}
		}
	}
}
