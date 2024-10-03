package com.elevatemc.anticheat.check.impl.combat.attack;

import java.util.Optional;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@CheckInfo(name = "Attack", type = "B", description = "Player is attacking outside hitbox")
public class AttackB extends Check
{
    public AttackB(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            final WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
            final Entity entity = SpigotReflectionUtil.getEntityById(wrapper.getEntityId());
            if (entity instanceof Player) {
                final Optional<Vector3f> vec3 = wrapper.getTarget();
                if (vec3.isPresent()) {
                    final Vector3f hitbox = vec3.get();
                    if (Math.abs(hitbox.getX()) > 0.4001 || Math.abs(hitbox.getZ()) > 0.4001) {
                        fail("X=" + Math.abs(hitbox.getX()) + " Z=" + Math.abs(hitbox.getZ()));
                    }
                }
            }
        }
    }
}
