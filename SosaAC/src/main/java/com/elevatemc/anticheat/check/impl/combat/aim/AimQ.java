package com.elevatemc.anticheat.check.impl.combat.aim;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

/*
    Taken from GladUrBad (Ares AntiCheat)
 */
@CheckInfo(name = "Aim", type = "Q", description = "Checks for small pitch rotations")
public class AimQ extends Check {

    public AimQ(PlayerData data) {
        super(data);
    }


    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (hitTicks() < 4 && !data.getRotationProcessor().isZooming()) {
                float yaw = data.getRotationProcessor().getYaw();
                float pitch = data.getRotationProcessor().getPitch();

                float deltaYaw = data.getRotationProcessor().getDeltaYaw();

                float deltaPitch = data.getRotationProcessor().getDeltaPitch();
                float lastDeltaPitch = data.getRotationProcessor().getLastDeltaPitch();

                double divisor = MathUtil.getGcd(deltaPitch, lastDeltaPitch);

                if (divisor < 0.0078125F) return;

                double deltaX = deltaYaw / divisor;
                double deltaY = deltaPitch / divisor;

                boolean properX = Math.abs(Math.round(deltaX) - deltaX) < 0.0001D;
                boolean properY = Math.abs(Math.round(deltaY) - deltaY) < 0.0001D;

                if (!properX || !properY || (hitTicks() > 10)) return;

                double diffX = Math.abs(yaw - (yaw - (yaw % divisor)));
                double diffY = Math.abs(pitch - (pitch - (pitch % divisor)));

                // 0.0001 = 1e-4 = 1 * 10^(-4)
                if (diffX < 1e-4 && diffY < 1e-4 && !isExempt(ExemptType.TELEPORT)) {
                    if (increaseBuffer() > 12) {
                        fail("X=" + diffX + " Y=" + diffY);
                    }
                } else {
                    decreaseBufferBy(1.25);
                }
            }
        }
    }
}
