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

@CheckInfo(name = "Auto Clicker", type = "G", description = "Checks for too low outlier")
public class AutoClickerG extends Check {
    private final EvictingList<Long> samples = new EvictingList<>(125);

    public AutoClickerG(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent packet) {

        if (packet.getPacketType() == PacketType.Play.Client.ANIMATION && !isExempt(ExemptType.AUTOCLICKER)) {
            final long delay = data.getClickProcessor().getDelay();

            if (delay > 5000L) {
                samples.clear();
                return;
            }
            samples.add(delay);
            if (samples.isFull()) {
                int outliers = (int) samples.stream().filter(l -> l > 150).count();
                double average = MathUtil.getAverage(samples);
                double cps = MathUtil.getCps(samples);
                if (outliers == 0) {
                    if (increaseBuffer() > 30) {
                        multiplyBuffer(.20);
                        fail("outliers=" + outliers + " average=" + average + " cps=" + cps);
                        staffAlert();
                    }
                } else {
                    decreaseBufferBy(8);
                }
            }
        }
    }
}
