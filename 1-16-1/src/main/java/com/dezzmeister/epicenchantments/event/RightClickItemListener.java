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
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RightClickItemListener {
	private final AtomicBoolean shouldHandle;
	
	public RightClickItemListener(final AtomicBoolean _shouldHandle) {
		shouldHandle = _shouldHandle;
	}

	@SubscribeEvent
	public void receive(final PlayerInteractEvent.RightClickItem event) {
		if (event.getWorld().isRemote()) {
			return;	// Don't do anything on client-side
		}
		
		// System.out.println("ITEM EVENT, HANDLED: " + RightClickBlockListener.handled);
		
		final PlayerEntity actor = event.getPlayer();
		final ServerWorld world = (ServerWorld) event.getWorld();
		final MinecraftServer server = world.getServer();
		
		if (event.getHand() == Hand.OFF_HAND) {
			return;
		}
		/*
		final float armRadius = 5;
		final Vector3d endVec1 = actor.getPositionVec().add(actor.getLookVec().scale(armRadius));
		final RayTraceContext context1 = new RayTraceContext(actor.getPositionVec(), endVec1, BlockMode.VISUAL, FluidMode.NONE, actor);
		final BlockRayTraceResult rayTraceResult1 = world.rayTraceBlocks(context1);
		final boolean isMiss1 = (rayTraceResult1.getType() == RayTraceResult.Type.MISS) || (rayTraceResult1.getType() == RayTraceResult.Type.ENTITY);
		
		if (isMiss1) {
			System.out.println("CONTINUE RUNNING");
		} else {
			System.out.println("STOP RUNNING");
			return;
		}
		*/
		final ItemStack itemStackInUse = actor.getHeldItem(actor.getActiveHand());
		final Item itemInUse = itemStackInUse.getItem();
		
		if (itemInUse.equals(Items.AIR)) {
			return;
		}
		
		if (shouldHandle.get()) {
			shouldHandle.set(false);
			return;
		}
		
		final CompoundNBT tag = itemStackInUse.getTag();
		final INBT bindingsTag = (tag == null) ? null : tag.get("RightBindings");
		
		if (!(bindingsTag instanceof ListNBT)) {
			return;
		}		
		
		final ListNBT bindingsList = (ListNBT) bindingsTag;
		
		for (int i = 0; i < bindingsList.size(); i++) {
			final CompoundNBT cmdObject = bindingsList.getCompound(i);
			if (cmdObject == null) continue;
			
			final String activationStr = cmdObject.getString(NBTKeys.ACTIVATION);
			final RightClick activation = RightClick.getEnumForName(activationStr);
			
			if (activation == null || activation == RightClick.ENTITY) continue;
			
			final float radius = cmdObject.getFloat(NBTKeys.RADIUS);
			if (radius == 0) continue;
			
			final Vector3d endVec = actor.getPositionVec().add(actor.getLookVec().scale(radius));
			final RayTraceContext context = new RayTraceContext(actor.getPositionVec(), endVec, BlockMode.VISUAL, FluidMode.NONE, actor);
			final BlockRayTraceResult rayTraceResult = world.rayTraceBlocks(context);
			final boolean isMiss = (rayTraceResult.getType() == RayTraceResult.Type.MISS) || (rayTraceResult.getType() == RayTraceResult.Type.ENTITY);
			// System.out.println("ISMISS: " + isMiss);
			
			if ((activation == RightClick.AIR && !isMiss) || (activation == RightClick.BLOCK && isMiss)) continue;
			
			final CommandSource source;
			final Vector3d target = rayTraceResult.getHitVec();
			
			if (isMiss) {
				source = actor.getCommandSource().withPermissionLevel(4).withWorld((ServerWorld) event.getWorld());
			} else {
				source = actor.getCommandSource().withPermissionLevel(4).withWorld((ServerWorld) event.getWorld()).withPos(target);
			}
			
			final INBT commandsTag = cmdObject.get(NBTKeys.COMMANDS);
			if (!(commandsTag instanceof ListNBT)) continue;
			
			final ListNBT commandsList = (ListNBT) commandsTag;
			
			for (int j = 0; j < commandsList.size(); j++) {
				final String command = commandsList.getString(j);
				if (command == null) continue;
				
				server.getCommandManager().handleCommand(source, command);
				// System.out.println("ITEM: HANDLING COMMAND");
			}
		}
	}
}
