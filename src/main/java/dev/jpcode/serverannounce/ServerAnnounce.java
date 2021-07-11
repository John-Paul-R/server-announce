package dev.jpcode.serverannounce;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class ServerAnnounce implements ModInitializer
{
    public static final Logger LOGGER = LogManager.getLogger("serverannounce");

    @Override
    public void onInitialize()
    {
        LOGGER.info("Server Announce starting...");
        SAConstants.init();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            MessageScheduler scheduler = new MessageScheduler();
            scheduler.load();
            scheduler.save();
        });

        ServerAnnounceCommandRegistry.register();

        LOGGER.info("Server Announce started.");
    }
}
