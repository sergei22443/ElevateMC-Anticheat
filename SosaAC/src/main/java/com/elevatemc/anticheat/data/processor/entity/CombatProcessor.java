package com.elevatemc.anticheat.data.processor.entity;

import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CombatProcessor
{
    private final PlayerData data;
    private int hitTicks;

    public CombatProcessor(final PlayerData data) {
        this.data = data;
    }

    public void handleEntityInteract(final WrapperPlayClientInteractEntity wrapper) {
        if (wrapper.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
            return;
        }
        this.hitTicks = 0;
    }

    public void handleFlying() {
        ++this.hitTicks;
    }

    public PlayerData getData() {
        return this.data;
    }

    public int getHitTicks() {
        return this.hitTicks;
    }

}
