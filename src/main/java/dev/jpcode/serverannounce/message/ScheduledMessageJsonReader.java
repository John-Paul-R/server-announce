package dev.jpcode.serverannounce.message;

import com.google.gson.JsonObject;

@FunctionalInterface
public interface ScheduledMessageJsonReader {
    ScheduledMessage readJson(JsonObject root);
}
