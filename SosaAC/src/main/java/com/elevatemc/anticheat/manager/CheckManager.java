package com.elevatemc.anticheat.manager;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.check.impl.combat.attack.*;
import com.elevatemc.anticheat.check.impl.combat.autoclicker.*;
import com.elevatemc.anticheat.check.impl.combat.criticals.*;
import com.elevatemc.anticheat.check.impl.combat.pattern.*;
import com.elevatemc.anticheat.check.impl.combat.velocity.*;
import com.elevatemc.anticheat.check.impl.movement.nofall.*;
import com.elevatemc.anticheat.check.impl.player.blink.*;
import com.elevatemc.anticheat.check.impl.player.entity.InvalidAttack;
import com.elevatemc.anticheat.check.impl.player.invalid.*;
import com.elevatemc.anticheat.check.impl.player.inventory.*;
import com.elevatemc.anticheat.check.impl.player.refill.*;
import com.elevatemc.anticheat.config.Config;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.check.impl.combat.aim.*;
import com.elevatemc.anticheat.check.impl.combat.killaura.*;
import com.elevatemc.anticheat.check.impl.movement.flight.*;
import com.elevatemc.anticheat.check.impl.movement.motion.*;
import com.elevatemc.anticheat.check.impl.movement.speed.*;
import com.elevatemc.anticheat.check.impl.player.interact.*;
import com.elevatemc.anticheat.check.impl.player.pingspoof.*;
import com.elevatemc.anticheat.check.impl.player.protocol.*;
import com.elevatemc.anticheat.check.impl.player.timer.*;
import lombok.Getter;

import java.lang.reflect.Constructor;
import java.util.*;

@Getter
public class CheckManager {

    private static final List<Constructor<?>> CONSTRUCTORS = new ArrayList<>();

    @Getter
    public static final Class<?>[] CHECKS;

    static {
        CHECKS = new Class[] {

                AutoClickerA.class,
                AutoClickerB.class,
                AutoClickerC.class,
                AutoClickerD.class,
                AutoClickerE.class,
                AutoClickerF.class,
                AutoClickerG.class,
                AutoClickerH.class,
                AutoClickerI.class,
                AutoClickerJ.class,
                AutoClickerK.class,
                AutoClickerL.class,
                AutoClickerM.class,

                VelocityA.class,
                VelocityB.class,

                AttackA.class,
                AttackB.class,

                AimA.class,
                AimB.class,
                AimC.class,
                AimC1.class,
                AimD.class,
                AimE.class,
                AimF.class,
                AimG.class,
                AimH.class,
                AimI.class,
                AimJ.class,
                AimK.class,
                AimL.class,
                AimM.class,
                AimN.class,
                AimO.class,
                AimP.class,
                AimQ.class,
                AimR.class,

                PatternA.class,
                PatternB.class,
                PatternC.class,
                PatternD.class,
                PatternE.class,
                PatternF.class,
                PatternG.class,
                PatternH.class,
                PatternI.class,
                PatternJ.class,
                PatternK.class,
                PatternL.class,
                PatternM.class,
                PatternN.class,
                PatternO.class,
                PatternP.class,

                KillAuraA.class,
                KillAuraB.class,
                KillAuraC.class,
                KillAuraD.class,
                KillAuraE.class,
                KillAuraF.class,
                KillAuraG.class,
                KillAuraH.class,
                KillAuraI.class,
                KillAuraJ.class,
                KillAuraK.class,
                KillAuraL.class,

                CriticalsA.class,
                CriticalsB.class,
                CriticalsC.class,

                FlightA.class,
                FlightB.class,
                FlightC.class,
                FlightD.class,
                FlightE.class,
                FlightF.class,
                FlightG.class,

                SpeedA.class,
                SpeedB.class,
                SpeedC.class,
                SpeedD.class,
                SpeedE.class,
                SpeedF.class,
                SpeedG.class,
                SpeedH.class,
                SpeedI.class,
                SpeedJ.class,
                SpeedK.class,
                SpeedL.class,
                SpeedM.class,

                MotionA.class,
                MotionB.class,
                MotionC.class,
                MotionD.class,
                MotionE.class,
                MotionF.class,
                MotionG.class,
                MotionH.class,
                MotionI.class,
                MotionJ.class,
                MotionK.class,
                MotionL.class,
                MotionM.class,
                MotionN.class,
                MotionO.class,
                MotionP.class,
                MotionQ.class,
                MotionR.class,
                MotionS.class,
                MotionT.class,
                MotionU.class,
                MotionV.class,
                MotionW.class,
                MotionX.class,

                NoFallA.class,
                NoFallB.class,

                TimerA.class,
                TimerB.class,
                TimerC.class,

                InvalidA.class,
                InvalidB.class,
                InvalidC.class,
                InvalidD.class,

                BlinkA.class,
                BlinkC.class,
                BlinkD.class,

                PingSpoofA.class,
                PingSpoofB.class,
                PingSpoofC.class,
                PingSpoofD.class,

                ProtocolA.class,
                ProtocolB.class,
                ProtocolC.class,
                ProtocolD.class,
                ProtocolE.class,
                ProtocolF.class,
                ProtocolG.class,
                ProtocolG2.class,
                ProtocolH.class,
                ProtocolI.class,
                ProtocolJ.class,
                ProtocolK.class,
                ProtocolL.class,
                ProtocolM.class,
                ProtocolN.class,
                ProtocolO.class,
                ProtocolP.class,
                ProtocolQ.class,
                ProtocolR.class,
                ProtocolT.class,
                ProtocolU.class,
                ProtocolV.class,
                ProtocolW.class,
                ProtocolX.class,
                ProtocolY.class,
                ProtocolZ.class,

                InteractA.class,
                InteractB.class,
                InteractC.class,
                InteractD.class,
                InteractE.class,
                InteractF.class,

                InventoryA.class,
                InventoryB.class,
                InventoryC.class,
                InventoryD.class,
                InventoryE.class,

                RefillA.class,
                RefillB.class,


        };
    }

    public static List<Check> loadChecks(final PlayerData data) {
        final List<Check> checkList = new ArrayList<>();
        for (final Constructor<?> constructor : CONSTRUCTORS) {
            try {
                checkList.add((Check) constructor.newInstance(data));
            } catch (final Exception exception) {
                System.err.println("Failed to load checks for " + data.getPlayer().getName() + " constructor: " + constructor.getName());
                exception.printStackTrace();
            }
        }
        return checkList;
    }

    public static void setup() {
        for (final Class<?> clazz : CHECKS) {
            if (Config.ENABLED_CHECKS.get(clazz.getSimpleName())) {
                try {
                    CONSTRUCTORS.add(clazz.getConstructor(PlayerData.class));
                } catch (final NoSuchMethodException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }
}