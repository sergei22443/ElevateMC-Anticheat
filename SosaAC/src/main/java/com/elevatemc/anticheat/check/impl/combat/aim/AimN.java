package com.elevatemc.anticheat.check.impl.combat.aim;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

import java.util.HashMap;
import java.util.Map;

// Taken from wizzard (patches some vape aims)
@CheckInfo(name = "Aim", type = "N", description = "Checks for impossible differences between rotations")
public class AimN extends Check {
    private int verbose;
    private Map<Float, Integer> values = new HashMap<>();
    private boolean pattern;
    private long reset;

    public AimN(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (hitTicks() < 5) {
                float yawDiff = (float) MathUtil.round(Math.abs(MathUtil.clamp180(data.getRotationProcessor().getDeltaYaw() % 360)), 3);
                int aurasmoothticks = data.getRotationProcessor().getSmoothingTicks();
                if (data.getRotationProcessor().isZooming()) {
                    values.clear();
                    pattern = false;
                }
                if (yawDiff > 0.0 && aurasmoothticks < 10) {
                    if (values.containsKey(yawDiff)) {
                        int needed = values.get(yawDiff);
                        values.put(yawDiff, needed + 1);
                        verbose += 1;
                    } else {
                        values.put(yawDiff, 1);
                        if (!pattern) {
                            pattern = true;
                            reset = System.currentTimeMillis();
                        } else {
                            if (elapsed(reset, 5500L)) {
                                if (verbose == 0 && values.size() > 29) {
                                    if (increaseBuffer() > 6.0) {
                                        fail("rotations= " + values.size());
                                        resetBuffer();
                                    }
                                }
                                verbose = 0;
                                values.clear();
                                pattern = false;
                            }
                        }
                    }
                }
            }
        }
    }
    public static boolean elapsed(long time, long needed) {
        return Math.abs(System.currentTimeMillis() - time) >= needed;
    }
}
