package com.elevatemc.anticheat.check.impl.combat.pattern;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Pattern", type = "A", description = "Too small pitch change.")
public class PatternA extends Check {

    public PatternA(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (!data.getRotationProcessor().isZooming()) {
                /*
                    Bored so let's do an explanation to this check because it's easy

                    This checks for when the deltaPitch is exponentially small in comparison to the deltaYaw

                    We grab our deltaYaw/Pitch from our processor

                    and proceed to make our check based on the following explanation:

                    - The players' rotations should be linear, moving your mouse an exponentially small amount
                    - on 1 AXIS and a large amount on the other is basically impossible

                    - the deltaYaw will be a large rotation where the deltaPitch will be in shambles.

                    Explanation done, Saif out.

                 */
                float deltaYaw = data.getRotationProcessor().getDeltaYaw();
                float deltaPitch = data.getRotationProcessor().getDeltaPitch();

                boolean invalid = deltaPitch < .0001 && deltaPitch > 0 && deltaYaw > .5F;

                if (invalid) {
                    if (increaseBuffer() > 6.0) {
                        fail("deltaPitch=" + deltaPitch + " deltaYaw=" + deltaYaw);
                        staffAlert();
                        increaseVlBy(1.25);
                        multiplyBuffer(.25);
                    }
                } else {
                    decreaseBufferBy(.25);
                }
            }
        }
    }
}
