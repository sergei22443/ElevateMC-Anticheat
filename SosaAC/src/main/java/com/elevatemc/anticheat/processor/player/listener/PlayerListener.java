package com.elevatemc.anticheat.processor.player.listener;

import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.config.Config;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        alerts: {
            if (!player.hasPermission("sosa.alerts") || !Config.ENABLE_ALERTS_ON_JOIN) break alerts;

            Sosa.INSTANCE.getAlertManager().toggleAlerts(player);
        }

        Sosa.INSTANCE.getPlayerDataManager().getPlayerData(event.getPlayer().getUniqueId()).initializePlayer(event.getPlayer());

        if (Sosa.INSTANCE.getPlayerDataManager().getPlayerData(event.getPlayer()).getPlayer() == null) {
            event.getPlayer().kickPlayer("Sosa failed to initialize your data.");
        }
    }
}
