package com.elevatemc.anticheat.config;

import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.util.cache.CacheUtil;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class Config {

    public Map<String, Boolean> ENABLED_CHECKS = new HashMap<>();
    public Map<String, Boolean> PUNISHABLE = new HashMap<>();
    public Map<String, Integer> MAX_VIOLATIONS = new HashMap<>();
    public Map<String, List<String>> PUNISHMENT_COMMANDS = new HashMap<>();

    public String PREFIX, ALERTS_ENABLED, ALERTS_DISABLED, ALERTS_FORMAT, CLIENT_BRAND_ALERTS_FORMAT, PUNISHMENT_MESSAGE,
            VIOLATION_RESET_MESSAGE, PUNISHMENT_BROADCAST_MESSAGE;

    public boolean ENABLE_ALERTS_ON_JOIN , CLIENT_BRAND_ALERTS, PUNISHMENT_BROADCAST_ENABLED,VIOLATION_RESET, VIOLATION_RESET_MESSAGE_ENABLED, DATABASE_LOGS_ENABLED;
    public boolean WEBHOOK_ENABLED;
    public String WEBHOOK_AVATAR, USERNAME, WEBHOOK_URL;
    public List<String> ALERTS_CLICK_COMMANDS, ALERTS_HOVER_MESSAGES;

    public void updateConfig() {
        try {
            PREFIX = getString("prefix");
            ENABLE_ALERTS_ON_JOIN = getBoolean("settings.enable-alerts-on-join");
            ALERTS_ENABLED = getString("messages.alerts-enabled");
            ALERTS_DISABLED = getString("messages.alerts-disabled");
            ALERTS_FORMAT = getString("alerts.format");
            ALERTS_CLICK_COMMANDS = getStringList("alerts.click-commands");
            ALERTS_HOVER_MESSAGES = getStringList("alerts.hover-message");
            CLIENT_BRAND_ALERTS = getBoolean("client-brand-alerts.enabled");
            CLIENT_BRAND_ALERTS_FORMAT = getString("client-brand-alerts.format");
            PUNISHMENT_MESSAGE = getString("punishments.message");
            PUNISHMENT_BROADCAST_ENABLED = getBoolean("broadcast.enabled");
            PUNISHMENT_BROADCAST_MESSAGE = getString("broadcast.message");
            DATABASE_LOGS_ENABLED = getBoolean("database-logs.enabled");
            VIOLATION_RESET = getBoolean("violation-reset.enabled");
            VIOLATION_RESET_MESSAGE_ENABLED = getBoolean("violation-reset.message-enabled");
            VIOLATION_RESET_MESSAGE = getString("violation-reset.message");
            WEBHOOK_ENABLED = getBoolean("discord-webhook.enabled");
            WEBHOOK_AVATAR = getString("discord-webhook.avatar");
            WEBHOOK_URL = getString("discord-webhook.webhook");
            USERNAME = getString("discord-webhook.username");
            CacheUtil.resetConfigValues();
            CacheUtil.updateCheckValues();
        } catch (final Exception e) {
            Bukkit.getLogger().severe("Error while reading Sosa's configuration file!");
            e.printStackTrace();
        }
    }

    public boolean getBoolean(final String string) {
        return Sosa.INSTANCE.getPlugin().getConfig().getBoolean(string);
    }

    public String getString(final String string) {
        return Sosa.INSTANCE.getPlugin().getConfig().getString(string);
    }

    public int getInt(final String string) {
        return Sosa.INSTANCE.getPlugin().getConfig().getInt(string);
    }


    public List<String> getStringList(final String string) {
        return Sosa.INSTANCE.getPlugin().getConfig().getStringList(string);
    }
}
