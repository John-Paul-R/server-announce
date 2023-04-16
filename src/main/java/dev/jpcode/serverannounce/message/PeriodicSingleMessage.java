package dev.jpcode.serverannounce.message;

import java.util.Map;

import com.google.gson.JsonObject;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import dev.jpcode.serverannounce.MessageScheduler;
import dev.jpcode.serverannounce.ScCollectors;

public class PeriodicSingleMessage extends PeriodicScheduledMessage {
    private final Text message;

    public PeriodicSingleMessage(String messageName, Text message, int tickPeriod) {
        super(messageName, tickPeriod);
        this.message = message;
    }

    @java.lang.Override
    protected Text nextMessage() {
        return message;
    }

    @Override
    public void writeJson(JsonObject root) {
        super.writeJson(root);
        root.add("message", serializeText(message));
    }

    public static PeriodicScheduledMessage readJson(JsonObject root) {
        var messagesProp = root.get("message");
        var message = Text.Serializer.fromJson(messagesProp);

        var tickPeriod = root.get("tickPeriod").getAsInt();
        var messageName = root.get("messageName").getAsString();
        return new PeriodicSingleMessage(messageName, message, tickPeriod);
    }

    public static LiteralArgumentBuilder<ServerCommandSource> getEditCommandBuilder() {
        return CommandManager.literal("periodic_single_message")
            .then(CommandManager.argument("message_name", StringArgumentType.word())
                .suggests((ctx, suggestionsBuilder) -> MessageScheduler.getInstance()
                    .streamScheduledMessagesEntries()
                    .filter(entry -> entry.getValue() instanceof PeriodicSingleMessage)
                    .map(Map.Entry::getKey)
                    .collect(ScCollectors.toSuggestionsProvider(ctx, suggestionsBuilder)))
                .then(CommandManager.argument("period_ticks", IntegerArgumentType.integer(1))
                    .then(CommandManager.argument("messaage_text", TextArgumentType.text()))
                )
            );
    }

    public static LiteralArgumentBuilder<ServerCommandSource> getCreateCommandBuilder() {
        return CommandManager.literal("periodic_single_message")
            .then(CommandManager.argument("message_name", StringArgumentType.word())
                .then(CommandManager.argument("period_ticks", IntegerArgumentType.integer(1))
                    .then(CommandManager.argument("messaage_text", TextArgumentType.text())
                        .executes(context -> {
                            var message_name = StringArgumentType.getString(context, "message_name");
                            var period_ticks = IntegerArgumentType.getInteger(context, "period_ticks");
                            var messaage_text = TextArgumentType.getTextArgument(context, "messaage_text");

                            MessageScheduler.getInstance().scheduleMessage(
                                message_name,
                                new PeriodicSingleMessage(message_name, messaage_text, period_ticks));

                            return 1;
                        })
                    )
                )
            );
    }
}
