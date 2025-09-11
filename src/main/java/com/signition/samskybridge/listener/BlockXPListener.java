package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.place.PlacedTracker;
import com.signition.samskybridge.xpguard.RecycleGuardService;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.List;

/**
 * Awards XP on normal placements; denies XP exactly once per recycled item usage.
 * Always marks the location as player-placed to enable future recycle tagging on break.
 */
public final class BlockXPListener implements Listener {

    private final Main plugin;
    private final LevelService level;
    private final PlacedTracker placedTracker;
    private final RecycleGuardService recycleGuard;

    public BlockXPListener(Main plugin, LevelService level, PlacedTracker placedTracker, RecycleGuardService recycleGuard){
        this.plugin = plugin;
        this.level = level;
        this.placedTracker = placedTracker;
        this.recycleGuard = recycleGuard;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e){
        Player p = e.getPlayer();
        if (p == null) return;

        // Allowed worlds guard (keep identical to LevelService behavior)
        List<String> allowedWorlds = plugin.getConfig().getStringList("xp.allowed-worlds");
        World w = e.getBlockPlaced().getWorld();
        if (w == null) return;
        if (!allowedWorlds.isEmpty() && !allowedWorlds.contains(w.getName())){
            // Still mark as placed to be consistent
            placedTracker.add(e.getBlockPlaced().getLocation());
            return;
        }

        boolean denyXp = recycleGuard.consumeIfRecycled(p.getUniqueId(), e.getBlockPlaced().getType());
        if (!denyXp){
            level.onPlace(e); // normal XP path
        }

        // Mark as player-placed for future drop tagging
        placedTracker.add(e.getBlockPlaced().getLocation());
    }
}