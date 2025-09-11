package com.signition.samskybridge.place;

import org.bukkit.Location;
import org.bukkit.World;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** tracks coordinates of blocks placed by players so that break→token can be attributed */
public final class PlacedTracker {
    private final Set<String> placed = ConcurrentHashMap.newKeySet();
    private String keyOf(Location loc){
        World w = loc.getWorld();
        if (w == null) return "null:0:0:0";
        return w.getUID().toString()+":"+loc.getBlockX()+":"+loc.getBlockY()+":"+loc.getBlockZ();
    }
    public void markPlaced(Location loc){ placed.add(keyOf(loc)); }
    public boolean wasPlaced(Location loc){ return placed.contains(keyOf(loc)); }
    public boolean consume(Location loc){ return placed.remove(keyOf(loc)); }
}
