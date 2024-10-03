package com.elevatemc.api;

import com.elevatemc.anticheat.check.Check;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SosaFlagEvent extends Event implements Cancellable {

    private final Player player;
    private final double vl;
    private final Check check;

    private String name, type;
    private static final HandlerList handlers;

    public SosaFlagEvent(final Player player, final Check check, final String name, final String type, final double vl) {
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

    public Player getPlayer() {
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
        return SosaFlagEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return SosaFlagEvent.handlers;
    }

    static {
        handlers = new HandlerList();
    }
}
