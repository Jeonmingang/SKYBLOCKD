package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.place.PlacedTracker;
import com.signition.samskybridge.antidupe.RecycleTokenService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/** On break of player-placed block: add a recycled token for that material. */
public class DropTagListener implements Listener {
    private final PlacedTracker placedTracker;
    private final RecycleTokenService recycle;

    public DropTagListener(Main plugin, PlacedTracker tracker, RecycleTokenService recycle){
        this.placedTracker = tracker;
        this.recycle = recycle;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e){
        if (!placedTracker.wasPlaced(e.getBlock().getLocation())) return;
        recycle.addToken(e.getPlayer().getUniqueId(), e.getBlock().getType());
        placedTracker.consume(e.getBlock().getLocation());
    }
}
