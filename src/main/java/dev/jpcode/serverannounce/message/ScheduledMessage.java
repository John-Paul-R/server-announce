package dev.jpcode.serverannounce.message;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.*;
import org.apache.commons.lang3.NotImplementedException;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.LowercaseEnumTypeAdapterFactory;
import net.minecraft.util.Util;

import dev.jpcode.serverannounce.ServerAnnounce;

public abstract class ScheduledMessage implements JsonSerializable {
    protected final String messageName;
    protected final int tickPeriod;
    protected int endTick;

    public ScheduledMessage(String messageName, int tickPeriod) {
        this.messageName = messageName;
        this.tickPeriod = tickPeriod;
        endTick = -1;
    }

    public void tick(MinecraftServer server) {
        int currentTicks = server.getTicks();
        if (this.endTick == -1) {
            // This can occur if the AScheduledMessage was just created without access to the current ticks;
            schedule(currentTicks);
            return;
        }
        if (this.endTick <= currentTicks) {
            exec(server);
        }
    }

    public void exec(MinecraftServer server) {
        onExec(server);
        server.getPlayerManager().broadcast(nextMessage(), false);
    }

    public void writeJson(JsonObject root) {
        root.addProperty("tickPeriod", tickPeriod);
    }

    protected abstract void onExec(MinecraftServer server);

    protected abstract Text nextMessage();

    protected void schedule(int currentTicks) {
        this.endTick = currentTicks + tickPeriod;
    }

    public abstract boolean isDone();

    public int getTickPeriod() {
        return tickPeriod;
    }

    protected JsonElement serializeText(Text text) {
        return Text.Serializer.toJsonTree(text);
    }

    // --- Serializer ---
    public static class Serializer implements JsonDeserializer<ScheduledMessage>, JsonSerializer<ScheduledMessage> {
        private static final Gson GSON = Util.make(() -> {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.disableHtmlEscaping();
            gsonBuilder.registerTypeHierarchyAdapter(Text.class, new Text.Serializer());
            gsonBuilder.registerTypeHierarchyAdapter(Style.class, new net.minecraft.text.Style.Serializer());
            gsonBuilder.registerTypeAdapterFactory(new LowercaseEnumTypeAdapterFactory());
            return gsonBuilder.create();
        });

        private ScheduledMessageType getTypeCode(ScheduledMessage scheduledMessage) {
            if (scheduledMessage instanceof PeriodicMessageGroup) {
                return ScheduledMessageType.PeriodicMessageGroup;
            }

            if (scheduledMessage instanceof SingleMessage) {
                return ScheduledMessageType.SingleMessage;
            }

            if (scheduledMessage instanceof PeriodicSingleMessage) {
                return ScheduledMessageType.PeriodicSingleMessage;
            }

            throw new NotImplementedException("Developer error! Reach out via GitHub issues!");
        }

        private static final Map<ScheduledMessageType, ScheduledMessageJsonReader> SCHEDULED_MESSAGE_READERS = Util.make(() -> {
            HashMap<ScheduledMessageType, ScheduledMessageJsonReader> readers = new HashMap<>();
            readers.put(ScheduledMessageType.SingleMessage, SingleMessage::readJson);
            readers.put(ScheduledMessageType.PeriodicMessageGroup, PeriodicMessageGroup::readJson);
            readers.put(ScheduledMessageType.PeriodicSingleMessage, PeriodicSingleMessage::readJson);
            return readers;
        });

        public JsonElement serialize(ScheduledMessage src) {
            return serialize(src, null, null);
        }

        @Override
        public JsonElement serialize(ScheduledMessage src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("_do_not_touch_schema_version", 1);
            jsonObject.addProperty("messageType", getTypeCode(src).toString());
            jsonObject.addProperty("messageName", src.messageName);
            src.writeJson(jsonObject);
            return jsonObject;
        }

        public ScheduledMessage deserialize(JsonElement jsonElement) {
            return deserialize(jsonElement, null, null);
        }

        @Override
        public ScheduledMessage deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (!jsonElement.isJsonObject()) {
                throw new JsonParseException("Don't know how to turn the JsonElement " + jsonElement + " into a AScheduledMessage.");
            }

            JsonObject jsonObject = jsonElement.getAsJsonObject();

            var schemaVersion = jsonObject.get("_do_not_touch_schema_version");
            if (schemaVersion == null) {
                ServerAnnounce.LOGGER.warn("Tried to read a ServerAnnounce message configuration file without a schema version.");
            }

            var messageType = ScheduledMessageType.valueOf(jsonObject.get("messageType").getAsString());
            var reader = SCHEDULED_MESSAGE_READERS.get(messageType);

            return reader.readJson(jsonObject);
        }
    }

}
