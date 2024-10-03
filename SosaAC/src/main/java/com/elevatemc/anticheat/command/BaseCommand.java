package com.elevatemc.anticheat.command;

import com.elevatemc.anticheat.SosaPlugin;
import com.elevatemc.anticheat.util.server.ColorUtil;
import org.atteo.classindex.IndexSubclasses;
import org.bukkit.command.Command;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Arrays;
import java.util.List;

@IndexSubclasses
// Credit goes out to Sim0n

public abstract class BaseCommand extends Command {
    protected final SosaPlugin plugin = SosaPlugin.getInstance();

    private boolean async;

    public BaseCommand(String name) {
        super(name);

        Class<? extends BaseCommand> clazz = getClass();
        if (!clazz.isAnnotationPresent(CommandManifest.class)) {
            return;
        }

        CommandManifest manifest = clazz.getAnnotation(CommandManifest.class);

        setPermission(manifest.permission());
        async = manifest.async();
    }

    public abstract void handle(Player player, List<String> args);

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof Player))
            return true;

        if (getPermission() != null && !sender.hasPermission(getPermission())) {
            sender.sendMessage(ChatColor.RED + getPermissionMessage());
            return true;
        }

        Player player = (Player) sender;

        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> handle(player, Arrays.asList(args)));
        } else {
            handle(player, Arrays.asList(args));
        }

        return true;
    }

    /**
     * Sends a message with the anticheat prefix appended
     * @param player The player to send the message to
     * @param message The message to send
     */
    public void sendMessage(final Player player, final String message) {
        player.sendMessage(ColorUtil.translate(message));
    }

    @Override
    public String getPermissionMessage() {
        return ChatColor.RED + "You are unable to use this command.";
    }
}
