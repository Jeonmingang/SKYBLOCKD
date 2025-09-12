
package com.signition.samskybridge.xpguard;

import org.bukkit.Material;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks per-player counts of "recycled" items (drops from player-placed blocks).
 * If a player places a block using a recycled item, XP must be denied once per recycled item.
 */
public final class RecycleGuardService {

    private final Map<UUID, EnumMap<Material, Integer>> recycled = new ConcurrentHashMap<>();

    /** Adds count of recycled items for a player. */
    public void add(UUID playerId, Material mat, int count){
        if (playerId == null || mat == null || count <= 0) return;
        EnumMap<Material, Integer> m = recycled.computeIfAbsent(playerId, k -> new EnumMap<>(Material.class));
        m.put(mat, m.getOrDefault(mat, 0) + count);
    }

    /**
     * Consumes one recycled item of the given material for the player, returning true if consumed.
     * If consumed, award should be denied once for this placement.
     */
    public boolean consume(UUID playerId, Material mat){
        if (playerId == null || mat == null) return false;
        EnumMap<Material, Integer> m = recycled.get(playerId);
        if (m == null) return false;
        Integer have = m.get(mat);
        if (have == null || have <= 0) return false;
        int left = have - 1;
        if (left <= 0){
            m.remove(mat);
            if (m.isEmpty()) recycled.remove(playerId);
        }else{
            m.put(mat, left);
        }
        return true;
    }

    /** Clears all counts for a player (e.g., on quit). */
    public void clear(UUID playerId){
        if (playerId != null) recycled.remove(playerId);
    }

    public boolean consumeIfRecycled(java.util.UUID playerId, org.bukkit.Material mat){
        return consume(playerId, mat);
    }
}
