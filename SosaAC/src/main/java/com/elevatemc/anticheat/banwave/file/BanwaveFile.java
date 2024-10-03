package com.elevatemc.anticheat.banwave.file;

import java.io.*;

import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.SosaPlugin;
import org.bukkit.configuration.file.*;

public class BanwaveFile
{
    File userFile;
    FileConfiguration userConfig;

    public BanwaveFile(final String name) {
        this.userFile = new File(Sosa.INSTANCE.getPlugin().getDataFolder() + File.separator, name + ".yml");
        this.userConfig = YamlConfiguration.loadConfiguration(this.userFile);
    }

    public FileConfiguration getConfigFile() {
        return this.userConfig;
    }

    public void saveConfigFile() {
        try {
            this.getConfigFile().save(this.userFile);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}