package com.elevatemc.api;

import com.elevatemc.anticheat.check.Check;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SosaPunishEvent extends Event implements Cancellable {

    private final Player player;
    private final double vl;
    private final Check check;

    private final String name, type;
    private static final HandlerList handlers;

    public SosaPunishEvent(final Player player, final Check check, String name, String type, final double vl) {
        this.player = player;
        this.check = check;
        this.name = name;
        this.type = type;
        this.vl = vl;
    }

    public boolean isCancelled() {
        return false;
    }

    public void setCancelled(boolean b) {
    }

    public Player getPunishedPlayer() {
        return this.player;
    }

    public double getVl() {
        return this.vl;
    }

    public Check getCheck() {
        return this.check;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public long getTimestamp() {
        return System.currentTimeMillis();
    }

    public HandlerList getHandlers() {
        return SosaPunishEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return SosaPunishEvent.handlers;
    }

    static {
        handlers = new HandlerList();
    }
}

