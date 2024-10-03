package com.elevatemc.anticheat.manager;

import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.SosaPlugin;
import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.database.log.Log;
import com.elevatemc.anticheat.config.Config;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.server.ColorUtil;
import com.elevatemc.anticheat.util.server.PlayerUtil;
import com.elevatemc.anticheat.util.server.ServerUtil;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PunishmentManager {

    @Getter
    private HashMap<UUID, Integer> bannedPlayers = new HashMap<>();

    public void punish(final Check check, final PlayerData data) {
        if (!Config.PUNISHABLE.get(check.getClass().getSimpleName())) return;

        if (bannedPlayers.containsKey(data.getPlayer().getUniqueId())) {
            if (Sosa.INSTANCE.getTickManager().getTicks() - bannedPlayers.get(data.getPlayer().getUniqueId()) < 50)
                return;
        }

        bannedPlayers.put(data.getPlayer().getUniqueId(), Sosa.INSTANCE.getTickManager().getTicks());

        Sosa.INSTANCE.getAlertManager().sendMessage(Config.PUNISHMENT_MESSAGE
                .replaceAll("%prefix%", Config.PREFIX)
                .replaceAll("%player%", data.getPlayer().getName())
                .replaceAll("%check%", check.getCheckInfo().name())
                .replaceAll("%type%", check.getCheckInfo().type())
                .replaceAll("%vl%", new DecimalFormat(".##").format(check.getVl())));


        List<String> punishmentCommands = Config.PUNISHMENT_COMMANDS.get(check.getClass().getSimpleName());
        String broadcastMessages = Config.PUNISHMENT_BROADCAST_MESSAGE;
        boolean broadcastEnabled = Config.PUNISHMENT_BROADCAST_ENABLED;

        punish: {
            if (punishmentCommands.isEmpty()) break punish;

            data.getChecks().forEach(check1 -> check1.setVl(0));

            Bukkit.getScheduler().runTask(Sosa.INSTANCE.getPlugin(), () -> {
                ServerUtil.dispatchCommand("sosa crash " + data.getPlayer().getName());
            });

            Bukkit.getScheduler().runTaskLater(Sosa.INSTANCE.getPlugin(), () -> punishmentCommands.forEach(command -> {
                ServerUtil.dispatchCommand(command
                        .replaceAll("%player%", data.getPlayer().getName())
                        .replaceAll("%description%", check.getCheckInfo().description())
                        .replaceAll("%check%", check.getCheckInfo().name())
                        .replaceAll("%type%", check.getCheckInfo().type()));
            }), 20 * 3);


            APIManager.punishEvent(check);

            data.getPlayer().getLocation().getWorld().strikeLightningEffect(data.getPlayer().getLocation());

            SosaPlugin.getInstance().getLogManager().getQueuedLogs().add(new Log(System.currentTimeMillis(), data.getPlayer().getUniqueId(), check.getName(), check.getType(), "<----- Automatically banned for", check.getVl(), PlayerUtil.getPing(data.getPlayer()), data.getClientBrand()));

           if (broadcastEnabled && !data.getPlayer().hasPermission("core.staff")) {
                Bukkit.broadcastMessage(ColorUtil.translate(String.valueOf(broadcastMessages)).replaceAll("%player%", data.getPlayer().getName()));
            }
        }
    }
}
