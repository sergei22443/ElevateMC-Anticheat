package com.elevatemc.anticheat.processor.player;

import com.elevatemc.anticheat.util.server.PlayerUtil;
import com.elevatemc.anticheat.config.Config;
import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.data.PlayerData;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.nio.charset.StandardCharsets;

public class ClientBrandListener implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(final String channel, final Player player, final byte[] msg) {
        final PlayerData data = Sosa.INSTANCE.getPlayerDataManager().getPlayerData(player);

        if (data == null) return;

        final String clientBrand = StringUtils.capitalize(new String(msg, StandardCharsets.UTF_8).substring(1));

        data.setClientBrand(clientBrand);

        final long now = System.currentTimeMillis();

        alert: {
            if (!Config.CLIENT_BRAND_ALERTS || now - data.getLastClientBrandAlert() < 5000) break alert;
            Sosa.INSTANCE.getAlertManager().sendMessage(Config.CLIENT_BRAND_ALERTS_FORMAT
                    .replaceAll("%player%", player.getName())
                    .replaceAll("%client-version%", PlayerUtil.getClientVersionToString(data))
                    .replaceAll("%client-brand%", clientBrand));

            data.setLastClientBrandAlert(now);

        }
    }
}
