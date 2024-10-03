package com.elevatemc.anticheat;

import com.elevatemc.anticheat.config.Config;
import com.elevatemc.anticheat.database.LogHandler;
import com.elevatemc.anticheat.database.ExportLogsTask;
import com.elevatemc.anticheat.database.log.LogManager;
import com.elevatemc.anticheat.util.cache.CommandUtil;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class SosaPlugin extends JavaPlugin {
    private ExportLogsTask exportLogsTask;

    public static SosaPlugin instance;

    @Getter
    private LogHandler logHandler;
    @Getter
    private LogManager logManager;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().bStats(true).checkForUpdates(false).debug(false);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        instance = this;
        Sosa.INSTANCE.start(this);

        logHandler = new LogHandler();
        logManager = new LogManager();

        Sosa.INSTANCE.getCommandManager().getCommands().forEach(CommandUtil::registerCommand);

        if (Config.DATABASE_LOGS_ENABLED) (exportLogsTask = new ExportLogsTask()).runTaskTimerAsynchronously(this, 300L, 300L);
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
        Sosa.INSTANCE.stop(this);
    }

    public static SosaPlugin getInstance() {
        return instance;
    }
}
