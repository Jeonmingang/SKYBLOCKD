package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.place.PlacedTracker;
import com.signition.samskybridge.xpguard.RecycleGuardService;
import com.signition.samskybridge.util.Configs;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.List;

public final class BlockXPListener implements Listener {
    private final Main plugin;
    private final LevelService level;
    private final PlacedTracker placedTracker;
    private final RecycleGuardService recycleGuard;
    private final List<String> allowedWorlds;

    public BlockXPListener(Main plugin, LevelService level, PlacedTracker placedTracker, RecycleGuardService recycleGuard){
        this.plugin = plugin;
        this.level = level;
        this.placedTracker = placedTracker;
        this.recycleGuard = recycleGuard;
        // pull once at startup; use Configs.ensureDefaults to guarantee keys exist
        this.allowedWorlds = plugin.getConfig().getStringList("xp.allowed-worlds");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e){
        Player p = e.getPlayer();
        World w = e.getBlockPlaced().getWorld();
        if (w == null) return;
        if (!allowedWorlds.isEmpty() && !allowedWorlds.contains(w.getName())) return;

        // If placing using a recycled item (pickup from player-placed drop), deny XP once per recycled item.
        boolean denyXp = recycleGuard.consumeIfRecycled(p.getUniqueId(), e.getBlockPlaced().getType());

        if (!denyXp){
            // Award XP normally
            level.onPlace(e);
        }

        // Mark location as player-placed for future drop tagging
        placedTracker.markPlaced(e.getBlockPlaced().getLocation());
    }
}