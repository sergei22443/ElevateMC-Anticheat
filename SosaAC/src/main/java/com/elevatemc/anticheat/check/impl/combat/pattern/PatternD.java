package com.elevatemc.anticheat.check.impl.combat.pattern;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Pattern", type = "D", description = "Invalid yaw rotations")
public class PatternD extends Check {

    public PatternD(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (!data.getRotationProcessor().isZooming() && hitTicks() < 4) {
                float deltaYaw = data.getRotationProcessor().getDeltaYaw();

                if (deltaYaw == 0.0f || deltaYaw == 360.0) {
                    return;
                }

                boolean invalid = deltaYaw % 0.25 == 0.0;
                boolean exempt = isExempt(ExemptType.JOINED, ExemptType.TELEPORT);

                if (invalid && !exempt) {
                    if (increaseBuffer() > 8.0) {
                        fail("deltaYaw=" + deltaYaw + " lastDY=" + data.getRotationProcessor().getLastDeltaYaw());
                        multiplyBuffer(.5);
                        staffAlert();
                    }
                } else {
                    decreaseBufferBy(.75);
                }
            }
        }
    }
}
