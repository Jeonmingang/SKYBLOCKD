package com.signition.samskybridge.cmd;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.feature.FeatureService;
import com.signition.samskybridge.util.Text;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Guard helpers for /섬 설치 <광산|농장> entry.
 * Merge these methods into your existing IslandCommand class if you already have one.
 */
public class IslandCommand {

    private final Main plugin;
    private final FeatureService features;

    public IslandCommand(Main plugin) {
        this.plugin = plugin;
        this.features = plugin.getFeatureService();
    }

    // Example entry points – adapt to your command dispatcher
    public void installMine(Player p) {
        if (!checkInstallGuards(p)) return;
        Location loc = p.getLocation();
        if (!isInsideOwnIsland(p, loc)) return;
        features.placeMine(p, loc);
        p.sendMessage(Text.color("&a광산을 설치했습니다."));
    }

    public void installFarm(Player p) {
        if (!checkInstallGuards(p)) return;
        Location loc = p.getLocation();
        if (!isInsideOwnIsland(p, loc)) return;
        features.placeFarm(p, loc);
        p.sendMessage(Text.color("&a농장을 설치했습니다."));
    }

    // ---- guard logic ----
    private boolean checkInstallGuards(Player p) {
        World w = p.getWorld();
        String onlyWorld = plugin.getConfig().getString("island-world", "bskyblock_world");
        if (!w.getName().equalsIgnoreCase(onlyWorld)) {
            p.sendMessage(Text.color("&c해당 월드에서만 설치할 수 있습니다: &f" + onlyWorld));
            return false;
        }
        if (!features.islandOwner(p)) {
            p.sendMessage(Text.color("&c섬장만 설치할 수 있습니다."));
            return false;
        }
        return true;
    }

    private boolean isInsideOwnIsland(Player p, Location loc) {
        // If your FeatureService uses a different method name for "own island" check,
        // replace the call below accordingly.
        try {
            boolean ok = features.isInOwnIsland(p, loc);
            if (!ok) {
                p.sendMessage(Text.color("&c자신의 섬 내부에서만 설치할 수 있습니다."));
            }
            return ok;
        } catch (Throwable t) {
            // Fallback: allow if any island region (least strict). Change if needed.
            boolean ok = features.isInFarmRegion(p, loc); // placeholder region check
            if (!ok) {
                p.sendMessage(Text.color("&c자신의 섬 내부에서만 설치할 수 있습니다."));
            }
            return ok;
        }
    }
}