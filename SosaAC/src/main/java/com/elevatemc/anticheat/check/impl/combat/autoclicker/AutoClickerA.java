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

@CheckInfo(name = "Auto Clicker", type = "A", description = "Checks if a player is clicking over 20 CPS")
public class AutoClickerA extends Check {

    private final Deque<Long> samples = Lists.newLinkedList();

    public AutoClickerA(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent packet) {

        if (packet.getPacketType() == PacketType.Play.Client.ANIMATION) {
            final boolean exempt = isExempt(ExemptType.AUTOCLICKER);

            check: {
                if (exempt) break check;

                final long delay = data.getClickProcessor().getDelay();

                samples.add(delay);

                if (samples.size() == 20) {
                    final double cps = MathUtil.getCps(samples);

                    if (cps > 19.0) {
                        fail("cps=" + cps);
                    }
                    samples.clear();
                }
            }
        }
    }
}
