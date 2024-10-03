package com.elevatemc.anticheat.data.processor.player;

import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.util.type.EvictingList;
import lombok.Getter;
import lombok.Setter;

@Getter

public class ClickProcessor
{
    private final PlayerData data;
    private long lastSwing;
    private long delay;
    @Setter
    private double cps;
    private EvictingList<Long> samples = new EvictingList<>(20);
    public ClickProcessor(final PlayerData data) {
        this.data = data;
    }

    public void handleArmAnimation() {
        final boolean exempt = data.getExemptProcessor().isExempt(ExemptType.AUTOCLICKER);
        if (!exempt) {
            final long now = System.currentTimeMillis();
            this.delay = now - this.lastSwing;
            this.lastSwing = now;

            samples.add(delay);

            if (samples.isFull()) {
                this.cps = MathUtil.getCps(samples);
                samples.clear();
            }
        }
    }

    public PlayerData getData() {
        return this.data;
    }

    public double getCps() {
        return cps;
    }

    public long getLastSwing() {
        return this.lastSwing;
    }

    public long getDelay() {
        return this.delay;
    }

}

