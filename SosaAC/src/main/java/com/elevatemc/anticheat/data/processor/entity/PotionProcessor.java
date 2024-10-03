package com.elevatemc.anticheat.data.processor.entity;

import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRemoveEntityEffect;
import lombok.Getter;

@Getter
public class PotionProcessor {
    private final PlayerData data;
    private int speedBoostAmplifier, slownessAmplifier, jumpBoostAmplifier;

    public PotionProcessor(final PlayerData data) {
        this.data = data;
    }

    public void handleEffect(final WrapperPlayServerEntityEffect packet) {
        if (packet.getEntityId() == data.getPlayer().getEntityId()) {
            int potionId = packet.getPotionType().getId(data.getUser().getClientVersion());
            if (potionId == 1 || potionId == 2 || potionId == 8) {
                int amplifier = packet.getEffectAmplifier();
                data.getConnectionProcessor().addTransactionHandler(data.getConnectionProcessor().getLastTransactionSent().get() + 1, () -> {
                    switch (potionId) {
                        case 1: {
                            speedBoostAmplifier = amplifier + 1;
                            break;
                        }
                        case 2: {
                            slownessAmplifier = amplifier + 1;
                            break;
                        }
                        case 8: {
                            jumpBoostAmplifier = amplifier + 1;
                            break;
                        }
                    }
                });
            }
        }
    }

    public void handleRemoveEffect(final WrapperPlayServerRemoveEntityEffect packet) {
        if (packet.getEntityId() == data.getPlayer().getEntityId()) {
            int potionId = packet.getPotionType().getId(data.getUser().getClientVersion());
            if (potionId == 1 || potionId == 2 || potionId == 8) {
                data.getConnectionProcessor().addTransactionHandler(data.getConnectionProcessor().getLastTransactionSent().get() + 1, () -> {
                    switch (potionId) {
                        case 1: {
                            speedBoostAmplifier = 0;
                            break;
                        }
                        case 2: {
                            slownessAmplifier = 0;
                            break;
                        }
                        case 8: {
                            jumpBoostAmplifier = 0;
                            break;
                        }
                    }
                });
            }
        }
    }

    public void handleRespawn() {
        data.getConnectionProcessor().addTransactionHandler(data.getConnectionProcessor().getLastTransactionSent().get(), () -> {
            speedBoostAmplifier = 0;
            slownessAmplifier = 0;
            jumpBoostAmplifier = 0;
        });
    }
}
