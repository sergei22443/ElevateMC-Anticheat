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

@CheckInfo(name = "Auto Clicker", type = "F", description = "Checks for amount of distinct delays")
public class AutoClickerF extends Check {
    private final EvictingList<Long> samples = new EvictingList<>(25);

    public AutoClickerF(final PlayerData data) {
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
                final int distinct = MathUtil.getDistinct(samples);
                final double cps = MathUtil.getCps(samples);
                if (distinct < 6) {
                    if (increaseBuffer() > 25) {
                        multiplyBuffer(.20);
                        fail("distinct=" + distinct + " cps=" + cps);
                        staffAlert();
                    }
                } else {
                    decreaseBufferBy(.75);
                }
            }
        }
    }
}
