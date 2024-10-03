package com.elevatemc.anticheat.check.impl.combat.killaura;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.util.type.EvictingList;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;


@CheckInfo(name = "Kill Aura", type = "K", experimental = true, description = "Checks for invalid average deviation")
public class KillAuraK extends Check {

    private EvictingList<Double> differenceSamples = new EvictingList<>(25);
    private Entity target;

    public KillAuraK(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent packet) {
        if (packet.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION || packet.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION) {
            if (hitTicks() < 4 && target != null) {

                Location origin = data.getPlayer().getLocation().clone();
                Vector end = target.getLocation().clone().toVector();

                float optimalYaw = origin.setDirection(end.subtract(origin.toVector())).getYaw() % 360F;
                float rotationYaw = data.getRotationProcessor().getYaw();
                float deltaYaw = data.getRotationProcessor().getDeltaYaw();
                float fixedRotYaw = (rotationYaw % 360F + 360F) % 360F;

                double difference = Math.abs(fixedRotYaw - optimalYaw);

                if (deltaYaw > 3f) {
                    differenceSamples.add(difference);
                }

                if (differenceSamples.isFull()) {
                    double average = MathUtil.getAverage(differenceSamples);
                    double deviation = MathUtil.getStandardDeviation(differenceSamples);

                    boolean invalid = average < 7 && deviation < 12;

                    if (invalid) {
                        if (increaseBuffer() > 15) {
                            fail("dev=" + deviation + " avg=" + average);
                            resetBuffer();
                        }
                    } else {
                        decreaseBufferBy(3);
                    }
                    differenceSamples.clear();
                }
            }
        } else if (packet.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            final WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(packet);

            if (wrapper.getAction().equals(WrapperPlayClientInteractEntity.InteractAction.ATTACK)) {
                Entity e = SpigotReflectionUtil.getEntityById(wrapper.getEntityId());

                if (e instanceof Player) {
                    target = SpigotReflectionUtil.getEntityById(wrapper.getEntityId());
                }
            }
        }
    }
}
