package com.elevatemc.anticheat.data.processor.entity;

import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.type.BotTypes;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BotProcessor{

    public int botID, rayCastEntityID, entityAReportedFlags, botTicks, entityATotalAttacks, movedBotTicks, randomBotSwingTicks, randomBotDamageTicks, rayCastFailHitTimes;
    public boolean hasBot, moveBot, WaitingForBot, hasRaycastBot, hasHitRaycast;
    public BotTypes botType;
    public double EntityAFollowDistance, rayCastEntityRoation;
    public float EntityAMovementOffset, EntityAStartYaw, rayCastStartYaw;
    public long lastEntitySpawn, entityHitTime, lastEntityBotHit, lastRaycastSpawn, lastRaycastGood, raycastEntity2HitTimes;
    public PlayerData forcedUser;

}
