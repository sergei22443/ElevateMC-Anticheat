package com.elevatemc.anticheat.check.impl.combat.autoclicker;

import com.elevatemc.anticheat.SosaPlugin;
import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.util.type.EvictingList;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.bukkit.Bukkit;

import java.text.DecimalFormat;

@CheckInfo(name = "Auto Clicker", type = "I", description = "Checks for too low skewness")
public class AutoClickerI extends Check {
    private final EvictingList<Long> samples = new EvictingList<>(125);

    public AutoClickerI(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent packet) {

        if (packet.getPacketType() == PacketType.Play.Client.ANIMATION) {
            final boolean exempt = isExempt(ExemptType.AUTOCLICKER);

            check:
            {
                if (exempt) break check;

                final long delay = data.getClickProcessor().getDelay();

                if (delay > 5000L) {
                    samples.clear();
                    return;
                }
                samples.add(delay);

                if (samples.isFull()) {
                    final double skewness = MathUtil.getSkewness(samples);

                    if (skewness < 1.0E-8) {
                        final double cps = MathUtil.getCps(samples);

                        if (increaseBuffer() > 10 && cps > 8.0) {
                            fail("skew=" + skewness + " cps=" + new DecimalFormat(".###").format(cps));
                            multiplyBuffer(.25);
                        }
                        samples.clear();
                    }
                }
            }
        }
    }
}
