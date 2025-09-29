
package kr.minkang.samskybridge;

public class Leveling {
    public static int requiredXpForLevel(org.bukkit.plugin.Plugin plugin, int level) {
        if (level <= 0) level = 1;
        double base = plugin.getConfig().getDouble("level.base-xp", 1000.0);
        double growth = plugin.getConfig().getDouble("level.growth-percent", 10.0) / 100.0;
        double req = base * Math.pow(1.0 + growth, Math.max(0, level - 1));
        return (int)Math.round(req);
    }
    public static int nextLevelRequiredXp(org.bukkit.plugin.Plugin plugin, int currentLevel) {
        return requiredXpForLevel(plugin, currentLevel + 1);
    }
}
