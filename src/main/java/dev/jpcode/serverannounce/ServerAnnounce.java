package dev.jpcode.serverannounce;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;

public class ServerAnnounce implements ModInitializer
{
    public static final Logger LOGGER = LogManager.getLogger("serverannounce");

    @Override
    public void onInitialize()
    {
        LOGGER.info("Server Announce is getting ready...");
    }
}
