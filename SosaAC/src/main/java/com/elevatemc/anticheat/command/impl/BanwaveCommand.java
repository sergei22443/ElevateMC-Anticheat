package com.elevatemc.anticheat.command.impl;

import com.elevatemc.anticheat.banwave.BanwaveThread;
import com.elevatemc.anticheat.banwave.file.BanwaveFile;
import com.elevatemc.anticheat.util.server.ColorUtil;
import com.elevatemc.anticheat.manager.APIManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.*;
import java.util.*;
import org.bukkit.*;
public class BanwaveCommand implements CommandExecutor {
    public boolean onCommand(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (!sender.hasPermission("sosa.banwave") || !sender.hasPermission("sosa.admin")) {
          sender.sendMessage(ColorUtil.translate("&cYou don't have permission to run this command."));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ColorUtil.translate("&8&l------»&r  " + "&c&lSosa" + "  §8§l«------"));
            sender.sendMessage(ColorUtil.translate("&7/&fbanwave info &7- &3Get information about the banwave"));
            sender.sendMessage(ColorUtil.translate("&7/&fbanwave execute &7- &3Starts the banwave"));
            sender.sendMessage(ColorUtil.translate("&7/&fbanwave add <&3player&f> <reason> &7(Optional) - &3Adds player to the banwave"));
            sender.sendMessage(ColorUtil.translate("&8&m---------------------------"));
            return true;
        } else if (args[0].equalsIgnoreCase("execute")) {
            if (BanwaveThread.getAmountToBan() == 0) {
                sender.sendMessage(ColorUtil.translate("&cThere are no pending bans"));
                return true;
            }
            new BanwaveThread().start();
            return true;
        }  else if (args[0].equalsIgnoreCase("info")) {
            sender.sendMessage(ColorUtil.translate("&c&l------»&r  " + "&c&lSOSA" + "  §c§l«------"));
            sender.sendMessage(ColorUtil.translate(""));
            sender.sendMessage(ColorUtil.translate("&eTotal players: &c&l" + BanwaveThread.getAmountToBan()));
            sender.sendMessage(ColorUtil.translate("&eTotal Banwaved players: &c&l" + BanwaveThread.getBannedAmount()));
            sender.sendMessage(ColorUtil.translate("&c&m---------------------------"));
            return true;
        }
        else {
            if (!args[0].equalsIgnoreCase("add")) {
                sender.sendMessage(ColorUtil.translate("&cInvalid args! Usage: /banwave add (name)"));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ColorUtil.translate("&cInvalid args! Usage: /banwave add (name)"));
                return true;
            }
            final OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            final BanwaveFile pending = new BanwaveFile("banwave-pending");
            final StringBuilder str = new StringBuilder();
            for (int i = 2; i < args.length; ++i) {
                str.append(args[i]).append(" ");
            }
            String reason = str.toString();
            if (reason.isEmpty() || reason.equals(" ")) {
                reason = "No Reason Specified. ";
            }
            else {
                reason = str.toString();
            }
            final int reasonLength = reason.length();
            reason = reason.substring(0, reasonLength - 1);
            pending.getConfigFile().set("PendingUsers." + target.getUniqueId() + ".Name", target.getName());
            pending.getConfigFile().set("PendingUsers." + target.getUniqueId() + ".UUID", String.valueOf(target.getUniqueId()));
            pending.getConfigFile().set("PendingUsers." + target.getUniqueId() + ".Date", String.valueOf(Calendar.getInstance().getTime()));
            pending.getConfigFile().set("PendingUsers." + target.getUniqueId() + ".Reason", reason);
            pending.getConfigFile().set("PendingUsers." + target.getUniqueId() + ".ExecutedBy", sender.getName());
            pending.getConfigFile().set("PendingUsers." + target.getUniqueId() + ".wasOnline", sender.getName());
            if (target.isOnline()) {
                pending.getConfigFile().set("PendingUsers." + target.getUniqueId() + ".wasOnline", true);
            }
            else {
                pending.getConfigFile().set("PendingUsers." + target.getUniqueId() + ".wasOnline", false);
            }
            pending.saveConfigFile();
            APIManager.judgementEvent(target.getPlayer(), reason);
            final String name = args[1];
            sender.sendMessage(ColorUtil.translate("&c&l" + name + " &ehas been added to the Judgement Day"));
            return true;
        }
    }
}

