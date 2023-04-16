package dev.jpcode.serverannounce;

import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

import dev.jpcode.serverannounce.message.PeriodicMessageGroup;
import dev.jpcode.serverannounce.message.PeriodicSingleMessage;
import dev.jpcode.serverannounce.message.SingleMessage;

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
                .then(CommandManager.literal("create")
                    .then(PeriodicMessageGroup.getCreateCommandBuilder())
                    .then(PeriodicSingleMessage.getCreateCommandBuilder())
                    .then(SingleMessage.getCreateCommandBuilder())
                )
                .then(CommandManager.literal("edit")
                    .then(PeriodicMessageGroup.getEditCommandBuilder())
//                    .then(PeriodicSingleMessage.getEditCommandBuilder())
//                    .then(SingleMessage.getEditCommandBuilder())
                )
                .then(CommandManager.literal("delete")
                    .then(CommandManager.argument("message_name", StringArgumentType.word())
                        .suggests((ctx, suggestionsBuilder) -> MessageScheduler.getInstance()
                            .streamScheduledMessagesEntries()
                            .map(Map.Entry::getKey)
                            .collect(ScCollectors.toSuggestionsProvider(ctx, suggestionsBuilder)))
                        .executes(context -> {
                            var messageName = StringArgumentType.getString(context, "message_name");
                            var deletedNode = MessageScheduler.getInstance().deleteScheduledMessage(messageName);
                            if (deletedNode == null) {
                                context.getSource().sendError(Text.of("No scheduled message with name '%s' exists.".formatted(messageName)));
                            } else {
                                context.getSource().sendFeedback(Text.of("Deleted scheduled message '%s'".formatted(messageName)), true);
                            }
                            return 1;
                        })
                    ))
            );
        });

    }
}
