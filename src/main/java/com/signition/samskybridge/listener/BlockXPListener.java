
package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.antidupe.AntiDupService;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.level.LevelService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockXPListener implements Listener {
    private final Main plugin;
    private final DataStore store;
    private final LevelService level;
    private final AntiDupService antiDup = new AntiDupService();

    public BlockXPListener(Main plugin, DataStore store, LevelService level){
        this.plugin = plugin;
        this.store = store;
        this.level = level;
        int ttl = plugin.getConfig().getInt("xp_anti_dup.ttl_seconds", 5);
        int radius = plugin.getConfig().getInt("xp_anti_dup.radius", 2);
        boolean sameY = plugin.getConfig().getBoolean("xp_anti_dup.only_same_y", true);
        antiDup.configure(ttl, radius, sameY);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e){
        antiDup.recordBreak(e.getPlayer(), e.getBlock().getLocation(), e.getBlock().getType());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlace(BlockPlaceEvent e){
        // Allowed worlds check (reuse existing config)
        java.util.List<String> worlds = plugin.getConfig().getStringList("xp.allowed-worlds");
        if (!worlds.isEmpty() && !worlds.contains(e.getBlock().getWorld().getName())) return;

        // If suspicious, allow place but DO NOT award XP
        boolean suspicious = antiDup.isSuspiciousPlace(e.getPlayer(), e.getBlockPlaced().getLocation(), e.getItemInHand().getType());
        if (suspicious){
            // No chat spam; just skip XP
            return;
        }
        level.onPlace(e); // delegate to LevelService (will compute/award XP)
    }
}
