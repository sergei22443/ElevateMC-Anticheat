package com.elevatemc.anticheat.check.impl.combat.pattern;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Pattern", type = "E", description = "Checks for invalid aim patterns")
public class PatternE extends Check {

    public PatternE(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (hitTicks() < 4) {
                if (!data.getSensitivityHolder().hasValidSensitivity()) return;

                float deltaYaw = data.getRotationProcessor().getDeltaYaw();
                float lastDeltaYaw = data.getRotationProcessor().getLastDeltaYaw();
                float deltaPitch = data.getRotationProcessor().getDeltaPitch();

                boolean invalid = MathUtil.isScientificNotation(deltaYaw) && MathUtil.isScientificNotation(lastDeltaYaw) && deltaPitch > 0.5;

                if (invalid) {
                    if (increaseBuffer() > 4.0) {
                        fail("y=" + deltaYaw + " pitch=" + deltaPitch + " lDY=" + lastDeltaYaw);
                        staffAlert();
                    }
                }
            }
        }
    }
}
