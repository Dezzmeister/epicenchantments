package com.dezzmeister.epicenchantments.event;

import java.util.concurrent.atomic.AtomicBoolean;

import com.dezzmeister.epicenchantments.bindings.Activation.RightClick;
import com.dezzmeister.epicenchantments.bindings.NBTKeys;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RightClickItemListener {
	private final AtomicBoolean handled;
	
	public RightClickItemListener(final AtomicBoolean _handled) {
		handled = _handled;
	}

	@SubscribeEvent
	public void receive(final PlayerInteractEvent.RightClickItem event) {
		if (event.getLevel().isClientSide()) {
			return;	// Don't do anything on client-side
		}
		
		final Player actor = event.getEntity();
		final ServerLevel world = (ServerLevel) event.getLevel();
		final MinecraftServer server = world.getServer();
		
		final InteractionHand hand = event.getHand();
		
		final ItemStack itemStackInUse = 
			hand == InteractionHand.MAIN_HAND ? actor.getMainHandItem() : actor.getOffhandItem();
		final Item itemInUse = itemStackInUse.getItem();
		
		if (itemInUse.equals(Items.AIR)) {
			return;
		}
		
		if (handled.get()) {
			handled.set(false);
			return;
		}
		
		final CompoundTag tag = itemStackInUse.getTag();
		final Tag bindingsTag = (tag == null) ? null : tag.get("RightBindings");
		
		if (!(bindingsTag instanceof ListTag)) {
			return;
		}		
		
		final ListTag bindingsList = (ListTag) bindingsTag;
		
		for (int i = 0; i < bindingsList.size(); i++) {
			final CompoundTag cmdObject = bindingsList.getCompound(i);
			if (cmdObject == null) continue;
			
			final String activationStr = cmdObject.getString(NBTKeys.ACTIVATION);
			final RightClick activation = RightClick.getEnumForName(activationStr);
			
			if (activation == null || activation == RightClick.ENTITY) continue;
			
			final float radius = cmdObject.getFloat(NBTKeys.RADIUS);
			if (radius == 0) continue;

			final Vec3 endVec = actor.getPosition(1.0f).add(actor.getLookAngle().scale(radius));
			final ClipContext context = new ClipContext(actor.getPosition(1.0f), endVec, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, actor);
			final BlockHitResult clipResult = world.clip(context);
			final boolean isMiss = (clipResult.getType() == BlockHitResult.Type.MISS);

			if ((activation == RightClick.AIR && !isMiss) || (activation == RightClick.BLOCK && isMiss)) continue;

			if (clipResult.getType() == BlockHitResult.Type.ENTITY) {
				// Impossible - this method never returns Type.ENTITY
				continue;
			}
			
			final CommandSourceStack source;
			final Vec3 target = clipResult.getLocation();
			
			if (isMiss) {
				source = actor.createCommandSourceStack().withSuppressedOutput().withPermission(4).withLevel((ServerLevel) event.getLevel());
			} else {
				source = actor.createCommandSourceStack().withSuppressedOutput().withPermission(4).withLevel((ServerLevel) event.getLevel()).withPosition(target);
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
