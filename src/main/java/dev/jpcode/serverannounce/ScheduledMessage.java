package dev.jpcode.serverannounce;

import java.lang.reflect.Type;

import com.google.gson.*;

import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.LowercaseEnumTypeAdapterFactory;
import net.minecraft.util.Util;

public class ScheduledMessage {

    private final Text text;
    private final int tickPeriod;
    private final boolean isPeriodic;
    private int endTick;
    private boolean isDone;
    // TODO option to send message to individual player(s)

    public ScheduledMessage(Text text, int tickPeriod, boolean isPeriodic) {
        this.text = text;
        this.tickPeriod = tickPeriod;
        this.isPeriodic = isPeriodic;
        endTick = -1;
        this.isDone = false;
    }

    public ScheduledMessage(Text text, int currentTicks, int tickPeriod, boolean isPeriodic) {
        this.text = text;
        this.tickPeriod = tickPeriod;
        this.isPeriodic = isPeriodic;
        schedule(currentTicks);
        this.isDone = false;
    }

    public Text getText() {
        return text;
    }

    public int getTickPeriod() {
        return tickPeriod;
    }

    public boolean isPeriodic() {
        return isPeriodic;
    }

    private void schedule(int currentTicks) {
        this.endTick = currentTicks + tickPeriod;
    }

    public void tick(MinecraftServer server) {
        int currentTicks = server.getTicks();
        if (this.endTick <= currentTicks) {
            if (this.endTick == -1) {
                // This can occur if the ScheduledMessage was just created without access to the current ticks;
                schedule(currentTicks);
            }
            server.getPlayerManager().broadcastChatMessage(text, MessageType.SYSTEM, Util.NIL_UUID);
            if (this.isPeriodic) {
                schedule(currentTicks);
            } else {
                this.isDone = true;
            }
        }
    }

    public boolean isDone() {
        return this.isDone;
    }

    public static class Serializer implements JsonDeserializer<ScheduledMessage>, JsonSerializer<ScheduledMessage> {
        private static final Gson GSON = (Gson)Util.make(() -> {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.disableHtmlEscaping();
            gsonBuilder.registerTypeHierarchyAdapter(Text.class, new Text.Serializer());
            gsonBuilder.registerTypeHierarchyAdapter(Style.class, new net.minecraft.text.Style.Serializer());
            gsonBuilder.registerTypeAdapterFactory(new LowercaseEnumTypeAdapterFactory());
            return gsonBuilder.create();
        });

        public JsonElement serialize(ScheduledMessage src) {
            return serialize(src, null, null);
        }

        @Override
        public JsonElement serialize(ScheduledMessage src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("message", Text.Serializer.toJsonTree(src.getText()));
            jsonObject.addProperty("tickPeriod", src.getTickPeriod());
            jsonObject.addProperty("isPeriodic", src.isPeriodic());
            return jsonObject;
        }

        public ScheduledMessage deserialize(JsonElement jsonElement) {
            return deserialize(jsonElement, null, null);
        }

        @Override
        public ScheduledMessage deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (jsonElement.isJsonPrimitive()) {
                throw new JsonParseException("Don't know how to turn the Primitive " + jsonElement + " into a ScheduledMessage.");
            } else if (!jsonElement.isJsonObject()) {
                throw new JsonParseException("Don't know how to turn the JsonElement " + jsonElement + " into a ScheduledMessage.");
            } else {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                /*
                 * Obj
                 *  - text (Text)
                 *  - tickPeriod (int ticks)
                 *  - isPeriodic (boolean)
                 */
                return new ScheduledMessage(
                    Text.Serializer.fromJson(jsonObject.get("message")),
                    jsonObject.get("tickPeriod").getAsInt(),
                    jsonObject.get("isPeriodic").getAsBoolean()
                );
            }

        }
    }
}
