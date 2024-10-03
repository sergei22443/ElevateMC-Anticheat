package com.elevatemc.anticheat.check.impl.combat.autoclicker;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.util.type.EvictingList;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Auto Clicker", type = "D", description = "Too low kurtosis")
public class AutoClickerD extends Check {

    private final EvictingList<Long> samples = new EvictingList<>(150);

    public AutoClickerD(final PlayerData data) {
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
                final double kurtosis = MathUtil.getKurtosis(samples) / 1000;
                final double cps = MathUtil.getCps(samples);
                if (kurtosis < 13) {
                    if (increaseBuffer() > 18) {
                        fail("kurtosis=" + kurtosis + " cps=" + cps);
                        multiplyBuffer(.15);
                        staffAlert();
                    }
                } else {
                    decreaseBufferBy(.75);
                }
            }
        }
    }
}
