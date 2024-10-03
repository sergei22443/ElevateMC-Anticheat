package com.elevatemc.anticheat.data;

import com.elevatemc.anticheat.data.processor.connection.ConnectionProcessor;
import com.elevatemc.anticheat.data.processor.entity.*;
import com.elevatemc.anticheat.data.processor.player.ActionProcessor;
import com.elevatemc.anticheat.data.processor.player.ClickProcessor;
import com.elevatemc.anticheat.data.processor.rotation.RotationProcessor;
import com.elevatemc.anticheat.data.processor.player.VelocityProcessor;
import com.elevatemc.anticheat.manager.CheckManager;
import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.data.processor.rotation.SensitivityHolder;
import com.elevatemc.anticheat.data.processor.rotation.SensitivityProcessor;
import com.elevatemc.anticheat.processor.player.validity.ExemptProcessor;
import com.elevatemc.anticheat.util.server.helper.EntityHelper;
import com.github.retrooper.packetevents.protocol.player.User;
import lombok.*;

import org.bukkit.entity.Player;

import java.util.List;

@Getter @Setter
public class PlayerData {

    private Player player;
    private final User user;

    private String clientBrand = null;

    private long joinTime = System.currentTimeMillis();
    private long lastClientBrandAlert;
    private int serverTicks;

    private final List<Check> checks = CheckManager.loadChecks(this);
    private final ExemptProcessor exemptProcessor = new ExemptProcessor(this);
    private final SensitivityProcessor sensitivityProcessor = new SensitivityProcessor(this);
    private final SensitivityHolder sensitivityHolder = new SensitivityHolder(this);
    private final CombatProcessor combatProcessor = new CombatProcessor(this);
    private final ActionProcessor actionProcessor = new ActionProcessor(this);
    private final ClickProcessor clickProcessor = new ClickProcessor(this);
    private final VelocityProcessor velocityProcessor = new VelocityProcessor(this);
    private final ConnectionProcessor connectionProcessor = new ConnectionProcessor(this);
    private final PlayerTracker playerTracker = new PlayerTracker(this);
    private final RotationProcessor rotationProcessor = new RotationProcessor(this);
    private final PotionProcessor potionProcessor = new PotionProcessor(this);
    private final PositionProcessor positionProcessor = new PositionProcessor(this);
    private final BotProcessor botProcessor = new BotProcessor();
    private final EntityHelper entityHelper = new EntityHelper();
    public PlayerData(final User user) {
        this.user = user;
    }

    public void initializePlayer(Player player) {
        this.player = player;
    }

    public void handleTickStart() {
        playerTracker.onTickStart();
    }

    public void handlePostEntityTracker() {
        playerTracker.postEntityTracker();
    }
}
