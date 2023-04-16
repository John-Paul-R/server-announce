package dev.jpcode.serverannounce.message;

import com.google.gson.JsonObject;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import dev.jpcode.serverannounce.MessageScheduler;

public class SingleMessage extends ScheduledMessage {
    private final Text message;
    private boolean isDone;

    public SingleMessage(String messageName, Text message, int tickPeriod) {
        super(messageName, tickPeriod);
        this.message = message;
    }

    @Override
    protected void onExec(MinecraftServer server) {
        this.isDone = true;
    }

    @Override
    protected Text nextMessage() {
        return message;
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public void writeJson(JsonObject root) {
        super.writeJson(root);
        root.add("message", serializeText(message));
    }

    public static SingleMessage readJson(JsonObject root) {
        var messagesProp = root.get("message");
        var message = Text.Serializer.fromJson(messagesProp);

        var tickPeriod = root.get("tickPeriod").getAsInt();
        var messageName = root.get("messageName").getAsString();
        return new SingleMessage(messageName, message, tickPeriod);
    }

    public static LiteralArgumentBuilder<ServerCommandSource> getCreateCommandBuilder() {
        return CommandManager.literal("single_message")
            .then(CommandManager.argument("message_name", StringArgumentType.word())
                .then(CommandManager.argument("delay_ticks", IntegerArgumentType.integer(1))
                    .then(CommandManager.argument("messaage_text", TextArgumentType.text())
                        .executes(context -> {
                            var message_name = StringArgumentType.getString(context, "message_name");
                            var delay_ticks = IntegerArgumentType.getInteger(context, "delay_ticks");
                            var messaage_text = TextArgumentType.getTextArgument(context, "messaage_text");

                            MessageScheduler.getInstance().scheduleMessage(
                                message_name,
                                new SingleMessage(message_name, messaage_text, delay_ticks));

                            return 1;
                        })
                    )
                )
            );
    }
}
