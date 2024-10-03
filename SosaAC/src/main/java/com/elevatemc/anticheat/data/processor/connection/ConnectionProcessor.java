package com.elevatemc.anticheat.data.processor.connection;

import com.elevatemc.anticheat.config.Config;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.util.server.ColorUtil;
import com.elevatemc.anticheat.util.type.EvictingList;
import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.type.Pair;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerKeepAlive;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class ConnectionProcessor
{
    private final PlayerData data;
    private final ConcurrentLinkedQueue<Pair<Short, Long>> transactionsSent = new ConcurrentLinkedQueue<>();
    private final int lastTransactionAtStartOfTick = 0;
    private final List<Short> didWeSendThatTrans = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger transactionIDCounter = new AtomicInteger(0);
    private final LinkedList<Pair<Integer, Runnable>> transactionMap = new LinkedList<>();
    private final AtomicInteger lastTransactionSent = new AtomicInteger(0);
    private final AtomicInteger lastTransactionReceived = new AtomicInteger(0);
    private long transactionPing;
    private long playerClockAtLeast;
    private final Map<Long, Long> keepAliveTimes = new HashMap<>();

    private boolean receivedKeepAlive = false;


    public ConnectionProcessor(final PlayerData data) {
        this.data = data;
    }

    public boolean isLagging() {
        return System.currentTimeMillis() - flyingDelay < 220L;
    }
    public boolean handleTransaction(WrapperPlayClientWindowConfirmation wrapper) {
        final short id = wrapper.getActionId();
        // Vanilla always uses an ID starting from 1
        if (id <= 0) {
            // Check if we sent this packet before cancelling it
            return addTransactionResponse(id);
        }
        return false;
    }

    public void sendTransaction() {
        sendTransaction(false);
    }

    public void sendTransaction(boolean runInEventLoop) {
        if (data.getUser().getConnectionState() != ConnectionState.PLAY) { // Only send transactions during play state
            return;
        }

        short transactionID = (short) (-1 * (transactionIDCounter.getAndIncrement() & 0x7FFF));
        try {
            this.addTransactionSend(transactionID);

            WrapperPlayServerWindowConfirmation packet = new WrapperPlayServerWindowConfirmation((byte) 0, transactionID, false);
            if (runInEventLoop) {
                ChannelHelper.runInEventLoop(data.getUser().getChannel(), () -> data.getUser().writePacket(packet));
            } else {
                data.getUser().writePacket(packet);
            }
        }
        catch (final Exception exception) {
            exception.printStackTrace();
        }
    }

    // Players can get 0 ping by repeatedly sending invalid transaction packets, but that will only hurt them
    // The design is allowing players to miss transaction packets, which shouldn't be possible
    // But if some error made a client miss a packet, then it won't hurt them too bad.
    // Also, it forces players to take knockback
    public boolean addTransactionResponse(short id) {
        Pair<Short, Long> data = null;
        boolean hasID = false;
        for (Pair<Short, Long> iterator : transactionsSent) {
            if (iterator.getX() == id) {
                hasID = true;
                break;
            }
        }

        if (hasID) {
            do {
                data = transactionsSent.poll();
                if (data == null)
                    break;

                lastTransactionReceived.incrementAndGet();
                transactionPing = (System.currentTimeMillis() - data.getY());
                playerClockAtLeast = data.getY();
            } while (data.getX() != id);

            // A transaction means a new tick, so apply any block places
            handleNettySyncTransaction(lastTransactionReceived.get());
        }

        // Were we the ones who sent the packet?
        return data != null && data.getX() == id;
    }

    public void handleNettySyncTransaction(int transaction) {
        synchronized (this) {
            for (ListIterator<Pair<Integer, Runnable>> iterator = transactionMap.listIterator(); iterator.hasNext(); ) {
                Pair<Integer, Runnable> pair = iterator.next();

                // We are at most a tick ahead when running tasks based on transactions, meaning this is too far
                if (transaction + 1 < pair.getX())
                    return;

                // This is at most tick ahead of what we want
                if (transaction == pair.getX() - 1)
                    continue;


                try {
                    // Run the task
                    pair.getY().run();
                } catch (Exception e) {
                    System.out.println("An error has occurred when running transactions for player: " + data.getPlayer().getName());
                    e.printStackTrace();
                }
                // We ran a task, remove it from the list
                iterator.remove();
            }
        }
    }


    public void handleOutboundTransaction(WrapperPlayServerWindowConfirmation wrapper) {
        final short id = wrapper.getActionId();
        // Vanilla always uses an ID starting from 1
        if (id <= 0) {
            if (this.didWeSendThatTrans.remove((Short) id)) {
                transactionsSent.add(new Pair<>(id, System.currentTimeMillis()));
                lastTransactionSent.getAndIncrement();
            }
        }
    }

    public void addTransactionSend(final short id) {
        this.didWeSendThatTrans.add(id);
    }

    public void addTransactionHandler(int transaction, Runnable runnable) {
        if (lastTransactionReceived.get() >= transaction) { // If the player already responded to this transaction
            ChannelHelper.runInEventLoop(data.getUser().getChannel(), runnable); // Run it sync to player channel
            return;
        }
        synchronized (this) {
            transactionMap.add(new Pair<>(transaction, runnable));
        }
    }

    // Keepalive handlers
    private long keepAlivePing;
    private long lastFlying;
    private long flyingDelay;
    private boolean fast;
    private int lastLagSpike;
    private final EvictingList<Long> samples = new EvictingList<>(5);

    public void handleFlying() {
        final long now = System.currentTimeMillis();
        final int ticks = Sosa.INSTANCE.getTickManager().getTicks();
        flyingDelay = now - lastFlying;
        fast = (flyingDelay < 5L);
        samples.add(flyingDelay);
        if (MathUtil.getAverage(samples) < 3.0) {
            lastLagSpike = ticks;
        }
        lastFlying = now;
    }

    public void handleKeepAlive(WrapperPlayClientKeepAlive wrapper) {
        long id = wrapper.getId();

        if (keepAliveTimes.containsKey(id)) {
            keepAlivePing = System.currentTimeMillis() - keepAliveTimes.remove(id);

            receivedKeepAlive = true;
        } else {
            Sosa.INSTANCE.getAlertManager().sendMessage(ColorUtil.translate(Config.ALERTS_FORMAT) + " " + data.getPlayer().getName() + " failed Connection ");
        }
    }

    public void handleOutboundKeepAlive(WrapperPlayServerKeepAlive wrapper) {
        keepAliveTimes.put(wrapper.getId(), System.nanoTime());
    }

    public int getPingTicks() {
        return (int) (Math.ceil(keepAlivePing / 100.0) + 2);
    }
}

