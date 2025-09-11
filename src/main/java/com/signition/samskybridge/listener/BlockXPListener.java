package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.place.PlacedTracker;
import com.signition.samskybridge.antidupe.RecycleTokenService;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import java.util.List;

/** XP rules: fresh inventory block => grant; recycled token => consume token & deny */
public class BlockXPListener implements Listener {
    private final Main plugin;
    private final LevelService level;
    private final PlacedTracker placed;
    private final RecycleTokenService recycle;

    public BlockXPListener(Main plugin, DataStore store, LevelService level, PlacedTracker tracker, RecycleTokenService recycle){
        this.plugin = plugin;
        this.level = level;
        this.placed = tracker;
        this.recycle = recycle;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlace(BlockPlaceEvent e){
        List<String> worlds = plugin.getConfig().getStringList("xp.allowed-worlds");
        if (!worlds.isEmpty() && !worlds.contains(e.getBlock().getWorld().getName())) return;

        Material mat = e.getBlockPlaced().getType();
        if (recycle.consumeIfExists(e.getPlayer().getUniqueId(), mat)){
            placed.markPlaced(e.getBlockPlaced().getLocation()); // keep lineage
            return; // deny XP
        }
        // fresh inventory consumption -> grant
        level.onPlace(e);
        placed.markPlaced(e.getBlockPlaced().getLocation());
    }
}
