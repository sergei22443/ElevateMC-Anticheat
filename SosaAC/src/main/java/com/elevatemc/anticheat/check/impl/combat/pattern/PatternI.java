package com.elevatemc.anticheat.check.impl.combat.pattern;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.util.type.EvictingList;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Pattern", type = "I", description = "Invalid head acceleration")
public class PatternI extends Check {

    private final EvictingList<Float> yawAcceleration = new EvictingList<>(60);
    private final EvictingList<Float> pitchAcceleration = new EvictingList<>(60);
    public PatternI(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (!data.getRotationProcessor().isZooming()) {

                float deltaYaw = data.getRotationProcessor().getDeltaYaw();
                float deltaPitch = data.getRotationProcessor().getDeltaPitch();

                float joltY = data.getRotationProcessor().getYawAcceleration();
                float joltP = data.getRotationProcessor().getPitchAcceleration();

                if (deltaYaw != 0.0f && deltaPitch != 0.0f && deltaYaw < 20.0f && deltaPitch < 20.0f) {

                    yawAcceleration.add(joltY);
                    pitchAcceleration.add(joltP);

                    if (yawAcceleration.isFull() && pitchAcceleration.isFull()) {

                        double averageYaw = MathUtil.getAverage(yawAcceleration);
                        double averagePitch = MathUtil.getAverage(pitchAcceleration);

                        if (averageYaw > 3.5 && averagePitch > 6.5) {
                            if (increaseBuffer() > 6.0) {
                                fail("avgY=" + averageYaw + " avgP=" + averagePitch);
                            }
                        } else {
                            decreaseBufferBy(.15);
                        }

                        yawAcceleration.clear();
                        pitchAcceleration.clear();
                    }
                }
            }
        }
    }
}
