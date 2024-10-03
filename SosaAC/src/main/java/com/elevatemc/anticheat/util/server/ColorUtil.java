package com.elevatemc.anticheat.util.server;

import com.elevatemc.anticheat.config.Config;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;

@UtilityClass
public class ColorUtil {

    public String translate(final String string) {
        return ChatColor.translateAlternateColorCodes('&', string.replaceAll("%prefix%", Config.PREFIX));
    }
}
