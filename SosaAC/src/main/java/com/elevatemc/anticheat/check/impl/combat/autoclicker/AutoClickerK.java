package com.elevatemc.anticheat.check.impl.combat.autoclicker;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.util.type.EvictingList;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Auto Clicker", type = "K", description = "Too low variance difference")
public class AutoClickerK extends Check {

    EvictingList<Long> samples = new EvictingList<>(100);

    public AutoClickerK(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent packet) {
        if (packet.getPacketType()== PacketType.Play.Client.ANIMATION && !isExempt(ExemptType.AUTOCLICKER)) {
            final long delay = data.getClickProcessor().getDelay();

            if (delay > 5000) {
                samples.clear();
            }
            samples.add(delay);

            if (samples.size() == 100) {
                final double variance = MathUtil.getVariance(samples) / 100.0;

                if (variance < 105.0) {
                    if (increaseBuffer() > 4.0) {
                        fail("var=" + variance);
                    }
                } else {
                    decreaseBufferBy(.75);
                }
                samples.clear();
            }
        }
    }
}