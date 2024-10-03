package com.elevatemc.anticheat.check.impl.combat.killaura;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.util.server.PlayerUtil;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@CheckInfo(name = "Kill Aura", type = "C", description = "Invalid acceleration.")
public class KillAuraC extends Check {

    private int hitTicks;
    private Entity target;

    public KillAuraC(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (++hitTicks < 3) {
                double deltaXZ = data.getPositionProcessor().getDeltaXZ();
                double acceleration = data.getPositionProcessor().getAcceleration();

                float baseSpeed = PlayerUtil.getBaseSpeed(data.getPlayer(), .23F);

                long swingDelay = data.getClickProcessor().getDelay();

                boolean sprinting = data.getActionProcessor().isSprinting();
                boolean validTarget = target != null && target instanceof Player && !target.hasMetadata("npc");
                boolean validVersion = data.getUser().getClientVersion().isOlderThan(ClientVersion.V_1_9);

                boolean invalid = acceleration < .0025 && sprinting && deltaXZ > baseSpeed
                        && swingDelay < 200 && validTarget && validVersion;

                if (invalid) {
                    if (increaseBuffer() > 5) {
                        multiplyBuffer(.35);
                        fail("a=" + acceleration);
                        increaseVlBy(.45);
                        staffAlert();
                    }
                } else {
                    decreaseBufferBy(.75);
                }
            }
        } else if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);

            Entity entity = SpigotReflectionUtil.getEntityById(wrapper.getEntityId());

            if (entity instanceof Player && wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                hitTicks = 0;
                target = entity;
            }
        }
    }
}
