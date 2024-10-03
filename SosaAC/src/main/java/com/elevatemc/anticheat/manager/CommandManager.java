package com.elevatemc.anticheat.manager;

import com.elevatemc.anticheat.command.BaseCommand;
import com.elevatemc.anticheat.util.server.ColorUtil;
import lombok.Getter;
import org.atteo.classindex.ClassIndex;
import org.bukkit.command.CommandSender;

import java.util.HashSet;
import java.util.Set;
// Credit goes out to Sim0n
@Getter
public class CommandManager {

    private final Set<BaseCommand> commands = new HashSet<>();

    public CommandManager() {
        ClassIndex.getSubclasses(BaseCommand.class, BaseCommand.class.getClassLoader())
                .forEach(clazz -> {
                    try {
                        commands.add(clazz.newInstance());
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
    }
}
