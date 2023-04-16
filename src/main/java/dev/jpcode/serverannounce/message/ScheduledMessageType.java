package dev.jpcode.serverannounce.message;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.server.command.ServerCommandSource;

public enum ScheduledMessageType {
    PeriodicMessageGroup,
    PeriodicSingleMessage,
    SingleMessage,
    ;

    public static CompletableFuture<Suggestions> getSuggestions(
        CommandContext<ServerCommandSource> context,
        SuggestionsBuilder builder)
    {
        return builder
            .suggest(PeriodicMessageGroup.toString())
            .suggest(PeriodicSingleMessage.toString())
            .suggest(SingleMessage.toString())
            .buildFuture();
    }
}
