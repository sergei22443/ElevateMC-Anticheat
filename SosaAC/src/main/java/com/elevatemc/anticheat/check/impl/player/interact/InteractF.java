package com.elevatemc.anticheat.check.impl.player.interact;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.api.CheckInfo;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

// Taken from Wizzard.
@CheckInfo(name = "Interact", type = "F", description = "Invalid aim heuristics.")
public class InteractF extends Check {

    private double previousPitchSquaredInversed;

    public InteractF(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            float deltaYaw = data.getRotationProcessor().getDeltaYaw() % 180;
            float deltaPitch = data.getRotationProcessor().getDeltaPitch() % 180;

            float lastDeltaYaw = data.getRotationProcessor().getLastDeltaYaw() % 180;
            float lastDeltaPitch = data.getRotationProcessor().getLastDeltaPitch() % 180;

            float yAcceleration = Math.abs(lastDeltaYaw - deltaYaw);
            float pAcceleration = Math.abs(lastDeltaPitch - deltaPitch);

            double pitchSquaredInverse = MathUtil.invSqrt(pAcceleration);
            double yawSquaredInverse = MathUtil.invSqrt(yAcceleration);

            if (deltaYaw > 0.1F && deltaYaw < 20.0F && deltaPitch > 0.1F && deltaPitch < 20.0F) {

                if (pitchSquaredInverse != yawSquaredInverse && deltaYaw > 1.0f && data.getRotationProcessor().getYaw() < 80) {
                    if (pitchSquaredInverse == previousPitchSquaredInversed && hitTicks() < 10 && data.getSensitivityHolder().hasValidSensitivity()) {
                        if (increaseBuffer() > 8.0) {
                            fail("p=" + pitchSquaredInverse + " y=" + yawSquaredInverse + " ticks=" + hitTicks());
                        }
                    } else {
                        resetBuffer();
                    }
                }
            }
            previousPitchSquaredInversed = pitchSquaredInverse;
        }
    }
}
