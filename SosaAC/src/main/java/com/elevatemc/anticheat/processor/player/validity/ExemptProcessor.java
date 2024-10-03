package com.elevatemc.anticheat.processor.player.validity;

import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
public class ExemptProcessor {
    private boolean isExempt;
    private List<Player> exemptList = new ArrayList<>();
    private final PlayerData data;

    public boolean isExempt(final ExemptType exemptType) {
        return exemptType.getException().apply(data);
    }

    public boolean isExempt(final ExemptType... exemptTypes) {
        for (ExemptType exemt : exemptTypes) {
            if (exemt.getException().apply(data)) {
                return true;
            }
        }

        return false;
    }

    public boolean isExempt(final Function<PlayerData, Boolean> exception) {
        return exception.apply(data);
    }

    public void setExempt(final Player player) {
        if (!exemptList.contains(player)) {
            exemptList.add(player);
        } else {
            exemptList.remove(player);
            isExempt = false;
        }
    }

    public boolean isAnticheatExempt(final Player player) {
        return exemptList.contains(player);
    }
}
