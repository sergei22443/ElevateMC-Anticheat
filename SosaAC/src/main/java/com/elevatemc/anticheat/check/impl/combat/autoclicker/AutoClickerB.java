package com.elevatemc.anticheat.check.impl.combat.autoclicker;

import com.elevatemc.anticheat.SosaPlugin;
import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.util.type.EvictingList;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.bukkit.Bukkit;

@CheckInfo(name = "Auto Clicker", type = "B", description = "Checks for too low deviation")
public class AutoClickerB extends Check {

    private final EvictingList<Long> samples = new EvictingList<>(100);

    public AutoClickerB(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent packet) {
        if (packet.getPacketType() == PacketType.Play.Client.ANIMATION && !isExempt(ExemptType.AUTOCLICKER)) {

            final long delay = data.getClickProcessor().getDelay();

            if (delay > 5000) {
                samples.clear();
                return;
            }

            samples.add(delay);

            if (samples.isFull()) {
                final double deviation = MathUtil.getStandardDeviation(samples);
                final double cps = MathUtil.getCps(samples);

                if (deviation < 130) {
                    if (increaseBuffer() > 23) {
                        fail("deviation=" + deviation + " cps=" + cps);
                        multiplyBuffer(.25);
                    }
                } else {
                    decreaseBuffer();
                }
            }
        }
    }
}
