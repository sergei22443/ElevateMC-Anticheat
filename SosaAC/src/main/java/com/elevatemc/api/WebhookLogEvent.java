package com.elevatemc.api;

import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.SosaPlugin;
import com.elevatemc.anticheat.config.Config;
import com.elevatemc.anticheat.util.hook.DiscordWebhook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.awt.*;
import java.io.IOException;

public class WebhookLogEvent implements Listener {

    @EventHandler
    public void onSosaPunishEvent(SosaPunishEvent event) {
        final Player player = event.getPunishedPlayer();
        final String checkName = event.getName();
        final String checkType = event.getType();

        sendDiscordWebhook(player, checkName, checkType);
    }

    private void sendDiscordWebhook(Player player, String name, String type) {
        if (!Config.WEBHOOK_ENABLED)
            return;

        Bukkit.getScheduler().runTaskAsynchronously(SosaPlugin.getInstance(), () -> {
            final DiscordWebhook webhook = new DiscordWebhook(Config.WEBHOOK_URL);
            webhook.setAvatarUrl(Config.WEBHOOK_AVATAR);
            webhook.setUsername(Config.USERNAME);
            webhook.addEmbed(
                    new DiscordWebhook.EmbedObject().setTitle("Sosa Punishment").setDescription("Player: " + player.getName())
                            .setThumbnail("http://cravatar.eu/avatar/" + player.getName() + "/64.png")
                            .setColor(new Color(0, 255, 0)).addField("Server", Sosa.INSTANCE.getPlugin().getServer().getServerName(), true)
                            .addField("Punished for: " ,"`" + name + " Type " + type + "`", true));

            try {
                webhook.execute();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        });
    }
}
