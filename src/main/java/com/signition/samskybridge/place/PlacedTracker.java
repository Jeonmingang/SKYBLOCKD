package com.signition.samskybridge.place;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks player-placed block locations so we can tag their drops later.
 * Thread-safe, string-keyed by (worldUUID:x:y:z) to avoid Location mutability pitfalls.
 */
public final class PlacedTracker {

    private final Set<String> placed = ConcurrentHashMap.newKeySet();

    private static String keyOf(Location loc){
        World w = loc.getWorld();
        if (w == null) return "null:0:0:0";
        return w.getUID().toString() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    /** Mark a location as player-placed. */
    public void add(Location loc){
        if (loc == null) return;
        placed.add(keyOf(loc));
    }

    /** Returns true if the location is marked player-placed (without consuming). */
    public boolean wasPlaced(Location loc){
        if (loc == null) return false;
        return placed.contains(keyOf(loc));
    }

    /**
     * Call when the block breaks; returns true if it was a player-placed block
     * and consumes the mark so it won't be double-counted.
     */
    public boolean consume(Location loc){
        if (loc == null) return false;
        String k = keyOf(loc);
        if (placed.remove(k)){
            return true;
        }
        return false;
    }
}