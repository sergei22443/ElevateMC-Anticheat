package com.elevatemc.anticheat.util.cache;

import com.elevatemc.anticheat.config.Config;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.manager.CheckManager;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class CacheUtil {

    public void resetConfigValues() {
        Config.ENABLED_CHECKS.clear();
        Config.PUNISHABLE.clear();
        Config.MAX_VIOLATIONS.clear();
    }

    public void updateCheckValues() {
        for (final Class clazz : CheckManager.CHECKS) {
            final CheckInfo checkInfo = (CheckInfo) clazz.getAnnotation(CheckInfo.class);

            final String className = clazz.getSimpleName();

            String checkCategory = "";
            if (clazz.getName().contains("combat")) {
                checkCategory = "combat";
            } else if (clazz.getName().contains("movement")) {
                checkCategory = "movement";
            } else if (clazz.getName().contains("player")) {
                checkCategory = "player";
            }

            final String checkName = checkInfo.name().toLowerCase().replaceAll(" ", "");
            final String checkType = checkInfo.type().toLowerCase();

            final String path = "checks." + checkCategory + "." + checkName + "." + checkType + ".";

            final boolean enabled = Config.getBoolean(path + "enabled");
            final boolean punishable = Config.getBoolean(path + "punishable");
            final int maxViolations = Config.getInt(path + "max-violations");
            final List<String> punishmentCommands = Config.getStringList(path + "punishment-commands");

            Config.ENABLED_CHECKS.put(className, enabled);
            Config.PUNISHABLE.put(className, punishable);
            Config.MAX_VIOLATIONS.put(className, maxViolations);
            Config.PUNISHMENT_COMMANDS.put(className, punishmentCommands);
        }
    }
}
