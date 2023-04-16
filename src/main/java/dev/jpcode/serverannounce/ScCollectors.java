package dev.jpcode.serverannounce;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collector;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.server.command.ServerCommandSource;

public final class ScCollectors {
    private ScCollectors() {}

    public static Collector<String, SuggestionsBuilder, CompletableFuture<Suggestions>> toSuggestionsProvider(
        final CommandContext<ServerCommandSource> ctx,
        final SuggestionsBuilder suggestionsBuilder)
    {
        return Collector.of(
            () -> new SuggestionsBuilder(suggestionsBuilder.getInput(), suggestionsBuilder.getStart()),
            SuggestionsBuilder::suggest,
            SuggestionsBuilder::add,
            SuggestionsBuilder::buildFuture);
    }

}
