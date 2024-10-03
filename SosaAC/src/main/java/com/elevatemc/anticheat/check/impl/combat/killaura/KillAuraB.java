package com.elevatemc.anticheat.check.impl.combat.killaura;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Kill Aura", type = "B", description = "Invalid acceleration.")
public class KillAuraB extends Check {

    public KillAuraB(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (hitTicks() < 3) {
                double acceleration = data.getPositionProcessor().getAcceleration();
                double deltaXZ = data.getPositionProcessor().getDeltaXZ();

                float deltaYaw = data.getRotationProcessor().getDeltaYaw();
                float deltaPitch = data.getRotationProcessor().getDeltaPitch();

                boolean invalid = acceleration < .001 && deltaYaw > 10 && deltaPitch > 26.5D && deltaXZ > 0;

                if (invalid) {
                    if (increaseBuffer() > 3) {
                        multiplyBuffer(.25);
                        fail("accel=" + acceleration + " deltaYaw=" + deltaYaw + " deltaPitch=" + deltaPitch);
                        increaseVlBy(.32);
                        staffAlert();
                    }
                } else {
                    decreaseBufferBy(.35);
                }
            }
        }
    }
}
