package com.dezzmeister.epicenchantments.event;

import java.util.concurrent.atomic.AtomicBoolean;

import com.dezzmeister.epicenchantments.bindings.Activation.RightClick;
import com.dezzmeister.epicenchantments.bindings.NBTKeys;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RightClickBlockListener {
	private final AtomicBoolean handled;
	
	public RightClickBlockListener(final AtomicBoolean _handled) {
		handled = _handled;
	}
	
	@SubscribeEvent
	public void receive(final PlayerInteractEvent.RightClickBlock event) {
		if (event.getLevel().isClientSide()) {
			return;	// Don't do anything on client-side
		}
		
		final Player actor = event.getEntity();
		final BlockPos target = event.getPos();
		final MinecraftServer server = event.getLevel().getServer();

		if (server == null) {
			return;
		}
		
		final InteractionHand hand = event.getHand();
		handled.set(true);
		
		final ItemStack itemStackInUse = 
			hand == InteractionHand.MAIN_HAND ? actor.getMainHandItem() : actor.getOffhandItem();
		final Item itemInUse = itemStackInUse.getItem();
		
		if (itemInUse.equals(Items.AIR)) {
			return;
		}
		
		final CompoundTag tag = itemStackInUse.getTag();
		final Tag bindingsTag = (tag == null) ? null : tag.get("RightBindings");
		
		if (!(bindingsTag instanceof ListTag)) {
			return;
		}

		final CommandSourceStack source = actor
			.createCommandSourceStack()
			.withSuppressedOutput()
			.withPermission(4)
			.withLevel((ServerLevel)event.getLevel())
			.withPosition(new Vec3(target.getX() + 0.5, target.getY(), target.getZ() + 0.5));
		
		final ListTag bindingsList = (ListTag) bindingsTag;
		
		for (int i = 0; i < bindingsList.size(); i++) {
			final CompoundTag cmdObject = bindingsList.getCompound(i);
			if (cmdObject == null) continue;
			
			final String activationStr = cmdObject.getString(NBTKeys.ACTIVATION);
			final RightClick activation = RightClick.getEnumForName(activationStr);
			
			if (activation != RightClick.ALL && activation != RightClick.BLOCK) {
				continue;
			}
			
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
