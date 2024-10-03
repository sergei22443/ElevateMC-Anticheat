package com.elevatemc.anticheat.check;

import com.elevatemc.anticheat.util.server.ServerUtil;
import com.elevatemc.anticheat.Sosa;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import lombok.Getter;
import lombok.Setter;
import java.util.*;

@Getter
public abstract class Check {

    protected final PlayerData data;

    @Setter
    private double vl;

    private double buffer;

    public Check(final PlayerData data) {
        this.data = data;
    }

    public abstract void onPacketReceive(final PacketReceiveEvent packet);
    public void onPacketSend(final PacketSendEvent packet) {}


    public void fail(final Object info) {
        Sosa.INSTANCE.getAlertManager().handleAlert(this, data, Objects.toString(info));
    }

    public void staffAlert() {
        Sosa.INSTANCE.getAlertManager().staffAlert(this, data);
    }

    protected boolean isExempt(final ExemptType exemptType) {
        return data.getExemptProcessor().isExempt(exemptType);
    }

    protected boolean isExempt(final ExemptType... exemptTypes) {
        return data.getExemptProcessor().isExempt(exemptTypes);
    }

    public long now() {
        return System.currentTimeMillis();
    }

    public int ticks() {
        return Sosa.INSTANCE.getTickManager().getTicks();
    }

    public double increaseBuffer() {
        return buffer = Math.min(10000, buffer + 1);
    }

    public void decreaseBuffer() {
        buffer = Math.max(0, buffer - 1);
    }

    public double decreaseBufferBy(final double amount) {
        return buffer = Math.max(0, buffer - amount);
    }

    public int hitTicks() {
        return data.getCombatProcessor().getHitTicks();
    }

    public void resetBuffer() {
        buffer = 0;
    }

    public void increaseVlBy(final double amount) {
        vl = Math.min(10000, vl + amount);
    }

    public void decreaseVlBy(final double amount) {
         vl = Math.max(0, vl - amount);
    }

    public void multiplyBuffer(final double multiplier) {
        buffer *= multiplier;
    }

    public CheckInfo getCheckInfo() {
        if (getClass().isAnnotationPresent(CheckInfo.class)) {
            return getClass().getAnnotation(CheckInfo.class);
        } else {
            System.err.println("CheckInfo annotation hasn't been added to the class " + getClass().getSimpleName() + ".");
        }
        return null;
    }

    public void debug(final Object object) {
        ServerUtil.broadcast("&b[Sosa-Debug] &f" + object);
    }

    public String getCategory() {
        String category = "";
        if (getClass().getName().contains("combat")) {
            category = "combat";
        } else if (getClass().getName().contains("movement")) {
            category = "movement";
        } else if (getClass().getName().contains("player")) {
            category = "player";
        }
        return category;
    }



    public String getName() {
        return getCheckInfo().name();
    }

    public String getType() {
        return getCheckInfo().type();
    }

}
