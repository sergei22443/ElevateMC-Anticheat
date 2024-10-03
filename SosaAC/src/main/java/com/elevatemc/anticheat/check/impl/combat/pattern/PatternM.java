package com.elevatemc.anticheat.check.impl.combat.pattern;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Pattern", type = "M", description = "Checks for too small yaw change")
public class PatternM extends Check {

    public PatternM(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {

            if (!data.getRotationProcessor().isZooming()) {
                float deltaYaw = data.getRotationProcessor().getDeltaYaw();
                float deltaPitch = data.getRotationProcessor().getDeltaPitch();

                boolean invalid = deltaYaw < .0001 && deltaYaw > 0 && deltaPitch > .5F;

                if (invalid) {
                    if (increaseBuffer() > 6.0) {
                        fail("deltaPitch=" + deltaPitch + " deltaYaw=" + deltaYaw);
                        staffAlert();
                        increaseVlBy(1.25);
                        multiplyBuffer(.25);
                    }
                } else {
                    decreaseBufferBy(.25);
                }
            }
        }
    }
}
