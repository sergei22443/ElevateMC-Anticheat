package com.elevatemc.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SosaJudgementDayAddEvent extends Event implements Cancellable {

    private final Player player;

    private final String name;
    private static final HandlerList handlers;

    public SosaJudgementDayAddEvent(final Player player, final String name) {
        this.player = player;
        this.name = name;
    }

    public boolean isCancelled() {
        return false;
    }

    public void setCancelled(boolean b) {
    }

    public Player getPlayer() {
        return this.player;
    }


    public String getReason() {
        return name;
    }


    public long getTimestamp() {
        return System.currentTimeMillis();
    }

    public HandlerList getHandlers() {
        return SosaJudgementDayAddEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return SosaJudgementDayAddEvent.handlers;
    }

    static {
        handlers = new HandlerList();
    }
}
