package com.elevatemc.anticheat.check.impl.combat.pattern;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Pattern", type = "P", description = "Checks for invalid ticks over rotation")
public class PatternP extends Check {

    int positivePitchTicks, negativePitchTicks, lastPositivePitchTicks, lastNegativePitchTicks;
    long lastSwitch;

    public PatternP(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent packet) {
        if (packet.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || packet.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            float deltaPitch = data.getRotationProcessor().getDeltaPitch();

            if (deltaPitch < 0.0f) {
                ++negativePitchTicks;
                positivePitchTicks = 0;
            } else if (deltaPitch > 0.0f) {
                ++positivePitchTicks;
                negativePitchTicks = 0;
            }

            boolean switchedDirection = (lastPositivePitchTicks != 1 && positivePitchTicks == 1) || (lastNegativePitchTicks != 1 && negativePitchTicks == 1);

            if (hitTicks() < 3 && switchedDirection && Math.abs(deltaPitch) > 6.5) {
                long switchTime = now();
                long delay = switchTime - lastSwitch;

                if (delay < 85L) {
                    if (increaseBuffer() > 7.0) {
                        fail("delay=" + delay + " deltaPitch=" + deltaPitch);
                    }
                } else {
                    decreaseBuffer();
                }
                lastSwitch = switchTime;
            }

            lastNegativePitchTicks = negativePitchTicks;
            lastPositivePitchTicks = positivePitchTicks;
        }
    }
}
