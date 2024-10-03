package com.elevatemc.anticheat.check.impl.combat.pattern;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Pattern", type = "L", description = "Checks if the acceleration doesn't match the rotation values")
public class PatternL extends Check {

    public PatternL(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {

            float deltaYaw = data.getRotationProcessor().getDeltaYaw();
            float deltaPitch = data.getRotationProcessor().getDeltaPitch();

            double acceleration = data.getPositionProcessor().getAcceleration();

            boolean invalid = MathUtil.isScientificNotation(acceleration) && deltaYaw > 10.0f && deltaPitch > 24.5D;

            if (invalid) {
                if (increaseBuffer() > 2) {
                    fail("dY=" + deltaYaw + " dP=" + deltaPitch + " accel=" + acceleration);
                    resetBuffer();
                }
            }
        }
    }
}
