package com.elevatemc.anticheat.manager;

import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.SosaPlugin;
import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.api.SosaFlagEvent;
import com.elevatemc.api.SosaJudgementDayAddEvent;
import com.elevatemc.api.SosaPunishEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class APIManager {

    public static void flagEvent(final Check check) {
        SosaFlagEvent event = new SosaFlagEvent(check.getData().getPlayer(), check, check.getName(), check.getType(), check.getVl());

        Bukkit.getScheduler().runTask(SosaPlugin.getInstance(), () -> Bukkit.getPluginManager().callEvent(event));
    }

    public static void punishEvent(final Check check) {
        SosaPunishEvent event = new SosaPunishEvent(check.getData().getPlayer(), check, check.getName(), check.getType(), check.getVl());

        Bukkit.getScheduler().runTask(SosaPlugin.getInstance(), () -> Bukkit.getPluginManager().callEvent(event));
    }

    public static void judgementEvent(Player player, final String check) {
        SosaJudgementDayAddEvent event = new SosaJudgementDayAddEvent(player.getPlayer(), check);

        Bukkit.getScheduler().runTask(SosaPlugin.getInstance(), () -> Bukkit.getPluginManager().callEvent(event));
    }

    public static PlayerData getPlayerData(final Player player) {
        return Sosa.INSTANCE.getPlayerDataManager().getPlayerData(player);
    }
}
