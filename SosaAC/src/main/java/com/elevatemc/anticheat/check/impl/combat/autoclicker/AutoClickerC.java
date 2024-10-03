package com.elevatemc.anticheat.check.impl.combat.autoclicker;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

@CheckInfo(name = "Auto Clicker", type = "C", description = "Checks for impossible consistency")
public class AutoClickerC extends Check {

    private final List<Long> samples = Lists.newArrayList();

    public AutoClickerC(final PlayerData data) {
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
                Collections.sort(samples);
                final double cps = MathUtil.getCps(samples);

                final long range = samples.get(samples.size() - 1) - samples.get(0);

                if (range < 50) {
                    if (increaseBuffer() > 3) {
                        fail("range=" + range + " cps=" + cps);
                        staffAlert();
                        multiplyBuffer(.25);
                    }
                } else {
                    decreaseBufferBy(.75);
                }
                samples.clear();
            }
        }
    }
}
