package com.elevatemc.anticheat.data.processor.player;

import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VelocityProcessor {
    private final PlayerData data;
    private double velocityX;
    private double velocityY;
    private double velocityZ;
    private double velocityXZ;
    private int tick;
    private int flyingVelocityTicks;

    private State state = State.IDLE;
    private boolean resetState = false;

    public VelocityProcessor(final PlayerData data) {
        this.data = data;
    }

    public void handle(PacketSendEvent event) {
        final WrapperPlayServerEntityVelocity wrapper = new WrapperPlayServerEntityVelocity(event);
        if (wrapper.getEntityId() == data.getPlayer().getEntityId()) {
            if (wrapper.getVelocity().getY() > 0 && canVerifyVelocity()) {
                resetState = false;
                state = State.IDLE;

                // Sandwich the velocity
                data.getConnectionProcessor().sendTransaction();
                event.getTasksAfterSend().add(() -> data.getConnectionProcessor().sendTransaction());

                data.getConnectionProcessor().addTransactionHandler(data.getConnectionProcessor().getLastTransactionSent().get(), () -> {
                    this.velocityX = wrapper.getVelocity().getX();
                    this.velocityY = wrapper.getVelocity().getY();
                    this.velocityZ = wrapper.getVelocity().getZ();
                    this.velocityXZ = MathUtil.hypot(velocityX, velocityZ);
                    this.tick = Sosa.INSTANCE.getTickManager().getTicks();
                    this.flyingVelocityTicks = 0;

                    state = State.PROCESSING;
                });

                data.getConnectionProcessor().addTransactionHandler(data.getConnectionProcessor().getLastTransactionSent().get() + 1, () -> state = State.SANDWICHED);
            }

        }


    }

    public boolean canVerifyVelocity() {
        boolean exempted = data.getExemptProcessor().isExempt(
                ExemptType.COLLIDING_VERTICALLY,
                ExemptType.COLLIDING_HORIZONTALLY,
                ExemptType.LIQUID,
                ExemptType.TELEPORT,
                ExemptType.JOINED,
                ExemptType.FLIGHT,
                ExemptType.CREATIVE);

        return !exempted
                && !data.getPositionProcessor().isJumped()
                && data.getPositionProcessor().isLastMovementIncludedPosition();
    }

    public int getTicksSinceVelocity() {
        return Sosa.INSTANCE.getTickManager().getTicks() - this.tick;
    }

    public void handleFlying() {
        if (!canVerifyVelocity()) {
            // We are in a situation that we can't verify if the velocity that was taken is 100% correct
            state = State.IDLE;
        } else if (resetState) {
            resetState = false;
            state = State.IDLE;
        } else if (state == State.SANDWICHED) {
            // We can't set the state to idle yet because our velocity checks to have to verify the last movement
            resetState = true;
        }


        ++this.flyingVelocityTicks;
    }

    public PlayerData getData() {
        return this.data;
    }

    public double getVelocityX() {
        return this.velocityX;
    }

    public double getVelocityY() {
        return this.velocityY;
    }

    public double getVelocityZ() {
        return this.velocityZ;
    }

    public double getVelocityXZ() {
        return this.velocityXZ;
    }

    public enum State {
        IDLE,
        PROCESSING,
        SANDWICHED
    }
}
