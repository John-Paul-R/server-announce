package dev.jpcode.serverannounce;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public final class ServerAnnounceCommandRegistry {

    private ServerAnnounceCommandRegistry() {
        //not called
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) -> {
            String resPrefix = "[ServerAnnounce] ";
            dispatcher.register(CommandManager.literal("serverannounce")
                .requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("reload")
                    .executes(context -> {
                        MessageScheduler.getInstance().load();
                        context.getSource().sendFeedback(
                            new LiteralText(resPrefix.concat("MessageScheduler reloaded from disk")), true
                        );
                        return 1;
                    }))
                .then(CommandManager.literal("save")
                    .executes(context -> {
                        MessageScheduler.getInstance().save();
                        context.getSource().sendFeedback(
                            new LiteralText(resPrefix.concat("MessageScheduler state saved to disk")), true
                        );
                        return 1;

                    }))
                .then(CommandManager.literal("createExampleMessage")
                    .executes(context -> {
                        MessageScheduler.getInstance().initExampleMessage();
                        context.getSource().sendFeedback(
                            new LiteralText(resPrefix.concat("Created an example announcement message.")), true
                        );
                        return 1;
                    }))
                .then(CommandManager.literal("executeAll")
                    .executes(context -> {
                        MessageScheduler.getInstance().streamScheduledMessages()
                            .forEach(m -> m.exec(context.getSource().getServer()));

                        context.getSource().sendFeedback(
                            new LiteralText(resPrefix.concat("Executed all scheduled messages.")), true
                        );

                        return 1;
                    }))
            );
        });

    }
}
