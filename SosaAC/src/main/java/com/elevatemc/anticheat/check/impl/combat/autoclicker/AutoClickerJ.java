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

@CheckInfo(name = "Auto Clicker", type = "J", description = "Checks for too low standard deviation")
public class AutoClickerJ extends Check {

    protected final List<Integer> delays = new ArrayList<>(125);
    private int movements;

    public AutoClickerJ(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent packet) {
        if (packet.getPacketType() == PacketType.Play.Client.ANIMATION && !isExempt(ExemptType.AUTOCLICKER)) {
            if (movements < 10) {
                delays.add(movements);

                if (delays.size() == 125) {
                    double cps = data.getClickProcessor().getCps();
                    double stDev = MathUtil.getStDev(delays);

                    if (stDev < 0.45) {
                        if (increaseBuffer() > 4) {
                            fail("SD=" + stDev + " cps=" + new DecimalFormat(".###").format(cps));
                        }
                    } else {
                        decreaseBuffer();
                    }
                    delays.clear();
                }
            }
            movements = 0;
        } else if (WrapperPlayClientPlayerFlying.isFlying(packet.getPacketType())) {
            ++movements;
        }
    }
}