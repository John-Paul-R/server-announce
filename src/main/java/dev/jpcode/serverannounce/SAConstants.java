package dev.jpcode.serverannounce;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;

public final class SAConstants {
    private SAConstants() {
        //not called
    }

    public record ModVersion(Version version, int dataVersion) {}

    private static ModVersion modVersion;
    private static ModMetadata modMetadata;

    public static void init() {
        modMetadata = FabricLoader.getInstance().getModContainer("serverannounce").get().getMetadata();
        CustomValue dataVersion = modMetadata.getCustomValue("dataVersion");
        modVersion = new ModVersion(
            modMetadata.getVersion(),
            dataVersion != null ? dataVersion.getAsNumber().intValue() : 0
        );
    }

    public static ModMetadata getModMetadata() {
        return modMetadata;
    }

    public static ModVersion getModVersion() {
        return modVersion;
    }
}
