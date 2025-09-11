
package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DropPickupTracker implements Listener {
    private final Main plugin;
    private final Map<Location, Long> lastBreakAt = new HashMap<>();
    private final Map<UUID, Long> lastPickupAt = new HashMap<>();

    public DropPickupTracker(Main plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e){
        lastBreakAt.put(e.getBlock().getLocation(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent e){
        if (e.getEntityType() != EntityType.PLAYER) return;
        lastPickupAt.put(((Player)e.getEntity()).getUniqueId(), System.currentTimeMillis());
    }

    public boolean isRecycleLoop(Player p, Location placeLoc){
        long ttl = plugin.getConfig().getLong("xp.recent-ttl-ms", 5000L);
        Long br = lastBreakAt.get(placeLoc);
        Long pk = lastPickupAt.get(p.getUniqueId());
        long now = System.currentTimeMillis();
        return br != null && pk != null && (now - br) <= ttl && pk >= br && (now - pk) <= ttl;
    }
}
