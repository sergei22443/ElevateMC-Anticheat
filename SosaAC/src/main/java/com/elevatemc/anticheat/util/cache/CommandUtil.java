package com.elevatemc.anticheat.util.cache;

import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.SosaPlugin;
import com.elevatemc.anticheat.command.BaseCommand;
import lombok.experimental.UtilityClass;
import org.bukkit.command.CommandMap;
import org.bukkit.Bukkit;
@UtilityClass
// Credit goes out to Sim0n
public class CommandUtil {
    private final CommandMap commandMap = ReflectionUtil.getFieldValue(ReflectionUtil.getField(Bukkit.getServer().getClass(), "commandMap"), Bukkit.getServer());

    public void registerCommand(BaseCommand command) {
        try {
            commandMap.register(Sosa.INSTANCE.getPlugin().getName(), command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
