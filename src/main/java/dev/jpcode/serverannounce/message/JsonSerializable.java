package dev.jpcode.serverannounce.message;

import com.google.gson.JsonObject;

public interface JsonSerializable {
    void writeJson(JsonObject root);
}
