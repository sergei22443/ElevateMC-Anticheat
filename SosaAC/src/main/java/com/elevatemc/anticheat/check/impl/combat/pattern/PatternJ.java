package com.elevatemc.anticheat.check.impl.combat.pattern;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Pattern", type = "J", description = "Invalid yaw and pitch changes")
public class PatternJ extends Check {
    public PatternJ(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (hitTicks() < 4) {
                double predictedPitch = data.getRotationProcessor().getPredictedPitch();
                double predictedYaw = data.getRotationProcessor().getPredictedYaw();

                boolean exempt = isExempt(ExemptType.DEAD, ExemptType.TELEPORT);

                if (predictedYaw > 1.0f && predictedPitch > 2.0 && !exempt) {
                    if (increaseBuffer() > 6) {
                        fail("predictedY=" + predictedYaw + " predictedP=" + predictedPitch);
                        staffAlert();
                        multiplyBuffer(.25);
                    }
                } else {
                    decreaseBufferBy(.55);
                }
            }
        }
    }
}

