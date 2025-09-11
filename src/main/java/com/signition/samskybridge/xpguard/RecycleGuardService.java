package com.signition.samskybridge.xpguard;

import org.bukkit.Material;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks per-player counts of "recycled" items (items picked up from player-placed block drops).
 * If a player places a block and still has recycled count for that material, XP will be denied once per consumed item.
 * Java 8 compatible.
 */
public final class RecycleGuardService {

    // player UUID -> (Material -> recycled count)
    private final Map<UUID, Map<Material, Integer>> recycled = new ConcurrentHashMap<UUID, Map<Material, Integer>>();

    /** Increment recycled counter for the given player/material by amount (>=1). */
    public void recordPickup(UUID player, Material mat, int amount){
        if (player == null || mat == null || amount <= 0) return;
        Map<Material, Integer> byMat = recycled.get(player);
        if (byMat == null){
            byMat = new ConcurrentHashMap<Material, Integer>();
            recycled.put(player, byMat);
        }
        Integer prev = byMat.get(mat);
        if (prev == null) prev = 0;
        byMat.put(mat, prev + amount);
    }

    /**
     * Consume one recycled token for the player's given material, if available.
     * @return true if a recycled item was available and consumed (thus XP should be denied)
     */
    public boolean consumeIfRecycled(UUID player, Material mat){
        if (player == null || mat == null) return false;
        Map<Material, Integer> byMat = recycled.get(player);
        if (byMat == null) return false;
        Integer cnt = byMat.get(mat);
        if (cnt == null || cnt <= 0) return false;
        int next = cnt - 1;
        if (next <= 0){
            byMat.remove(mat);
            if (byMat.isEmpty()) recycled.remove(player);
        } else {
            byMat.put(mat, next);
        }
        return true;
    }

    /** Clear all recycled counters for a player (e.g., on logout). */
    public void clear(UUID player){
        if (player == null) return;
        recycled.remove(player);
    }
}