package com.elevatemc.anticheat.check.impl.combat.pattern;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Pattern", type = "F", experimental = true, description = "Checks for preemptive aim")
public class PatternF extends Check {

    private double lastDeltaPitch = 0;

    public PatternF(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (hitTicks() < 4 && !data.getRotationProcessor().isZooming()) {

                float deltaYaw = MathUtil.distanceBetweenAngles(data.getRotationProcessor().getLastYaw(), data.getRotationProcessor().getYaw());
                float deltaPitch = MathUtil.distanceBetweenAngles(data.getRotationProcessor().getLastPitch(), data.getRotationProcessor().getPitch());

                double pitchAcceleration = Math.abs(this.lastDeltaPitch - deltaPitch);
                boolean preemptive = deltaYaw > 1.975f;

                if (preemptive) {
                    boolean flag = deltaPitch < this.lastDeltaPitch && deltaPitch < 0.0700001f && deltaPitch > 0.0015f;
                    if (flag) {
                        if (increaseBuffer() > 5.0) {
                            fail("dP=" + deltaPitch + " lDP=" + lastDeltaPitch + " sup=" + (deltaPitch + pitchAcceleration));
                            resetBuffer();
                        }
                    } else {
                        decreaseBufferBy(0.25);
                    }
                }
                this.lastDeltaPitch = deltaPitch;
            }
        }
    }
}
