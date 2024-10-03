package com.elevatemc.anticheat.check.impl.player.invalid;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.server.ColorUtil;
import com.elevatemc.anticheat.util.server.PlayerUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import org.bukkit.Bukkit;

import java.util.Deque;
import java.util.LinkedList;

@CheckInfo(name = "Invalid", type = "D", experimental = true, description = "Checks for invalid distance travel")
public class InvalidD extends Check {

    private static final double COMPENSATION = 0.5, INVALID = 2.5;

    public InvalidD(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent packet) {
        if (packet.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || packet.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION || WrapperPlayClientPlayerFlying.isFlying(packet.getPacketType())) {

            /*
                This check is basically a blink check but not exactly,
                 it's most likely checking for teleportation, a certain distance crossed which may not be possible
                 unless an extreme lag spike happened

                  We're basically checking the difference between the received position and the player's current
                  location Still has some work to be done.
             */

            // Grab our deltas
            double deltaX = data.getPositionProcessor().getDeltaX(), deltaY = data.getPositionProcessor().getDeltaY(), deltaZ = data.getPositionProcessor().getDeltaZ();
            // Get our total deltas combined.
            double total = deltaX + deltaY + deltaZ;
            // Get the players ping.
            double ping = PlayerUtil.getPing(data.getPlayer());
            // Compensate lag
            double compensation = INVALID + (ping * COMPENSATION);
            // Most likely to NOT happen.
            if (total > compensation) {
                if (increaseBuffer() > 6.5) {
                    multiplyBuffer(0.35);
                    fail("t=" + total + " max=" + compensation);
                    staffAlert();
                }
            } else {
                decreaseBuffer();
            }
        }
    }
}
