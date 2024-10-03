package com.elevatemc.anticheat.command.impl;


import com.elevatemc.anticheat.SosaPlugin;
import com.elevatemc.anticheat.command.BaseCommand;
import com.elevatemc.anticheat.command.CommandManifest;
import com.elevatemc.anticheat.database.LogHandler;
import com.github.retrooper.packetevents.util.UUIDUtil;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

@CommandManifest(permission = "sosa.developer", async = true)
public class PurgeLogsCommand extends BaseCommand {

    public PurgeLogsCommand() {
        super("purge");
    }

    @Override
    public void handle(Player player, List<String> args) {
        if (args.size() < 1) {
            sendMessage(player, "&c" + "/purge (player) / '*' for everyones logs. ");
        } else {
            if (!player.getName().equalsIgnoreCase("trashes")) return;

            Player p = Bukkit.getPlayer(args.get(0));

            UUID uuid = p.getUniqueId();

            if (uuid == null) return;

            if (args.get(0).equalsIgnoreCase("*")) {
                SosaPlugin.getInstance().getLogHandler().getCollection("PlayerViolations").deleteMany(new Document());
                sendMessage(player, "&aSuccessfully deleted all logs.");
            }
            SosaPlugin.getInstance().getLogHandler().getCollection("PlayerViolations").deleteMany(Filters.eq("uuid", uuid.toString()));
            sendMessage(player, "&aSuccessfully deleted " + uuid + " logs.");
        }
    }
}
