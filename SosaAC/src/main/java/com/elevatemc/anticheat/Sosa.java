package com.elevatemc.anticheat;

import com.elevatemc.anticheat.manager.AlertManager;
import com.elevatemc.anticheat.banwave.BanwaveThread;
import com.elevatemc.anticheat.manager.CheckManager;
import com.elevatemc.anticheat.manager.CommandManager;
import com.elevatemc.anticheat.manager.PlayerDataManager;
import com.elevatemc.anticheat.processor.player.listener.EventProcessor;
import com.elevatemc.anticheat.processor.player.listener.CrackshotListener;
import com.elevatemc.anticheat.util.server.ServerUtil;
import com.elevatemc.api.WebhookLogEvent;
import com.elevatemc.anticheat.processor.player.ClientBrandListener;
import com.elevatemc.anticheat.command.impl.*;
import com.elevatemc.anticheat.config.Config;
import com.elevatemc.anticheat.processor.player.listener.PlayerListener;
import com.elevatemc.anticheat.manager.NetworkManager;
import com.elevatemc.anticheat.processor.processor.ReceivingPacketProcessor;
import com.elevatemc.anticheat.processor.processor.SendingPacketProcessor;
import com.elevatemc.anticheat.manager.PunishmentManager;
import com.elevatemc.anticheat.manager.ResetManager;
import com.elevatemc.anticheat.manager.TickManager;
import com.elevatemc.anticheat.util.cache.CacheUtil;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public enum Sosa {

    INSTANCE;

    private SosaPlugin plugin;
    private final TickManager tickManager = new TickManager();
    private final ResetManager resetManager = new ResetManager();
    private final PlayerDataManager playerDataManager = new PlayerDataManager();
    private final AlertManager alertManager = new AlertManager();
    private final PluginManager pluginManager = Bukkit.getPluginManager();
    private final PunishmentManager punishmentManager = new PunishmentManager();
    private final ReceivingPacketProcessor receivingPacketProcessor = new ReceivingPacketProcessor();
    private final SendingPacketProcessor sendingPacketProcessor = new SendingPacketProcessor();
    private final ExecutorService packetExecutor = Executors.newSingleThreadExecutor();

    private final CommandManager commandManager = new CommandManager();

    public void start(final SosaPlugin plugin) {
        this.plugin = plugin;

        assert plugin != null : "Error while starting Sosa!";

        getPlugin().saveDefaultConfig();

        getPlugin().getCommand("sosa").setExecutor(new SosaCommand());
        getPlugin().getCommand("forceban").setExecutor(new ForcebanCommand());
        getPlugin().getCommand("banwave").setExecutor(new BanwaveCommand());

        Config.updateConfig();

        CheckManager.setup();

        tickManager.start();
        resetManager.start();

        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, "MC|Brand", new ClientBrandListener());

        pluginManager.registerEvents(new PlayerListener(), plugin);
        pluginManager.registerEvents(new EventProcessor(), plugin);
        pluginManager.registerEvents(new WebhookLogEvent(), plugin);

        if (Bukkit.getServer().getPluginManager().isPluginEnabled("crackshot")) pluginManager.registerEvents(new CrackshotListener(), plugin);

        PacketEvents.getAPI().getEventManager().registerListener(new NetworkManager(PacketListenerPriority.LOWEST));
        PacketEvents.getAPI().init();

        new BanwaveThread();
    }

    public void stop(final SosaPlugin plugin) {
        this.plugin = plugin;

        assert plugin != null : "Error while shutting down Sosa!";

        tickManager.stop();
        resetManager.stop();
        SosaPlugin.getInstance().getLogHandler().closeMongo();
    }

    public void reload() {
        Config.updateConfig();
        CacheUtil.resetConfigValues();
        CacheUtil.updateCheckValues();
        Sosa.INSTANCE.getPlugin().reloadConfig();
    }

    public boolean isShittingItself() {
        return ServerUtil.getTPS() < 19.0 || System.currentTimeMillis() - tickManager.getLastResponse() > 60L;
    }

    public SosaPlugin getPlugin() {
        return plugin;
    }
}
