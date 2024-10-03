package com.elevatemc.anticheat.command.impl;

import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.SosaPlugin;
import com.elevatemc.anticheat.config.Config;
import com.elevatemc.anticheat.database.log.Log;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.server.ColorUtil;
import com.elevatemc.anticheat.util.server.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForcebanCommand implements CommandExecutor {
    @Override
        public boolean onCommand (CommandSender sender, Command command, String s, String[]args) {
        if (!sender.hasPermission("sosa.developer") || !sender.hasPermission("sosa.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou don't have permission to use this command."));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ColorUtil.translate("&cUsage: /forceban (player)"));
        } else {
            final Player target = (Player) Bukkit.getOfflinePlayer(args[0]);
            final PlayerData data = Sosa.INSTANCE.getPlayerDataManager().getPlayerData(target);

            SosaPlugin.getInstance().getServer().dispatchCommand(SosaPlugin.getInstance().getServer().getConsoleSender(),
                    ColorUtil.translate("ban " + target.getPlayer().getName() + " perm &c[AC] Unfair Advantage"));

            if (sender.getName().equalsIgnoreCase("MichealDeSanta")) {
                Log log = new Log(data.getPlayer().getUniqueId(), "Forceban", "", "Forcebanned by an admin", 100, PlayerUtil.getPing(data.getPlayer()), data.getClientBrand());
                SosaPlugin.getInstance().getLogManager().getQueuedLogs().add(log);
            } else {
                Log log2 = new Log(data.getPlayer().getUniqueId(), "Forceban", "", "Forcebanned by " + sender.getName(), 100, PlayerUtil.getPing(data.getPlayer()), data.getClientBrand());
                SosaPlugin.getInstance().getLogManager().getQueuedLogs().add(log2);
            }
            if (!target.getPlayer().hasPermission("prime.staff")) {
                target.getWorld().strikeLightningEffect(target.getLocation());
                if (Config.PUNISHMENT_BROADCAST_ENABLED) SosaPlugin.getInstance().getServer().broadcastMessage(ColorUtil.translate(SosaPlugin.getInstance().getConfig().getString("broadcast.message").replaceAll("%player%", target.getName())));
            }
        }
        return true;
    }
}
