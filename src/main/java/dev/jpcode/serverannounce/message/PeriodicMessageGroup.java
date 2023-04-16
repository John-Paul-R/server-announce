package dev.jpcode.serverannounce.message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import dev.jpcode.serverannounce.MessageScheduler;
import dev.jpcode.serverannounce.ScCollectors;

public class PeriodicMessageGroup extends PeriodicScheduledMessage {
    private final HashMap<String, Text> messagesMap;
    private List<Text> messagesList;
    private int messageIdx = -1;


    public PeriodicMessageGroup(String messageGroupName, int tickPeriod) {
        super(messageGroupName, tickPeriod);
        this.messagesMap = new HashMap<>();
        updateMessagesList();
    }

    public PeriodicMessageGroup(String messageGroupName, Map<String, Text> messages, int tickPeriod) {
        super(messageGroupName, tickPeriod);
        this.messagesMap = new HashMap<>(messages);
        updateMessagesList();
    }

    private void updateMessagesList() {
        this.messagesList = this.messagesMap.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(Map.Entry::getValue)
            .toList();
    }

    private int nextIdx() {
        return messageIdx = (messageIdx + 1) % messagesMap.size();
    }

    @java.lang.Override
    protected Text nextMessage() {
        return messagesList.size() == 0
            ? Text.of("[ServerAnnounce]: No messages configured for message group '%s'".formatted(super.messageName))
            : messagesList.get(nextIdx());
    }

    @Override
    public void writeJson(JsonObject root) {
        super.writeJson(root);
        JsonObject messagesObj = new JsonObject();
        messagesMap.forEach((messageName, messageText) -> messagesObj.add(messageName, serializeText(messageText)));
        root.add("messages", messagesObj);
    }

    public static PeriodicScheduledMessage readJson(JsonObject root) {
        var messagesProp = root.get("messages").getAsJsonObject();
        var messages = new HashMap<String, Text>();
        for (Map.Entry<String, JsonElement> entry : messagesProp.entrySet()) {
            messages.put(entry.getKey(), Text.Serializer.fromJson(entry.getValue()));
        }

        var tickPeriod = root.get("tickPeriod").getAsInt();
        var messageName = root.get("messageName").getAsString();
        return new PeriodicMessageGroup(messageName, messages, tickPeriod);
    }

    public static LiteralArgumentBuilder<ServerCommandSource> getEditCommandBuilder() {
        return CommandManager.literal("periodic_message_group")
            .then(CommandManager.argument("message_group_name", StringArgumentType.word())
                .suggests((ctx, suggestionsBuilder) -> MessageScheduler.getInstance()
                    .streamScheduledMessagesEntries()
                    .filter(entry -> entry.getValue() instanceof PeriodicMessageGroup)
                    .map(Map.Entry::getKey)
                    .collect(ScCollectors.toSuggestionsProvider(ctx, suggestionsBuilder)))
                .then(CommandManager.literal("addMessage")
                    .then(CommandManager.argument("message_name", StringArgumentType.word())
                        .then(CommandManager.argument("messaage_text", TextArgumentType.text())
                            .executes(context -> {
                                var message_group_name = StringArgumentType.getString(context, "message_group_name");
                                var message_name = StringArgumentType.getString(context, "message_name");
                                var messaage_text = TextArgumentType.getTextArgument(context, "messaage_text");

                                var messageScheduler = MessageScheduler.getInstance();
                                var message = messageScheduler.getScheduledMessage(message_group_name);

                                if (!(message instanceof PeriodicMessageGroup editableMessage)) {
                                    context.getSource().sendError(new LiteralText("Message '%s' is not a PeriodicMessageGroup".formatted(message_name)));
                                    return 2;
                                }

                                editableMessage.messagesMap.put(message_name, messaage_text);
                                editableMessage.updateMessagesList();

                                messageScheduler.save();


                                return 1;

                            }))))
            );
    }

    public static LiteralArgumentBuilder<ServerCommandSource> getCreateCommandBuilder() {
        return CommandManager.literal("periodic_message_group")
            .then(CommandManager.argument("message_group_name", StringArgumentType.word())
                .then(CommandManager.argument("period_ticks", IntegerArgumentType.integer(1))
                    .executes(context -> {
                        var message_group_name = StringArgumentType.getString(context, "message_group_name");
                        var period_ticks = IntegerArgumentType.getInteger(context, "period_ticks");

                        MessageScheduler.getInstance().scheduleMessage(
                            message_group_name,
                            new PeriodicMessageGroup(message_group_name, period_ticks));

                        return 1;
                    })
                )
            );
    }
}
