package com.elevatemc.anticheat.check.impl.combat.aim;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Aim", type = "A",description = "Not constant rotations [GCD].")
public class AimA extends Check {

    public AimA(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (hitTicks() < 4 && !data.getRotationProcessor().isZooming()) {
                float deltaPitch = data.getRotationProcessor().getDeltaPitch();
                float lastDeltaPitch = data.getRotationProcessor().getLastDeltaPitch();

                float deltaYaw = data.getRotationProcessor().getDeltaYaw();

                long expandedPitch = (long) (MathUtil.EXPANDER * deltaPitch);
                long expandedLastPitch = (long) (MathUtil.EXPANDER * lastDeltaPitch);

                long gcd = MathUtil.getGcd(expandedPitch, expandedLastPitch);

                boolean validAngles = deltaYaw > .1F && deltaPitch > .1F && deltaPitch < 20F && deltaYaw < 20F;
                boolean invalid = gcd < 131072L;

                if (invalid && validAngles) {
                    if (increaseBuffer() > 11) {
                        staffAlert();
                        fail("deltaPitch=" + deltaPitch + " deltaYaw=" + deltaYaw + " rotation=" + gcd / 1000);
                        multiplyBuffer(.35);
                    }
                } else {
                    decreaseBufferBy(1.25);
                }
            }
        }
    }
}
