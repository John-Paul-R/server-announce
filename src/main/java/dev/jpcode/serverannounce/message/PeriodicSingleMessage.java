package dev.jpcode.serverannounce.message;

import com.google.gson.JsonObject;

import net.minecraft.text.Text;

public class PeriodicSingleMessage extends PeriodicScheduledMessage {
    private final Text message;

    public PeriodicSingleMessage(Text message, int tickPeriod) {
        super(tickPeriod);
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
        return new PeriodicSingleMessage(message, tickPeriod);
    }
}
