package com.elevatemc.anticheat.check.impl.player.refill;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCloseWindow;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.Queue;

@CheckInfo(name = "Refill", type = "B", description = "Checks for auto-refill")
public class RefillB extends Check {

    private final Queue<Long> delays = new LinkedList<>();
    private final Queue<Double> samples = new LinkedList<>();

    private long lastMove = -1;
    private int cancelMassMoveBuffer = 0;

    private final static int SAMPLE_SIZE_MIN = 2;
    private final static int SAMPLE_CHECK_SIZE = 5;

    public RefillB(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            final WrapperPlayClientClickWindow wrapper = new WrapperPlayClientClickWindow(event);

            if (wrapper.getWindowId() == 0 && wrapper.getSlot() >= 9 && wrapper.getSlot() <= 35 && wrapper.getButton() == 0 && wrapper.getWindowClickType() == WrapperPlayClientClickWindow.WindowClickType.QUICK_MOVE) {
                ItemStack item = data.getPlayer().getInventory().getItem(wrapper.getSlot());
                if (item == null) return;
                Material type = item.getType();

                if ((type.equals(Material.POTION) && (item.getDurability() == 16421 || item.getDurability() == 16389)) || type.equals(Material.MUSHROOM_SOUP)) { // Checks for a healing item
                    if (lastMove > 0) {
                        long delay = now() - lastMove;
                        delays.add(delay);

                        if (delay == 0) cancelMassMoveBuffer++;
                        if (cancelMassMoveBuffer > 3) {
                            delays.clear();
                            lastMove = -1; // Cancel out the next move (inaccurate)
                        }
                    }
                    lastMove = now();
                }
            }
        } else if (event.getPacketType() == PacketType.Play.Client.CLOSE_WINDOW) {
            WrapperPlayClientCloseWindow wrapped = new WrapperPlayClientCloseWindow(event);
            if (wrapped.getWindowId() == 0) {
                cancelMassMoveBuffer = 0;
                lastMove = -1;

                if (delays.size() >= SAMPLE_SIZE_MIN) {
                    double average = MathUtil.getAverage(delays);
                    samples.add(average);
                }
                delays.clear();

                if (samples.size() >= SAMPLE_CHECK_SIZE) {
                    double deviation = MathUtil.getStandardDeviation(samples);
                    if (deviation < 5) {
                        fail("deviation=" + deviation);
                    }
                    samples.clear();
                }
            }
        }
    }
}