package com.elevatemc.anticheat.check.impl.combat.aim;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Aim", type = "I", description = "Checks for invalid modulo changes")
public class AimI extends Check {

    private boolean targetSet = false;

    public AimI(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (hitTicks() < 4 && !data.getRotationProcessor().isZooming() && data.getSensitivityHolder().hasValidSensitivity()) {
                float deltaPitch = MathUtil.getDistanceBetweenAngles(data.getRotationProcessor().getPitch(), data.getRotationProcessor().getLastPitch());
                float deltaYaw = MathUtil.getDistanceBetweenAngles(data.getRotationProcessor().getYaw(), data.getRotationProcessor().getLastYaw());

                double modulo = deltaYaw % deltaPitch;

                if (!targetSet) return;

                boolean invalid = (Double.isNaN(modulo) && deltaYaw > 1.65 && deltaPitch > 0.0f) || (Double.isNaN(modulo) && deltaYaw < 0.065 && deltaYaw > 0.0f);

                if (invalid) {
                    if (increaseBuffer() > 12.0) {
                        fail("modulo=" + modulo + " dY=" + deltaYaw + " dP=" + deltaPitch);
                        increaseVlBy(.15);
                        multiplyBuffer(.5);
                    }
                } else {
                    decreaseBufferBy(1.5);
                }
            }
        } else if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            targetSet = true;
        }
    }
}
