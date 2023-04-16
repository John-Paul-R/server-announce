package dev.jpcode.serverannounce.message;

import java.util.Arrays;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.text.Text;

public class PeriodicMessageGroup extends PeriodicScheduledMessage {
    private final Text[] messages;
    private int messageIdx = -1;

    public PeriodicMessageGroup(Text[] messages, int tickPeriod) {
        super(tickPeriod);
        this.messages = messages;
    }

    private int nextIdx() {
        return messageIdx = (messageIdx + 1) % messages.length;
    }

    @java.lang.Override
    protected Text nextMessage() {
        return messages[nextIdx()];
    }

    @Override
    public void writeJson(JsonObject root) {
        super.writeJson(root);
        JsonArray arr = new JsonArray();
        Arrays.stream(messages).map(this::serializeText).forEach(arr::add);
        root.add("messages", arr);
    }

    public static PeriodicScheduledMessage readJson(JsonObject root) {
        var messagesProp = root.get("messages").getAsJsonArray();
        var messages = new Text[messagesProp.size()];
        int i = 0;
        for (JsonElement jsonElement : messagesProp) {
            messages[i] = Text.Serializer.fromJson(jsonElement);
        }

        var tickPeriod = root.get("tickPeriod").getAsInt();
        return new PeriodicMessageGroup(messages, tickPeriod);
    }
}
