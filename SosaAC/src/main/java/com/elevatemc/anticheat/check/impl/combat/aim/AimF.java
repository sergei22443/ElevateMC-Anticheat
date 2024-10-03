package com.elevatemc.anticheat.check.impl.combat.aim;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Aim", type = "F", description = "Checks for invalid resolution (gcd)")
public class AimF extends Check {

    public AimF(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (hitTicks() < 4 && !data.getRotationProcessor().isZooming()) {
                float deltaYaw = data.getRotationProcessor().getDeltaYaw();
                float deltaPitch = data.getRotationProcessor().getDeltaPitch();

                float lastDeltaYaw = data.getRotationProcessor().getLastDeltaYaw();
                float lastDeltaPitch = data.getRotationProcessor().getLastDeltaPitch();

                float yAccel = Math.abs(deltaYaw - lastDeltaYaw);
                float pAccel = Math.abs(deltaPitch - lastDeltaPitch);

                if (yAccel < 0.1 || pAccel < 0.1) return;

                if (deltaYaw < 0.35 || deltaPitch < 0.35 || deltaYaw > 12.5 || deltaPitch > 12.5) return;

                double gcd = MathUtil.getGcd(deltaPitch, lastDeltaPitch);

                final boolean exempt = isExempt(ExemptType.TELEPORT, ExemptType.JOINED);

                if (gcd < 0.007 && !exempt) {
                    if (increaseBuffer() > 13.0) {
                        fail("deltaYaw=" + deltaYaw + " deltaPitch=" + deltaPitch);
                        staffAlert();
                    }
                } else {
                    decreaseBufferBy(1.25);
                }
            }
        }
    }
}
