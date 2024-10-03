package com.elevatemc.anticheat.check.impl.combat.pattern;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Pattern", type = "G", description = "Checks for aim patterns by predicting the yaw and pitch")
public class PatternG extends Check {

    float lastExpectedYaw, lastExpectedPitch;

    public PatternG(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (hitTicks() < 4) {

                if (data.getRotationProcessor().isZooming()) return;

                boolean exempt = isExempt(ExemptType.TELEPORT);

                if (!exempt) {
                    float deltaPitch = data.getRotationProcessor().getDeltaPitch();
                    float yawDiff = Math.abs(data.getRotationProcessor().getExpectedYaw() - lastExpectedYaw);
                    float pitchDiff = Math.abs(data.getRotationProcessor().getExpectedPitch() - lastExpectedPitch);

                    boolean invalid = pitchDiff == 0.0f && yawDiff == 0.0f && deltaPitch > 2.95;

                    if (invalid) {
                        if (increaseBuffer() > 2) fail("deltaPitch=" + deltaPitch);
                    } else {
                        decreaseBufferBy(0.005);
                    }
                }
                lastExpectedYaw = data.getRotationProcessor().getExpectedYaw();
                lastExpectedPitch = data.getRotationProcessor().getExpectedPitch();
            }
        }
    }
}
