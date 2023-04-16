package dev.jpcode.serverannounce.message;

import com.google.gson.JsonObject;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

public class SingleMessage extends ScheduledMessage {
    private final Text message;
    private boolean isDone;

    public SingleMessage(Text message, int tickPeriod) {
        super(tickPeriod);
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

    public static PeriodicScheduledMessage readJson(JsonObject root) {
        var messagesProp = root.get("message");
        var message = Text.Serializer.fromJson(messagesProp);

        var tickPeriod = root.get("tickPeriod").getAsInt();
        return new PeriodicSingleMessage(message, tickPeriod);
    }
}
