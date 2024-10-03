package com.elevatemc.anticheat.check.impl.combat.pattern;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Pattern", type = "C", description = "Too small rotation on pitch while yaw axis has a large one")
public class PatternC extends Check {

    public PatternC(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (hitTicks() < 4 && !data.getRotationProcessor().isZooming()) {

                float deltaPitch = data.getRotationProcessor().getDeltaPitch();
                float lastDeltaPitch = data.getRotationProcessor().getLastDeltaPitch();
                float deltaYaw = data.getRotationProcessor().getDeltaYaw();

                boolean invalid = MathUtil.isScientificNotation(deltaPitch) && MathUtil.isScientificNotation(lastDeltaPitch) && deltaYaw > 0.5;

                if (invalid) {
                    fail("dYaw=" + deltaYaw + " dPitch=" + deltaPitch);
                    staffAlert();
                }
            }
        }
    }
}
