package com.elevatemc.anticheat.check.impl.player.refill;


import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCloseWindow;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@CheckInfo(name = "Refill", type = "A",  description = "Pattern Detection for Auto-Refill.")
public class RefillA extends Check {

    private long lastMove = -1;
    private int cancelMassMoveBuffer = 0;
    private int prediction = 0;

    public RefillA(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            final WrapperPlayClientClickWindow wrapper = new WrapperPlayClientClickWindow(event);

            if (wrapper.getSlot() >= 9
                    && wrapper.getSlot() <= 35
                    && wrapper.getButton() == 0
                    && wrapper.getWindowClickType() == WrapperPlayClientClickWindow.WindowClickType.QUICK_MOVE
                    && wrapper.getWindowId() == 0) {

                com.github.retrooper.packetevents.protocol.item.ItemStack item = wrapper.getCarriedItemStack();
                if (item == null) return;

                ItemType type = item.getType();
                if ((type.equals(ItemTypes.POTION) && (item.getDamageValue() == 16421 || item.getDamageValue() == 16389)) || type.equals(ItemTypes.MUSHROOM_STEW)) { // Checks for a healing item
                    long delay = now() - lastMove;
                    if (delay == 0) cancelMassMoveBuffer++;
                    if (cancelMassMoveBuffer > 3) {
                        cancelMassMoveBuffer = 0;
                        resetBuffer();
                    }

                    int slot = wrapper.getSlot();
                    if (slot == prediction) {
                        if (increaseBuffer() > 18.0) {
                            fail("total=" + getBuffer());
                            multiplyBuffer(.25);
                        }
                    } else {
                        resetBuffer();
                    }
                    prediction = predictNextSlot(data.getPlayer().getInventory(), slot);
                    lastMove = now();
                }
            }
        } else if (event.getPacketType() == PacketType.Play.Client.CLOSE_WINDOW) {
            WrapperPlayClientCloseWindow wrapped = new WrapperPlayClientCloseWindow(event);
            if (wrapped.getWindowId() == 0) {
                cancelMassMoveBuffer = 0;
            }
        }
    }

    public int predictNextSlot(Inventory inventory, int start) {
        int predicted = start;
        boolean found = false;

        while (!found && predicted <= 35) {
            predicted++;

            ItemStack item = inventory.getItem(predicted);
            if (item == null) continue;

            Material type = item.getType();
            if ((type.equals(Material.POTION) && (item.getDurability() == 16421 || item.getDurability() == 16389)) || type.equals(Material.MUSHROOM_SOUP)) { // Checks for a healing item
                found = true;
            }
        }

        if (!found) {
            predicted = 0;
            resetBuffer();
        }

        return predicted;
    }
}