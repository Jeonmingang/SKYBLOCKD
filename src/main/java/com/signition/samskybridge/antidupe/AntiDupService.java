
package com.signition.samskybridge.antidupe;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AntiDupService {
    public static final class Rec {
        public final long ts;
        public final Location loc;
        public final Material type;
        Rec(long ts, Location loc, Material type){
            this.ts = ts;
            this.loc = loc.clone();
            this.type = type;
        }
    }

    private final Map<UUID, Deque<Rec>> recentBreaks = new ConcurrentHashMap<>();
    private volatile int ttlSec = 5;
    private volatile int radius = 2;
    private volatile boolean onlySameY = true;

    public void configure(int ttlSec, int radius, boolean onlySameY){
        this.ttlSec = ttlSec;
        this.radius = radius;
        this.onlySameY = onlySameY;
    }

    public void recordBreak(Player p, Location loc, Material type){
        Deque<Rec> q = recentBreaks.computeIfAbsent(p.getUniqueId(), k -> new ArrayDeque<Rec>(32));
        q.addLast(new Rec(System.currentTimeMillis(), loc, type));
        long cut = System.currentTimeMillis() - ttlSec * 1000L;
        while (!q.isEmpty() && q.peekFirst().ts < cut) q.pollFirst();
        while (q.size() > 64) q.pollFirst();
    }

    /** 설치 허용 + XP만 0 처리할지 판단 */
    public boolean isSuspiciousPlace(Player p, Location placeLoc, Material placing){
        Deque<Rec> q = recentBreaks.get(p.getUniqueId());
        if (q == null || q.isEmpty()) return false;
        long cut = System.currentTimeMillis() - ttlSec * 1000L;
        World w = placeLoc.getWorld();
        Iterator<Rec> it = q.descendingIterator();
        while (it.hasNext()){
            Rec r = it.next();
            if (r.ts < cut) break;
            if (r.type != placing) continue;
            if (w == null || !r.loc.getWorld().equals(w)) continue;
            if (onlySameY && r.loc.getBlockY() != placeLoc.getBlockY()) continue;
            if (r.loc.distanceSquared(placeLoc) <= (radius * radius)) return true;
        }
        return false;
    }
}
