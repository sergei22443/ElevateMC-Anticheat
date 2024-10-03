package com.elevatemc.anticheat.check.impl.player.blink;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.server.PlayerUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Blink", type = "D", description = "Sent movement too late.")
public class BlinkD extends Check {

    private boolean blinking;
    private int forLoc = 0;
    private long lastMillis;
    private long lastStandTicks;
    private int standTicks = 0;

    public BlinkD(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        int ping =  PlayerUtil.getPing(data.getPlayer());
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            WrapperPlayClientPlayerFlying packet = new WrapperPlayClientPlayerFlying(event);
            if(packet.hasPositionChanged()) {
                if (data.getPositionProcessor().getDeltaXZ() == 0.0 && data.getPositionProcessor().getDeltaY() == 0.0) {
                    standTicks++;
                } else {
                    standTicks = 0;
                }
            }
        }

        if (ping > 150 || hitTicks() < 5) {
            return;
        }

        if (event.getPacketType() == PacketType.Play.Client.KEEP_ALIVE) {
            if (isExempt(ExemptType.VEHICLE, ExemptType.DEAD)) {
                return;
            }

            long deltaMillis = data.getConnectionProcessor().getFlyingDelay();
            if (standTicks < 20 && standTicks > 3 && data.getConnectionProcessor().isLagging()) {
                this.blinking = true;
                this.lastMillis = deltaMillis;
                this.lastStandTicks = standTicks;
            }
        } else if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())  && this.blinking) {
            if (this.forLoc == 0) {
                ++this.forLoc;
            } else if (this.forLoc == 1) {
                double zDiff = data.getPositionProcessor().getLastZ() - data.getPositionProcessor().getZ();
                double yDiff = data.getPositionProcessor().getLastY() - data.getPositionProcessor().getY();
                double xDiff = data.getPositionProcessor().getLastX() - data.getPositionProcessor().getX();

                if (xDiff * yDiff * zDiff == 0.0) return;

                if (xDiff > 0.21 || xDiff < -0.21 || yDiff < -0.21 || yDiff > 0.21 || zDiff < -0.21 || zDiff > 0.21) {
                    if (increaseBuffer() > 10.0) {
                        fail("xDiff=" + xDiff + " yDiff=" + yDiff + " zDiff=" + zDiff + " stand=" + this.lastStandTicks + " lastMillis=" +
                                this.lastMillis);
                        resetBuffer();
                    }
                } else {
                    decreaseBufferBy(0.75);
                }
                this.blinking = false;
                this.forLoc = 0;
            }
        }
    }
}
