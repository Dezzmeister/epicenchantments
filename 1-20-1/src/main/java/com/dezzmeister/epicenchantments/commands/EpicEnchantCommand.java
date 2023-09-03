package com.dezzmeister.epicenchantments.commands;

import java.util.List;

import javax.annotation.Nullable;

import com.dezzmeister.epicenchantments.bindings.EpicEnchanter;
import com.dezzmeister.epicenchantments.bindings.ItemConsumedException;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class EpicEnchantCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("epicenchant").requires(p -> p.hasPermission(2))
            .then(Commands.argument("targets", EntityArgument.players())
                .then(Commands.literal("air")
                    .then(Commands.argument("radius", FloatArgumentType.floatArg())
                        .then(Commands.argument("commands", StringArgumentType.greedyString())
                            .executes(EpicEnchantCommand::epicEnchantAir)
                        )
                    )
                )
                .then(Commands.literal("entity")
                    .then(Commands.argument("asPlayer", BoolArgumentType.bool())
                        .then(Commands.argument("commands", StringArgumentType.greedyString())
                            .executes(EpicEnchantCommand::epicEnchantEntity)
                        )
                    )
                )
                .then(Commands.literal("block")
                    .then(Commands.argument("radius", FloatArgumentType.floatArg())
                        .then(Commands.argument("commands", StringArgumentType.greedyString())
                            .executes(EpicEnchantCommand::epicEnchantBlock)
                        )
                    )
                )
                .then(Commands.literal("all")
                    .then(Commands.argument("radius", FloatArgumentType.floatArg())
                        .then(Commands.argument("asPlayer", BoolArgumentType.bool())
                            .then(Commands.argument("commands", StringArgumentType.greedyString())
                                .executes(EpicEnchantCommand::epicEnchantAll)
                            )
                        )
                    )
                )
            )
        );
    }

    private static int epicEnchantAir(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final CommandSourceStack source = context.getSource();

        for (ServerPlayer target : EntityArgument.getPlayers(context, "targets")) {
            final EnchantableItem enchantableItem = getHeldItemStack(target);

            if (enchantableItem == null) {
                continue;
            }

            final String fullCommandString = StringArgumentType.getString(context, "commands");
            final List<String> commands = List.of(fullCommandString.split(";"));
            final float radius = FloatArgumentType.getFloat(context, "radius");

            try {
                final ItemStack enchantedItem = new EpicEnchanter(enchantableItem.item()).withRightItemBinding(radius, commands).enchant();
                final ServerPlayer player = source.getPlayerOrException();

                player.setItemSlot(enchantableItem.slot(), enchantedItem);
            } catch (ItemConsumedException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    private static int epicEnchantEntity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final CommandSourceStack source = context.getSource();

        for (ServerPlayer target : EntityArgument.getPlayers(context, "targets")) {
            final EnchantableItem enchantableItem = getHeldItemStack(target);

            if (enchantableItem == null) {
                continue;
            }

            final String fullCommandString = StringArgumentType.getString(context, "commands");
            final List<String> commands = List.of(fullCommandString.split(";"));
            final boolean asPlayer = BoolArgumentType.getBool(context, "asPlayer");

            try {
                final ItemStack enchantedItem = new EpicEnchanter(enchantableItem.item()).withRightEntityBinding(asPlayer, commands).enchant();
                final ServerPlayer player = source.getPlayerOrException();

                player.setItemSlot(enchantableItem.slot(), enchantedItem);
            } catch (ItemConsumedException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    private static int epicEnchantBlock(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final CommandSourceStack source = context.getSource();

        for (ServerPlayer target : EntityArgument.getPlayers(context, "targets")) {
            final EnchantableItem enchantableItem = getHeldItemStack(target);

            if (enchantableItem == null) {
                continue;
            }

            final String fullCommandString = StringArgumentType.getString(context, "commands");
            final List<String> commands = List.of(fullCommandString.split(";"));
            final float radius = FloatArgumentType.getFloat(context, "radius");

            try {
                final ItemStack enchantedItem = new EpicEnchanter(enchantableItem.item()).withRightBlockBinding(radius, commands).enchant();
                final ServerPlayer player = source.getPlayerOrException();

                player.setItemSlot(enchantableItem.slot(), enchantedItem);
            } catch (ItemConsumedException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    private static int epicEnchantAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final CommandSourceStack source = context.getSource();

        for (ServerPlayer target : EntityArgument.getPlayers(context, "targets")) {
            final EnchantableItem enchantableItem = getHeldItemStack(target);

            if (enchantableItem == null) {
                continue;
            }

            final String fullCommandString = StringArgumentType.getString(context, "commands");
            final List<String> commands = List.of(fullCommandString.split(";"));
            final float radius = FloatArgumentType.getFloat(context, "radius");
            final boolean asPlayer = BoolArgumentType.getBool(context, "asPlayer");

            try {
                final ItemStack enchantedItem = new EpicEnchanter(enchantableItem.item()).withRightAllBinding(radius, asPlayer, commands).enchant();
                final ServerPlayer player = source.getPlayerOrException();

                player.setItemSlot(enchantableItem.slot(), enchantedItem);
            } catch (ItemConsumedException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    private static @Nullable EnchantableItem getHeldItemStack(ServerPlayer player) {
        final ItemStack mainHandItem = player.getMainHandItem();

        if (mainHandItem != null) {
            return new EnchantableItem(mainHandItem, EquipmentSlot.MAINHAND);
        }

        final ItemStack offHandItem = player.getOffhandItem();

        if (offHandItem != null) {
            return new EnchantableItem(offHandItem, EquipmentSlot.OFFHAND);
        }

        return null;
    }

    private record EnchantableItem(ItemStack item, EquipmentSlot slot) {}
}
