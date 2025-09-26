package com.signition.samskybridge.upgrade;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Text;
import org.bukkit.entity.Player;

/**
 * Minimal, additive level requirement checks for mine/farm upgrade.
 * Merge the checkRequiredLevelOrDeny(...) into your existing upgrade flow
 * BEFORE money is charged and level values are mutated.
 */
public class UpgradeService {

    private final Main plugin;

    public UpgradeService(Main plugin) {
        this.plugin = plugin;
    }

    // Example wrapper – adjust to your existing signature/flow
    public boolean tryUpgradeMine(Player p, IslandData is, int nextLevel) {
        if (!checkRequiredLevelOrDeny(p, is, "upgrades.mine.level-required.", nextLevel)) {
            return false;
        }
        // call your existing upgrade routine here
        // upgradeMineCore(p, is, nextLevel);
        return true;
    }

    public boolean tryUpgradeFarm(Player p, IslandData is, int nextLevel) {
        if (!checkRequiredLevelOrDeny(p, is, "upgrades.farm.level-required.", nextLevel)) {
            return false;
        }
        // call your existing upgrade routine here
        // upgradeFarmCore(p, is, nextLevel);
        return true;
    }

    // --- helper ---
    private boolean checkRequiredLevelOrDeny(Player p, IslandData is, String pathPrefix, int nextLevel) {
        int required = plugin.getConfig().getInt(pathPrefix + nextLevel, 0);
        int currentIslandLevel;
        try {
            currentIslandLevel = is.getLevel(); // prefer IslandData API
        } catch (Throwable t) {
            // If IslandData has different accessor, adapt here (e.g., is.level or levelService.getLevel(is))
            currentIslandLevel = 0;
        }
        if (currentIslandLevel < required) {
            p.sendMessage(Text.color("&c요구 섬 레벨: &f" + required + " &7(현재: " + currentIslandLevel + ")"));
            return false;
        }
        return true;
    }
}