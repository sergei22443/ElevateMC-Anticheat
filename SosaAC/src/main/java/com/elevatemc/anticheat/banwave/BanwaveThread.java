package com.elevatemc.anticheat.banwave;

import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.banwave.file.BanwaveFile;
import com.elevatemc.anticheat.util.server.ServerUtil;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.List;

public class BanwaveThread extends Thread {

    @Override
    @SneakyThrows
    public void run() {
        String text = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "    " + ChatColor.RESET +
                ChatColor.RED + ChatColor.BOLD +
                "Sosa is now playing FANETO " + ChatColor.RESET + ChatColor.GRAY + ChatColor.STRIKETHROUGH + "   ";
        sendDangerSign("",
                text,
                " ",
                ChatColor.RED + "There is a total of " + ChatColor.DARK_RED + getAmountToBan(),
                ChatColor.RED + "listeners in the party!",
                " ",
                text,
                ""
        );

        Thread.sleep(1000);
        BanwaveFile pending = new BanwaveFile("banwave-pending");
        BanwaveFile banned = new BanwaveFile("banwave-banned");
        List<String> commands = Sosa.INSTANCE.getPlugin().getConfig().getStringList("banwave.commands");
        String broadcast = Sosa.INSTANCE.getPlugin().getConfig().getString("banwave.message");
        for  (String s : pending.getConfigFile().getConfigurationSection("PendingUsers").getKeys(false)) {
            String name = pending.getConfigFile().getString("PendingUsers." + s + ".Name");
            String uuid = pending.getConfigFile().getString("PendingUsers." + s + ".UUID");
            String reason = pending.getConfigFile().getString("PendingUsers." + s + ".Reason");
            String executedBy = pending.getConfigFile().getString("PendingUsers." + s + ".ExecutedBy");
            String wasOnline = pending.getConfigFile().getString("PendingUsers." + s + ".wasOnline");
            String date = pending.getConfigFile().getString("PendingUsers." + s + ".Date");
            Thread.sleep(1000);
            for (String command : commands) {
                Thread.sleep(1000);
                Bukkit.getScheduler().runTask(Sosa.INSTANCE.getPlugin(), () -> commands.forEach(cd -> ServerUtil.dispatchCommand(command.replaceAll("%player%", name))));
                ServerUtil.broadcast(broadcast.replaceAll("%player%", name));
            }
            banned.getConfigFile().set("BannedUsers." + s + ".Name", name);
            banned.getConfigFile().set("BannedUsers." + s + ".UUID", uuid);
            banned.getConfigFile().set("BannedUsers." + s + ".Date", date);
            banned.getConfigFile().set("BannedUsers." + s + ".Reason", reason);
            banned.getConfigFile().set("BannedUsers." + s + ".ExecutedBy", executedBy);
            banned.getConfigFile().set("BannedUsers." + s + ".wasOnline", wasOnline);
            banned.saveConfigFile();
        }

        String a = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "    " + ChatColor.RESET +
                ChatColor.RED + ChatColor.BOLD +
                "Sosa has finished playing FANETO " + ChatColor.RESET + ChatColor.GRAY + ChatColor.STRIKETHROUGH + "   ";
        sendDangerSign("",
                a,
                " ",
                ChatColor.RED + "Thank you for listening! ",
                " ",
                a,
                ""
        );
        pending.getConfigFile().set("PendingUsers", null);
        pending.saveConfigFile();
        Sosa.INSTANCE.getPlugin().saveConfig();

    }

    public static int getAmountToBan() {
        BanwaveFile pending = new BanwaveFile("banwave-pending");
        int count = 0;
        if (pending.getConfigFile().getConfigurationSection("PendingUsers") == null) {
            return 0;
        }
        for (String s : pending.getConfigFile().getConfigurationSection("PendingUsers").getKeys(false)) {
            ++count;
        }
        return count;
    }

    public static int getBannedAmount() {
        BanwaveFile pending = new BanwaveFile("banwave-banned");
        int count = 0;
        if (pending.getConfigFile().getConfigurationSection("BannedUsers") == null) {
            return 0;
        }
        for (String ignored : pending.getConfigFile().getConfigurationSection("BannedUsers").getKeys(false)) {
            ++count;
        }
        return count;
    }

    private static void sendDangerSign(String... args) {
        String[] lines = new String[]{"", "", "", "", "", "", "", ""};
        System.arraycopy(args, 0, lines, 0, args.length);
        Bukkit.broadcastMessage(ChatColor.WHITE + "\u2588\u2588\u2588\u2588" + ChatColor.RED + "\u2588" + ChatColor.WHITE + "\u2588\u2588\u2588\u2588" + ChatColor.RESET + (lines[0].isEmpty() ? "" : " " + lines[0]));
        Bukkit.broadcastMessage(ChatColor.WHITE + "\u2588\u2588\u2588" + ChatColor.RED + "\u2588" + ChatColor.GOLD + "\u2588" + ChatColor.RED + "\u2588" + ChatColor.WHITE + "\u2588\u2588\u2588" + ChatColor.RESET + (lines[1].isEmpty() ? "" : new StringBuilder().append(" ").append(lines[1]).toString()));
        Bukkit.broadcastMessage(ChatColor.WHITE + "\u2588\u2588" + ChatColor.RED + "\u2588" + ChatColor.GOLD + "\u2588" + ChatColor.BLACK + "\u2588" + ChatColor.GOLD + "\u2588" + ChatColor.RED + "\u2588" + ChatColor.WHITE + "\u2588\u2588" + ChatColor.RESET + (lines[2].isEmpty() ? "" : new StringBuilder().append(" ").append(lines[2]).toString()));
        Bukkit.broadcastMessage(ChatColor.WHITE + "\u2588\u2588" + ChatColor.RED + "\u2588" + ChatColor.GOLD + "\u2588" + ChatColor.BLACK + "\u2588" + ChatColor.GOLD + "\u2588" + ChatColor.RED + "\u2588" + ChatColor.WHITE + "\u2588\u2588" + ChatColor.RESET + (lines[3].isEmpty() ? "" : new StringBuilder().append(" ").append(lines[3]).toString()));
        Bukkit.broadcastMessage(ChatColor.WHITE + "\u2588" + ChatColor.RED + "\u2588" + ChatColor.GOLD + "\u2588\u2588" + ChatColor.BLACK + "\u2588" + ChatColor.GOLD + "\u2588\u2588" + ChatColor.RED + "\u2588" + ChatColor.WHITE + "\u2588" + ChatColor.RESET + (lines[4].isEmpty() ? "" : new StringBuilder().append(" ").append(lines[4]).toString()));
        Bukkit.broadcastMessage(ChatColor.WHITE + "\u2588" + ChatColor.RED + "\u2588" + ChatColor.GOLD + "\u2588\u2588\u2588\u2588\u2588" + ChatColor.RED + "\u2588" + ChatColor.WHITE + "\u2588" + ChatColor.RESET + (lines[5].isEmpty() ? "" : new StringBuilder().append(" ").append(lines[5]).toString()));
        Bukkit.broadcastMessage(ChatColor.RED + "\u2588" + ChatColor.GOLD + "\u2588\u2588\u2588" + ChatColor.BLACK + "\u2588" + ChatColor.GOLD + "\u2588\u2588\u2588" + ChatColor.RED + "\u2588" + ChatColor.RESET + (lines[6].isEmpty() ? "" : new StringBuilder().append(" ").append(lines[6]).toString()));
        Bukkit.broadcastMessage(ChatColor.RED + "\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588" + ChatColor.RESET + (lines[6].isEmpty() ? "" : " " + lines[7]));
    }
}