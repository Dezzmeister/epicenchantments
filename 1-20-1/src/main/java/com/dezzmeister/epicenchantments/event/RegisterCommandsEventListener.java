package com.dezzmeister.epicenchantments.event;

import com.dezzmeister.epicenchantments.commands.EpicEnchantCommand;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RegisterCommandsEventListener {

    @SubscribeEvent
    public void handleRegisterCommandsEvent(RegisterCommandsEvent event) {
        final CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        EpicEnchantCommand.register(dispatcher);
    }
}
