package com.elevatemc.anticheat.check.impl.combat.killaura;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@CheckInfo(name = "Kill Aura", type = "A", description = "Attacked two entities at once.")
public class KillAuraA extends Check {

    private int lastEntityId, hits;

    public KillAuraA(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
            Entity entity = SpigotReflectionUtil.getEntityById(wrapper.getEntityId());

            if (wrapper.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK || !(entity instanceof Player) || data.getUser().getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) {
                return;
            }

            int id = wrapper.getEntityId();

            if (id != lastEntityId) {
                if (++hits > 1) {
                    fail("id=" + id + " lastId=" + lastEntityId);
                    increaseVlBy(.75);
                }
            }

            lastEntityId = id;
        } else if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            hits = 0;
        }
    }
}
