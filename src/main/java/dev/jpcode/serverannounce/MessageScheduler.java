package dev.jpcode.serverannounce;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import dev.jpcode.serverannounce.message.PeriodicSingleMessage;
import dev.jpcode.serverannounce.message.ScheduledMessage;

import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.newBufferedReader;

public class MessageScheduler {

    private final LinkedHashMap<String, ScheduledMessage> scheduledMessages;
    private final ArrayList<ScheduledMessage> notDoneMessages;
    private static MessageScheduler instance;

    public MessageScheduler() {
        instance = this;
        this.scheduledMessages = new LinkedHashMap<>();
        notDoneMessages = new ArrayList<>();

        ServerTickEvents.END_SERVER_TICK.register(this::tick);
    }

    public static MessageScheduler getInstance() {
        return instance;
    }

    private void tick(MinecraftServer server) {

        Iterator<ScheduledMessage> it = notDoneMessages.iterator();
        while (it.hasNext()) {
            ScheduledMessage message = it.next();
            message.tick(server);
            if (message.isDone()) {
                it.remove();
            }
        }
    }

    public Stream<ScheduledMessage> streamScheduledMessages() {
        return this.scheduledMessages.values().stream();
    }

    public Stream<Map.Entry<String, ScheduledMessage>> streamScheduledMessagesEntries() {
        return this.scheduledMessages.entrySet().stream();
    }

    public ScheduledMessage getScheduledMessage(String messageName) {
        return scheduledMessages.get(messageName);
    }

    public void scheduleMessage(String messageName, ScheduledMessage message) {
        scheduledMessages.put(messageName, message);
        notDoneMessages.add(message);
        this.save();
    }

    private void saveScheduledMessage(ScheduledMessage message, File file, ScheduledMessage.Serializer serializer) {
        JsonElement jsonElement = serializer.serialize(message);
        try {
            Gson gson = new Gson();
            JsonWriter writer = gson.newJsonWriter(new FileWriter(file));
            writer.setIndent("  ");

            gson.toJson(jsonElement, writer);

            writer.flush();
            writer.close();

        } catch (IOException e) {
            ServerAnnounce.LOGGER.error("Could not save data {}", message, e);
        }

    }

    private static final Path SAVE_PATH = Paths.get("./config/server_announce/scheduled_messages");

    private Path messageFilePath(String messageName) {
        return SAVE_PATH.resolve(messageName.concat(".json"));
    }

    public void save() {
        final File saveDir = SAVE_PATH.toFile();
        saveDir.mkdirs();
        final ScheduledMessage.Serializer serializer = new ScheduledMessage.Serializer();
        for (Map.Entry<String, ScheduledMessage> next : scheduledMessages.entrySet()) {
            saveScheduledMessage(next.getValue(), messageFilePath(next.getKey()).toFile(), serializer);
        }
    }

    public void initExampleMessage() {
        scheduleMessage("ExampleMessage", new PeriodicSingleMessage(
            "ExampleMessage",
            new LiteralText("")
                .append(new LiteralText("ServerAnnounce").formatted(Formatting.GREEN))
                .append(new LiteralText(" >> ").formatted(Formatting.DARK_GRAY))
                .append(
                    new LiteralText(String.format(
                        "Configure scheduled messages in '%s'.\nThis message will repeat every 2 minutes.",
                        SAVE_PATH.toFile().getPath()
                    )).formatted(Formatting.GRAY)
                ),
            20 * 60 * 2
        ));
        this.save();
    }

    public void load() {
        final File saveDir = SAVE_PATH.toFile();
        if (!saveDir.exists() || !saveDir.isDirectory()) {
            ServerAnnounce.LOGGER.warn("ScheduledMessage directory did not exist.");
            saveDir.mkdirs();
//            initExampleMessage();
            return;
        }
        final ScheduledMessage.Serializer serializer = new ScheduledMessage.Serializer();
        final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ScheduledMessage.class, serializer)
            .create();
        File[] files = saveDir.listFiles();
        for (File file : files) {
            if (!file.getPath().endsWith(".json")) {
                ServerAnnounce.LOGGER.warn("Skipping non-JSON file '{}'", file.getPath());
                continue;
            }

            try {
                ScheduledMessage message = gson.fromJson(newBufferedReader(file.toPath()), ScheduledMessage.class);
                this.scheduledMessages.put(
                    Files.getNameWithoutExtension(file.getName()),
                    Objects.requireNonNull(
                        message,
                        String.format("Message loaded from file '%s' failed to parse!", file.getPath())
                    )
                );
                notDoneMessages.add(message);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                ServerAnnounce.LOGGER.error(e.getMessage());
            }
        }

    }

    public @Nullable ScheduledMessage deleteScheduledMessage(String messageName) {
        var deletedMsg = scheduledMessages.remove(messageName);
        notDoneMessages.remove(deletedMsg);

        try {
            deleteIfExists(messageFilePath(messageName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return deletedMsg;
    }
}
