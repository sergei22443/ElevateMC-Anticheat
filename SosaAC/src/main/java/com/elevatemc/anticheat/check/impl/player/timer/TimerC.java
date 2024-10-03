package com.elevatemc.anticheat.check.impl.player.timer;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Timer", type = "C", description = "Checks for gamespeed")
public class TimerC extends Check {

    long balance = -100L;

    Long lastFlying;

    public TimerC(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent packet) {
        if (WrapperPlayClientPlayerFlying.isFlying(packet.getPacketType())) {

            boolean valid = data.getConnectionProcessor().isReceivedKeepAlive();

            if (valid) {

                if (lastFlying != null) {
                    balance += 50L;
                    balance -= (now() - lastFlying);

                    if (balance > 50L) {
                        if (increaseBuffer() > 2) {
                            fail("balance= " + balance);
                        }

                        balance = 0L;
                    } else {
                        decreaseBufferBy(0.05);
                    }
                }

                lastFlying = now();
            }
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.SPAWN_POSITION || event.getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
            //debug(balance);
            balance -= 100L;

            // what the actual fuck?
            if (event.getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
                //debug(balance);
                balance -= 750L;
            }

        }
    }
}
