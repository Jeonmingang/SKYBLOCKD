
package com.signition.samskybridge.xpguard;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SlotGuardService {
    private final Map<String, Long> awarded = new ConcurrentHashMap<String, Long>();
    private volatile long ttlMillis = -1L; // <0 = permanent

    public void configureSeconds(int ttlSeconds){
        if (ttlSeconds < 0) this.ttlMillis = -1L;
        else this.ttlMillis = ttlSeconds * 1000L;
    }

    private String keyOf(Location loc){
        World w = loc.getWorld();
        if (w == null) return "null:0:0:0";
        return w.getUID().toString()+":"+loc.getBlockX()+":"+loc.getBlockY()+":"+loc.getBlockZ();
    }

    public boolean alreadyAwarded(Location loc){
        String k = keyOf(loc);
        Long ts = awarded.get(k);
        if (ts == null) return false;
        if (ttlMillis < 0) return true;
        long now = System.currentTimeMillis();
        return now - ts.longValue() <= ttlMillis;
    }

    public void markAwarded(Location loc){
        String k = keyOf(loc);
        awarded.put(k, System.currentTimeMillis());
    }
}
