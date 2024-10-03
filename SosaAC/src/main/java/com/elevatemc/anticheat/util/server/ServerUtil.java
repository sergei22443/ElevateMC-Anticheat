package com.elevatemc.anticheat.util.server;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

@UtilityClass
public class ServerUtil {

    public double getTPS() {
        return Math.min(20, SpigotReflectionUtil.getTPS());
    }

    public void dispatchCommand(final String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ColorUtil.translate(command));
    }

    public ServerVersion getVersion() {
        return PacketEvents.getAPI().getServerManager().getVersion();
    }

    public void broadcast(final String message) {
        Bukkit.broadcastMessage(ColorUtil.translate(message));
    }
}
