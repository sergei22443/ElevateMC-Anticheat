package com.elevatemc.anticheat.database;

import com.elevatemc.anticheat.SosaPlugin;
import com.elevatemc.anticheat.database.log.Log;

import com.elevatemc.anticheat.database.log.LogManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Queue;
import java.util.stream.Collectors;

public class ExportLogsTask extends BukkitRunnable {
    private final LogHandler dbManager = SosaPlugin.getInstance().getLogHandler();
    private final LogManager logManager = SosaPlugin.getInstance().getLogManager();
    @Override
    public void run() {
        Queue<Log> queuedLogs = logManager.getQueuedLogs();

        if (queuedLogs.isEmpty())
            return;

        dbManager.getCollection("PlayerViolations").insertMany(queuedLogs.stream()
                .map(Log::toDocument)
                .collect(Collectors.toList()));

        System.out.println("Saving " + logManager.getQueuedLogs().size() + " logs...");

        queuedLogs.clear();
    }
}