package com.elevatemc.anticheat.check.impl.combat.autoclicker;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.google.common.collect.Lists;

import java.util.Deque;

@CheckInfo(name = "Auto Clicker", type = "H", description = "Checks for impossible CPS Spikes")
public class AutoClickerH extends Check {

    private final Deque<Long> samples = Lists.newLinkedList();
    private double lastCps = -1;

    public AutoClickerH(final PlayerData data) {
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

            if (samples.size() == 10) {
                final double cps = MathUtil.getCps(samples);

                if (lastCps > 0) {
                    final double difference = Math.abs(cps - lastCps);
                    final double average = (lastCps + cps) / 2;

                    final boolean invalid = average > 9.25D && difference > 2.8D;

                    if (invalid) {
                        if (increaseBuffer() > 15) {
                            multiplyBuffer(.5);
                            fail("average=" + average + " difference=" + difference);
                            staffAlert();
                        }
                    } else {
                        decreaseBufferBy(1);
                    }
                }

                lastCps = cps;
                samples.clear();
            }
        }
    }
}