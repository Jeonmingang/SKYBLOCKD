
package com.signition.samskybridge.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Placeholder applier for GUI lores:
 * Supports <xp>, <need>, <percent>, <gauge>, <level>
 * 
 * - NO dependency on Island or LevelService classes.
 * - Reads leveling formula & gauge settings directly from plugin config.
 */
public final class CommonPlaceholders {

    private CommonPlaceholders() {}

    /** Apply placeholders to a list of lines. */
    public static List<String> apply(List<String> lines, int level, long xp, JavaPlugin plugin) {
        if (lines == null) return Collections.emptyList();
        PlaceholderValues v = compute(level, xp, plugin);
        return lines.stream().map(s -> apply(s, v)).collect(Collectors.toList());
    }

    /** Apply placeholders to a single string. */
    public static String apply(String s, int level, long xp, JavaPlugin plugin) {
        if (s == null) return null;
        PlaceholderValues v = compute(level, xp, plugin);
        return apply(s, v);
    }

    // ------------ internal helpers ------------

    private static String apply(String s, PlaceholderValues v) {
        return s
            .replace("<xp>", String.valueOf(v.xp))
            .replace("<need>", String.valueOf(v.need))
            .replace("<percent>", String.valueOf(v.percent))
            .replace("<gauge>", v.gauge)
            .replace("<level>", String.valueOf(v.level));
    }

    private static PlaceholderValues compute(int level, long xp, JavaPlugin plugin) {
        double base = plugin.getConfig().getDouble("level.base-xp", 500.0);
        double per = plugin.getConfig().getDouble("level.per-level", 250.0);
        double growth = plugin.getConfig().getDouble("level.growth", 1.10);
        double mult = plugin.getConfig().getDouble("level.requirement-multiplier-percent", 100.0) / 100.0;
        int max = plugin.getConfig().getInt("level.max", 100);

        // requirement for NEXT level (XP needed within this level)
        double reqNext = requirementForLevel(level + 1, base, per, growth, mult)
                       - requirementForLevel(level, base, per, growth, mult);
        if (reqNext < 1) reqNext = 1; // avoid /0

        // percent within current level
        double pct = Math.max(0.0, Math.min(100.0, (xp / reqNext) * 100.0));
        int percent = (int)Math.round(pct);

        // remaining to next
        long need = (long)Math.max(0L, Math.round(reqNext) - xp);
        if (level >= max) { percent = 100; need = 0L; }

        // build gauge
        int len = Math.max(1, plugin.getConfig().getInt("level.gauge.length", 20));
        String full = plugin.getConfig().getString("level.gauge.full", "█");
        String empty = plugin.getConfig().getString("level.gauge.empty", "░");
        String gauge = buildGauge(len, percent, full, empty);

        return new PlaceholderValues(level, xp, need, percent, gauge);
    }

    private static double requirementForLevel(int targetLevel, double base, double per, double growth, double mult) {
        // total XP required to REACH 'targetLevel' from level 0
        double sum = 0.0;
        for (int l = 1; l <= targetLevel; l++) {
            double req = (base + per * (l - 1)) * Math.pow(growth, l - 1) * mult;
            sum += req;
        }
        return sum;
    }

    private static String buildGauge(int length, int percent, String full, String empty) {
        int fullCount = (int)Math.round(length * (percent / 100.0));
        if (fullCount < 0) fullCount = 0;
        if (fullCount > length) fullCount = length;
        StringBuilder sb = new StringBuilder(length * Math.max(1, full.length()));
        for (int i = 0; i < fullCount; i++) sb.append(full);
        for (int i = fullCount; i < length; i++) sb.append(empty);
        return sb.toString();
    }

    private static final class PlaceholderValues {
        final int level;
        final long xp;
        final long need;
        final int percent;
        final String gauge;
        PlaceholderValues(int level, long xp, long need, int percent, String gauge) {
            this.level = level;
            this.xp = xp;
            this.need = need;
            this.percent = percent;
            this.gauge = gauge;
        }
    }
}
