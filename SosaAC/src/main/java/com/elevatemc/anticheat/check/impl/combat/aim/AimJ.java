package com.elevatemc.anticheat.check.impl.combat.aim;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Aim", type = "J", description = "Checks for improper yaw rotations")
public class AimJ extends Check {
    public AimJ(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (hitTicks() < 5 && !data.getRotationProcessor().isZooming()) {

                boolean exempt = isExempt(ExemptType.JOINED, ExemptType.TELEPORT);

                float deltaYaw = data.getRotationProcessor().getDeltaYaw(), deltaPitch = data.getRotationProcessor().getDeltaPitch();
                float lastDeltaYaw = data.getRotationProcessor().getLastDeltaYaw(), lastDeltaPitch = data.getRotationProcessor().getLastDeltaPitch();

                if (deltaYaw < 1.0F && deltaPitch < 1.0F) return;

                double divisorX = MathUtil.getGcd(deltaYaw, lastDeltaYaw);
                double divisorY = MathUtil.getGcd(deltaPitch, lastDeltaPitch);

                double maxDivisor = Math.max(divisorX, divisorY);

                // 131072 / 16777216
                boolean invalid = maxDivisor < 0.0078125F;

                if (invalid && !exempt) {
                    if (increaseBuffer() > 24) fail("divisor=" + maxDivisor);
                } else {
                    decreaseBufferBy(0.3D);
                }
            }
        }
    }
}
