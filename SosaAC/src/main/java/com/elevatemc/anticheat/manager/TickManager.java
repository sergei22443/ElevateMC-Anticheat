package com.elevatemc.anticheat.manager;

import com.elevatemc.anticheat.Sosa;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class TickManager implements Runnable {

    @Getter
    private int ticks;
    @Getter
    private long lastResponse;
    private static BukkitTask task;

    public void start() {
        assert task == null : "TickProcessor has already been started!";

        task = Bukkit.getScheduler().runTaskTimer(Sosa.INSTANCE.getPlugin(), this, 0L, 1L);
    }

    public void stop() {
        if (task == null) return;

        task.cancel();
        task = null;
    }

    @Override
    public void run() {
        ticks++;
        lastResponse = System.currentTimeMillis();
    }
}
