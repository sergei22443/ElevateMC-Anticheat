package com.elevatemc.anticheat.check.impl.player.timer;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.util.server.PlayerUtil;
import com.elevatemc.anticheat.util.type.EvictingList;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Timer", type = "B", description = "Checks for game speed")
public class TimerB extends Check {
    private final EvictingList<Long> samples = new EvictingList<>(50);
    private long lastFlying;

    public TimerB(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            long delay = now() - lastFlying;

            boolean exempt = isExempt(ExemptType.TPS, ExemptType.JOINED) || PlayerUtil.getPing(data.getPlayer()) > 530;

            add: {
                if (delay < 5 || exempt) break add;

                samples.add(delay);
            }

            if (samples.isFull()) {
                final double average = MathUtil.getAverage(samples);
                final double speed = 50 / average;

                if (speed > 1.02) {
                    if (increaseBuffer() > 35) {
                        fail("speed=" + speed * 100 + "%");
                        multiplyBuffer(.25);
                    }
                } else {
                    decreaseBufferBy(2.25);
                }
            }

            lastFlying = now();
        }
    }
    @Override
    public void onPacketSend(final PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
            samples.add(110L);
        }
    }
}
