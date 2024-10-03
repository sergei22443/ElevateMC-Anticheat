package com.elevatemc.anticheat.check.impl.combat.autoclicker;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.util.type.EvictingList;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Auto Clicker", type = "E", description = "Too low average deviation")
public class AutoClickerE extends Check {
    private final EvictingList<Long> samples = new EvictingList<>(40);
    private double lastDeviation;

    public AutoClickerE(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent packet) {
        if (packet.getPacketType() == PacketType.Play.Client.ANIMATION && !isExempt(ExemptType.AUTOCLICKER)) {
            final long delay = data.getClickProcessor().getDelay();

            if (delay > 5000) {
                samples.clear();
                return;
            }

            samples.add(delay);

            if (samples.isFull()) {
                final double deviation = MathUtil.getStandardDeviation(samples);
                final double difference = Math.abs(deviation - lastDeviation);
                final double cps = MathUtil.getCps(samples);
                final double average = Math.abs(deviation + lastDeviation) / 2;

                if (difference < 3 && average < 150) {
                    if (increaseBuffer() > 30) {
                        fail("difference=" + difference + " average=" + average + " cps=" + cps);
                        staffAlert();
                        multiplyBuffer(.33);
                    }
                } else {
                    decreaseBufferBy(2);
                }
                lastDeviation = deviation;
            }
        }
    }
}
