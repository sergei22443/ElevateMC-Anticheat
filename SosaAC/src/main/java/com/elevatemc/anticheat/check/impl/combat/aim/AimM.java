package com.elevatemc.anticheat.check.impl.combat.aim;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.math.LinearRegression;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.google.common.collect.Lists;
import lombok.val;

import java.util.*;

//Credit goes to GladUrBad (Kyle) for the check! (Ares-Anticheat)
@CheckInfo(name = "Aim", type = "M", description = "Checks for unrealistic non-linear rotations")
public class AimM extends Check {

    private final List<Double> samplesYaw = Lists.newArrayList();
    private final List<Double> samplesPitch = Lists.newArrayList();

    public AimM(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (hitTicks() < 4 && !data.getRotationProcessor().isZooming()) {
                double deltaYaw = data.getRotationProcessor().getDeltaYaw();
                double deltaPitch = data.getRotationProcessor().getDeltaPitch();

                boolean attacking = hitTicks() < 4;

                handle:
                {
                    if (deltaYaw == 0.0 || deltaPitch == 0.0 || !attacking || data.getRotationProcessor().isZooming())
                        break handle;

                    samplesYaw.add(deltaYaw);
                    samplesPitch.add(deltaPitch);

                    if (samplesYaw.size() + samplesPitch.size() == 60) {

                        val outliersYaw = MathUtil.getOutliers(samplesYaw);
                        val outliersPitch = MathUtil.getOutliers(samplesPitch);

                        Double[] regressionX = new Double[samplesYaw.size()];
                        Double[] regressionY = new Double[samplesPitch.size()];

                        regressionX = samplesYaw.toArray(regressionX);
                        regressionY = samplesPitch.toArray(regressionY);

                        final LinearRegression regression = new LinearRegression(regressionX, regressionY);

                        int fails = 0;

                        for (int i = 0; i < 30; i++) {
                            double tempX = regressionX[i];
                            double tempY = regressionY[i];

                            double predicted = regression.predict(tempX);
                            double subtracted = predicted - tempY;

                            fails = subtracted > 0.1 ? fails + 1 : fails;
                        }

                        double intercepts = regression.interceptStdErr();
                        double slope = regression.slopeStdErr();

                        int outliersX = outliersYaw.getX().size() + outliersYaw.getY().size();
                        int outliersY = outliersPitch.getX().size() + outliersPitch.getY().size();

                        if (intercepts > 1.4 && slope > 0.0 && fails > 15 && outliersX < 10 && outliersY < 10) {
                            if (increaseBuffer() > 6) {
                                fail("intercepts=" + intercepts + " slope=" + slope);
                            }
                        } else decreaseBufferBy(0.15);

                        samplesYaw.clear();
                        samplesPitch.clear();
                    }
                }
            }
        }
    }
}
