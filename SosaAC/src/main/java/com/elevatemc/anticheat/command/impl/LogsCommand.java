package com.elevatemc.anticheat.command.impl;


import com.elevatemc.anticheat.SosaPlugin;
import com.elevatemc.anticheat.command.BaseCommand;
import com.elevatemc.anticheat.command.CommandManifest;
import com.elevatemc.anticheat.database.log.Log;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.libs.joptsimple.internal.Strings;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CommandManifest(permission = "sosa.logs", async = true)
public class LogsCommand extends BaseCommand {

    private double vl;
    private int totalLogs;
    public LogsCommand() {
        super("logs");
    }
    @Override
    public void handle(Player player, List<String> args) {
        if (args.size() < 2) {
            sendMessage(player, "&c" + "/logs (player) (page)");
        } else {

            sendMessage(player, "&c&lSOSA &eGathering &c" + args.get(0) + " 's &erecords");

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args.get(0));
            int page = Integer.parseInt(args.get(1));

            if (offlinePlayer == null) {
                sendMessage(player, "&cPlayer doesn't exist");
                return;
            }

            UUID uuid = offlinePlayer.getUniqueId();

            Iterable<Log> logs = SosaPlugin.getInstance().getLogHandler().getCollection("PlayerViolations")
                    .find(Filters.eq("uuid", uuid.toString()))
                    .sort(Indexes.descending("_id"))
                    .map(Log::fromDocument);

            StringBuilder sb = new StringBuilder();
            List<String> clients = new ArrayList<>(), bans = new ArrayList<>();
            List<String> lines = new ArrayList<>();
            List<Double> violations = new ArrayList<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String format = "and [%s] &b%s %s &8- &fPing: &b%s, &fVL: &b%s, &fClient: &b%s, &fInfo: &b%s";

            SosaPlugin.getInstance().getLogManager().getQueuedLogs().stream()
                    .filter(log -> log.getUuid().equals(uuid))
                    .forEach(log -> sb.append(String.format(format, dateFormat.format(log.getTimestamp()), log.getCheck(), log.getType(), log.getBrand(), log.getPing(), log.getData()))
                            .append("\n"));

            logs.forEach(log -> sb.append(lines.add(String.format(format, dateFormat.format(log.getTimestamp()), log.getCheck(), log.getType(), log.getPing(), new DecimalFormat(".##").format(log.getVl()), log.getBrand(), log.getData()))).append("\n"));

            logs.forEach(log -> {
                violations.add(log.getVl());
                if (!clients.contains(log.getBrand())) {
                    clients.add(log.getBrand());
                }
                if (log.getData().equalsIgnoreCase("<----- Automatically banned for")) {
                    bans.add(log.getCheck() + " Type " + log.getType());
                }
                totalLogs = violations.size();
            });
            final int maxPages = (int) Math.ceil(lines.size() / 10D);

            if (page > maxPages) {
                sendMessage(player, "&cThey have less log pages than that big man.");
                return;
            }
            sendMessage(player, "&7&m---»--*--&r&7[ Logs for &b" + offlinePlayer.getName() + " &7(&b" + page + "&7/&b" + maxPages + " &7) &7]&7&m--*--«---");
            sendMessage(player, "&fTotal VL: &b" + totalLogs);
            sendMessage(player, "&fClients: &b" + Strings.join(clients, ", ") );
            if (!bans.isEmpty()) {
                sendMessage(player, "&fBanned for: &b" + Strings.join(bans, ", ") );
            }
            sendMessage(player, "");
            int blank = 0;
            for (int i = (page - 1) * 10; (i) < page * 10; i++) {
                if (i < lines.size()) {
                    String[] spl = lines.get(i).split("and");
                    if (blank > 0) player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&r"));
                    sendMessage(player,"&f"+ spl[1]);
                    ++blank;
                }
            }
            sendMessage(player, "&7&m---»--*--&r&7[ Logs for &b" + offlinePlayer.getName() + " &7(&b" + page + "&7/&b" + maxPages + " &7) &7]&7&m--*--«---");

            if (sb.length() == 0) {
                sendMessage(player,"&cNo logs found for " + offlinePlayer.getName());
            }
        }
    }
}