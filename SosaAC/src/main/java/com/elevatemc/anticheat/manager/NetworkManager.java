package com.elevatemc.anticheat.manager;

import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

public class NetworkManager extends PacketListenerAbstract {

    public NetworkManager(final PacketListenerPriority priority) {
        super(priority);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        final PlayerData data = Sosa.INSTANCE.getPlayerDataManager().getPlayerData(event.getUser());

        handle: {
            if (data == null) break handle;
            if (data.getPlayer() == null) break handle;

            Sosa.INSTANCE.getReceivingPacketProcessor().handle(data, event);
        }
    }

    @Override
    public void onPacketSend(final PacketSendEvent event) {
        final PlayerData data = Sosa.INSTANCE.getPlayerDataManager().getPlayerData(event.getUser());

        handle: {
            if (data == null) break handle;
            if (data.getPlayer() == null) break handle;

            Sosa.INSTANCE.getSendingPacketProcessor().handle(data, event);
        }
    }

    @Override
    public void onUserLogin(UserLoginEvent event) {
        Sosa.INSTANCE.getPlayerDataManager().add(event.getUser());
    }

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        Sosa.INSTANCE.getPlayerDataManager().remove(event.getUser());
    }
}
