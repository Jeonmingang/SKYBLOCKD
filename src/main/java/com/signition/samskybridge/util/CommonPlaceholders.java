
package com.signition.samskybridge.util;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.model.IslandInfo; // Create an interface alias if needed.

/**
 * Apply common island placeholders to strings/lists:
 * <xp>, <need>, <percent>, <gauge>, <level>
 */
public final class CommonPlaceholders {

    private CommonPlaceholders() {}

    public static List<String> apply(List<String> lines, Player player, IslandInfo island, LevelService levels) {
        if (lines == null) return Collections.emptyList();
        return lines.stream().map(s -> apply(s, player, island, levels)).collect(Collectors.toList());
    }

    public static String apply(String s, Player player, IslandInfo island, LevelService levels) {
        if (s == null) return null;
        if (island == null || levels == null) return s;

        int level = island.getLevel();
        long xp = island.getXp();
        long need = levels.getXpToNext(level, xp); // implement or adapt
        int percent = levels.getProgressPercent(level, xp); // implement or adapt
        String gauge = levels.getGauge(level, xp); // implement or adapt

        return s
            .replace("<xp>", String.valueOf(xp))
            .replace("<need>", String.valueOf(need))
            .replace("<percent>", String.valueOf(percent))
            .replace("<gauge>", gauge != null ? gauge : "")
            .replace("<level>", String.valueOf(level));
    }
}
