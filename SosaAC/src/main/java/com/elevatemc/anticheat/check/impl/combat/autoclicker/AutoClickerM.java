package com.elevatemc.anticheat.check.impl.combat.autoclicker;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@CheckInfo(name = "Auto Clicker", type = "M", description = "Invalid difference between deviation")
public class AutoClickerM extends Check {

    protected final List<Integer> delays = new ArrayList<>(125);
    private int movements;

    double lastDev;

    public AutoClickerM(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent packet) {
        if (packet.getPacketType() == PacketType.Play.Client.ANIMATION && !isExempt(ExemptType.TELEPORT)) {
            if (movements < 10) {
                delays.add(movements);

                if (delays.size() == 125) {
                    double cps = data.getClickProcessor().getCps();
                    double stDev = MathUtil.getStDev(delays);

                    if (Math.abs(stDev - lastDev) < 0.05) {
                        if (increaseBuffer() > 12) {
                            fail("SD=" + stDev + " cps=" + new DecimalFormat(".###").format(cps));
                            decreaseBufferBy(4);
                        }
                    } else {
                        decreaseBufferBy(0.15);
                    }
                    lastDev = stDev;
                    delays.clear();
                }
            }

            movements = 0;
        } else if (WrapperPlayClientPlayerFlying.isFlying(packet.getPacketType())) {
            ++movements;
        }
    }
}