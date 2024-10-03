package com.elevatemc.anticheat.check.impl.player.inventory;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Inventory", type = "A", description = "Moved while inventory is open")
public class InventoryA extends Check {

    public InventoryA(PlayerData data) {
        super(data);
    }


    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            boolean exempt = isExempt(ExemptType.WEB, ExemptType.LIQUID, ExemptType.VELOCITY, ExemptType.CLIMBABLE);

            boolean valid = !exempt && (data.getPositionProcessor().isClientOnGround() || data.getPositionProcessor().isMathematicallyOnGround());
            boolean inventoryOpen = data.getActionProcessor().isInventoryOpen();

            if (valid && inventoryOpen) {
                double deltaXZ = data.getPositionProcessor().getDeltaXZ();

                if (deltaXZ > 0.15) {
                    if (increaseBuffer() > 3) {
                        fail("Moving whilst clicking in inventory");
                        resetBuffer();
                    }
                }
            }
        }
    }
}
