package com.elevatemc.anticheat.command.impl;

import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.command.BaseCommand;
import com.elevatemc.anticheat.command.CommandManifest;
import org.bukkit.entity.Player;

import java.util.List;

@CommandManifest(permission = "sosa.alerts")
public class AlertsCommand extends BaseCommand {
    public AlertsCommand() {
        super("alerts");
    }

    @Override
    public void handle(Player player, List<String> args) {
        Sosa.INSTANCE.getAlertManager().toggleAlerts(player);
    }
}
