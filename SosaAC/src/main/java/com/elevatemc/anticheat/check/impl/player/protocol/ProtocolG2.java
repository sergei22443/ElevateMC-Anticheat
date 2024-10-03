package com.elevatemc.anticheat.check.impl.player.protocol;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientHeldItemChange;

@CheckInfo(name = "Protocol", type = "G2", description = "Checks for invalid slot change")
public class ProtocolG2 extends Check {

    private int lastSlot;
    private boolean outgoingHeldItemSlot;

    public ProtocolG2(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            if (isExempt(ExemptType.JOINED)) return;

            final WrapperPlayClientHeldItemChange wrapper = new WrapperPlayClientHeldItemChange(event);

            final int slot = wrapper.getSlot();

            final boolean invalid = slot == lastSlot;
            final boolean exempt = outgoingHeldItemSlot;

            if (invalid && !exempt) {
                if (increaseBuffer() > 1.32) {
                    fail("slot=" + slot + " lastSlot=" + slot);
                }
            } else {
                resetBuffer();
            }
            lastSlot = slot;
            outgoingHeldItemSlot = false;
        }
    }

    public void onPacketSend(final PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.HELD_ITEM_CHANGE) {
            outgoingHeldItemSlot = true;
        }
    }
}
