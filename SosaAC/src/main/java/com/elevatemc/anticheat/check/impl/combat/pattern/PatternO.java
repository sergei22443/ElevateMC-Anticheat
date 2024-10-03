package com.elevatemc.anticheat.check.impl.combat.pattern;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Pattern", type = "O", experimental = true, description = "Checks for invalid head deceleration")
public class PatternO extends Check {

    public PatternO(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent packet) {
        if (packet.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || packet.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {

            float deltaYaw = data.getRotationProcessor().getDeltaYaw();

            double acceleration = data.getPositionProcessor().getAcceleration();
            double deltaXZ = data.getPositionProcessor().getDeltaXZ();

            boolean validAngle = deltaYaw > 1.5F;
            boolean invalidAccel = (acceleration * 100) < 1.0E-5 && deltaXZ > .15;

            if (validAngle && invalidAccel) {
                if (increaseBuffer() > 4.5){
                    fail("");
                    resetBuffer();
                }
            }
        }
    }
}
