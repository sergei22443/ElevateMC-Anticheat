package com.elevatemc.anticheat.check.impl.combat.aim;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.util.type.EvictingList;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Aim", type = "L", experimental = true, description = "Checks for precise aim")
public class AimL extends Check {

    EvictingList<Double> moduloSamples = new EvictingList<>(20);
    EvictingList<Float> pitchSamples = new EvictingList<>(20);
    public AimL(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (hitTicks() < 5 && !data.getRotationProcessor().isZooming()) {

                float deltaYaw = data.getRotationProcessor().getDeltaYaw();
                float deltaPitch = data.getRotationProcessor().getDeltaPitch();

                float lastDeltaYaw = data.getRotationProcessor().getLastDeltaYaw();

                double divisorYaw = MathUtil.getGcd((long) (deltaYaw * MathUtil.EXPANDER), (long) (lastDeltaYaw * MathUtil.EXPANDER));
                double constantYaw = divisorYaw / MathUtil.EXPANDER;

                double currentX = deltaYaw / constantYaw;
                double previousX = lastDeltaYaw / constantYaw;

                if (deltaYaw > 0.0 && deltaPitch > 0.0 && deltaYaw < 20.f && deltaPitch < 20.f) {
                    double moduloX = currentX % previousX;
                    double floorModuloX = Math.abs(Math.floor(moduloX) - moduloX);

                    moduloSamples.add(floorModuloX);
                    pitchSamples.add(deltaPitch);

                    if (moduloSamples.isFull() && pitchSamples.isFull()) {
                        double deviation = MathUtil.getStandardDeviation(moduloSamples);

                        int duplicates = MathUtil.getDuplicates(moduloSamples);
                        int pitchDuplicates = MathUtil.getDuplicates(pitchSamples);
                        int combinedDuplicates = duplicates + pitchDuplicates;

                        if(Double.isNaN(deviation) && combinedDuplicates <= 5) {
                            if (increaseBuffer() > 7) {
                                fail("dev=NaN dup=" + combinedDuplicates);
                                multiplyBuffer(.35);
                            }
                        } else {
                           decreaseBuffer();
                        }
                        moduloSamples.clear();
                        pitchSamples.clear();
                    }
                }
            }
        }
    }
}