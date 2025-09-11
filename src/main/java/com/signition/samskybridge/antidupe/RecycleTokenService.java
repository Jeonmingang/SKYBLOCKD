package com.signition.samskybridge.antidupe;

import org.bukkit.Material;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** per-player, per-material recycled tokens to block drop→pickup→replace loops */
public final class RecycleTokenService {
    private final Map<UUID, Map<Material, Integer>> tokens = new ConcurrentHashMap<>();

    public void addToken(UUID playerId, Material mat){
        tokens.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>()).merge(mat, 1, Integer::sum);
    }
    public boolean consumeIfExists(UUID playerId, Material mat){
        Map<Material, Integer> m = tokens.get(playerId);
        if (m == null) return false;
        Integer c = m.get(mat);
        if (c == null || c <= 0) return false;
        if (c == 1) m.remove(mat); else m.put(mat, c - 1);
        return true;
    }
    public void clear(UUID playerId){ tokens.remove(playerId); }
}
