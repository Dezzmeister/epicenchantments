package com.dezzmeister.epicenchantments;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dezzmeister.epicenchantments.event.RegisterCommandsEventListener;
import com.dezzmeister.epicenchantments.event.RightClickBlockListener;
import com.dezzmeister.epicenchantments.event.RightClickEntityListener;
import com.dezzmeister.epicenchantments.event.RightClickItemListener;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod("epicenchantments")
public class EpicEnchantments {
    public static final Logger LOGGER = LogManager.getLogger();

    public EpicEnchantments() {
        
        final AtomicBoolean handledFlag = new AtomicBoolean(false);

        MinecraftForge.EVENT_BUS.register(new RegisterCommandsEventListener());
        MinecraftForge.EVENT_BUS.register(new RightClickItemListener(handledFlag));
        MinecraftForge.EVENT_BUS.register(new RightClickEntityListener());
        MinecraftForge.EVENT_BUS.register(new RightClickBlockListener(handledFlag));
    }
}
