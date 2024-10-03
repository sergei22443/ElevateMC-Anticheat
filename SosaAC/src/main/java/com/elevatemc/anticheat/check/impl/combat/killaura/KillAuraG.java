package com.elevatemc.anticheat.check.impl.combat.killaura;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.google.common.collect.Lists;
import lombok.val;

import java.util.Deque;

@CheckInfo(name = "Kill Aura", type = "G",description = "Checks for invalid acceleration overtime")
public class KillAuraG extends Check {
    private final Deque<Float> samplesYaw = Lists.newLinkedList();
    private final Deque<Float> samplesPitch = Lists.newLinkedList();

    private float lastDifferenceYaw, lastDifferencePitch;


    public KillAuraG(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {

            if (hitTicks() < 4 && !isExempt(ExemptType.TELEPORT, ExemptType.JOINED)) {
                float deltaYaw = data.getRotationProcessor().getDeltaYaw();
                float lastDeltaYaw = data.getRotationProcessor().getLastDeltaYaw();

                float deltaPitch = data.getRotationProcessor().getDeltaPitch();
                float lastDeltaPitch = data.getRotationProcessor().getLastDeltaPitch();

                float yawDiff = Math.abs(deltaYaw - lastDeltaYaw);
                float pitchDiff = Math.abs(deltaPitch - lastDeltaPitch);

                float joltYaw = Math.abs(yawDiff - lastDifferenceYaw);
                float joltPitch = Math.abs(pitchDiff - lastDifferencePitch);

                handle:
                {
                    if (joltPitch == 0.0 || joltYaw == 0.0) break handle;

                    samplesPitch.add(joltPitch);
                    samplesYaw.add(joltYaw);

                    if (samplesYaw.size() + samplesPitch.size() == 60) {
                        val outliersYaw = MathUtil.getOutliers(samplesYaw);
                        val outliersPitch = MathUtil.getOutliers(samplesPitch);

                        int duplicatesX = MathUtil.getDuplicates(samplesYaw);
                        int duplicatesY = MathUtil.getDuplicates(samplesPitch);

                        int outliersX = outliersYaw.getX().size() + outliersYaw.getY().size();
                        int outliersY = outliersPitch.getX().size() + outliersPitch.getY().size();

                        if (duplicatesX + duplicatesY == 0.0 && outliersX < 5 && outliersY < 5) {
                            if (increaseBuffer() > 6) {
                                fail("olX=" + outliersX + " olY=" + outliersY);
                            }
                        } else {
                            decreaseBufferBy(0.25);
                        }

                        samplesYaw.clear();
                        samplesPitch.clear();
                    }
                }

                lastDifferenceYaw = yawDiff;
                lastDifferencePitch = pitchDiff;
            }
        }
    }
}