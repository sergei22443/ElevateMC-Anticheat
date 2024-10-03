package com.elevatemc.anticheat.manager;

import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.server.ColorUtil;
import com.elevatemc.anticheat.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class ResetManager implements Runnable {

    private static BukkitTask task;

    public void reset() {
        if (Config.VIOLATION_RESET) {
            for (PlayerData data : Sosa.INSTANCE.getPlayerDataManager().getAllData()) {
                for (Check check : data.getChecks()) {
                    check.setVl(0);
                    check.resetBuffer();
                }
            }

            if (Config.VIOLATION_RESET_MESSAGE_ENABLED) {
                System.out.println(ColorUtil.translate(Config.VIOLATION_RESET_MESSAGE));

                Sosa.INSTANCE.getAlertManager().sendMessage(Config.VIOLATION_RESET_MESSAGE);
            }
        }
    }

    public void start() {
        assert task == null : "ResetProcessor has already been started!";

        final int RESET_INTERVAL = Config.getInt("violation-reset.interval-in-minutes");

        task = Bukkit.getScheduler().runTaskTimerAsynchronously(Sosa.INSTANCE.getPlugin(), this, RESET_INTERVAL * 1200L, RESET_INTERVAL * 1200L);
    }

    public void stop() {
        if (task == null) return;

        task.cancel();
        task = null;
    }

    @Override
    public void run() {
        reset();
    }
}
