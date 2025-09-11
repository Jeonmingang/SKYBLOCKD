
package com.signition.samskybridge.place;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class PlacedTracker {
    private final Set<String> placed = ConcurrentHashMap.newKeySet();

    private String keyOf(Location loc){
        World w = loc.getWorld();
        if (w == null) return "null:0:0:0";
        return w.getUID().toString()+":"+loc.getBlockX()+":"+loc.getBlockY()+":"+loc.getBlockZ();
    }

    public void markPlaced(Location loc){
        placed.add(keyOf(loc));
    }

    public boolean wasPlaced(Location loc){
        return placed.contains(keyOf(loc));
    }

    /** call when block breaks; returns true if it was a player-placed block */
    public boolean consume(Location loc){
        String k = keyOf(loc);
        if (placed.contains(k)){
            placed.remove(k);
            return true;
        }
        return false;
    }
}
