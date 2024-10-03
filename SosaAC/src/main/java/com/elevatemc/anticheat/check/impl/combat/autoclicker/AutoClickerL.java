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

@CheckInfo(name = "Auto Clicker", type = "L", description = "Invalid kurtosis")
public class AutoClickerL extends Check {

    protected final List<Integer> delays = new ArrayList<>(250);

    private int movements;

    public AutoClickerL(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent packet) {
        if (packet.getPacketType() == PacketType.Play.Client.ANIMATION && !isExempt(ExemptType.AUTOCLICKER)) {
            if (movements < 10) {
                delays.add(movements);

                if (delays.size() == 250) {
                    double kurtosis = MathUtil.kurtosis(delays);

                    if (kurtosis < 0D) {
                        if (increaseBuffer() > 2) {
                            fail("KU " + kurtosis + " cps=" + new DecimalFormat(".###").format(data.getClickProcessor().getCps()));
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