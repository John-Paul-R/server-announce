package dev.jpcode.serverannounce.message;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.server.command.ServerCommandSource;

public interface CommandBuildableMessage {
    LiteralArgumentBuilder<ServerCommandSource> getCreateCommandBuilder();

    LiteralArgumentBuilder<ServerCommandSource> getEditCommandBuilder();
}
