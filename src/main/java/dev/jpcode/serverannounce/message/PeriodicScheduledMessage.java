package dev.jpcode.serverannounce.message;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

public abstract class PeriodicScheduledMessage extends ScheduledMessage {
    public PeriodicScheduledMessage(int tickPeriod) {
        super(tickPeriod);
    }

    @java.lang.Override
    protected void onExec(MinecraftServer server) {
        schedule(server.getTicks());
    }

    @java.lang.Override
    public boolean isDone() {
        return false;
    }

    protected abstract Text nextMessage();
}
