package com.elevatemc.anticheat.check.impl.movement.motion;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

import java.util.Arrays;

@CheckInfo(name = "Motion", type = "K", experimental = true, description = "Invalid vertical motion")
public class MotionK extends Check {
    public MotionK(PlayerData data) {
        super(data);
    }

    Double[] list = {0.5800000131130219D, 0.4203014563238838, 0.4203014563238847, 0.514931815469879, 0.5800000131130219,
            0.4203014563238838,  0.4203014563238847, 0.5390050231218337, 0.5573390312643056, 0.4945615005650694, 0.49456150056508363,
            0.49499999999999744,0.4970461089582443 , 0.49500000000000455, 0.5170841594871405, 0.49456150056507653 ,0.45000000000000284,
            0.4375,0.5926045976350611, 0.5694954887628256, 0.5952911683122082,0.44096591315684464,0.5952911683122011,0.5628236320017379,
            0.4453744695041024, 0.44537446950410686, 0.44091960879394154, 0.5926045976350593 , 0.4203014563238696, 0.592604597635062,
            0.4641593749554431, 0.4598749991059279, 0.4641593749554449, 0.5184762024129892, 0.4203014563238847, 0.5952911683122082,
            0.4440989118733967, 0.5625, 0.5926045976350451,0.5926045976350593,0.5938211453046662,0.5694954887628114,0.592604597635062,
            0.43920171408269937,0.5374763018953246};
    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            boolean exempt = isExempt(ExemptType.TELEPORT, ExemptType.FLIGHT, ExemptType.JOINED, ExemptType.COLLIDING_VERTICALLY, ExemptType.STAIR, ExemptType.VELOCITY, ExemptType.WEB, ExemptType.LIQUID) || !data.getPositionProcessor().isLastMovementIncludedPosition() || data.getPositionProcessor().isNearSlab() || data.getPositionProcessor().isNearStair();

            if (exempt) return;

            double deltaY = data.getPositionProcessor().getDeltaY();
            double max = 0.41999998688697815;

            max += data.getPotionProcessor().getJumpBoostAmplifier() * 0.12;
            boolean exemptDeltas = shouldExemptDelta(deltaY);

            if (deltaY > max && data.getPlayer().getWalkSpeed() < 0.23f && !exemptDeltas) {
                if (increaseBuffer() > 2.0) {
                    fail("dY=" + deltaY);
                }
            } else {
                decreaseBuffer();
            }
        }
    }

    public boolean shouldExemptDelta(double delta) {

        return Arrays.asList(list).contains(delta);
    }
}
