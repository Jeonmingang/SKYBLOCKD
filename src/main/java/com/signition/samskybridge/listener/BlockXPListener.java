
package com.signition.samskybridge.listener;
import com.signition.samskybridge.level.LevelService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
public class BlockXPListener implements Listener {
    private final java.util.Map<String, Long> recentBreaks = new java.util.concurrent.ConcurrentHashMap<String, Long>();
    private long ttlMs = 5000;
    private final java.util.Map<String, Long> recentBreaks = new java.util.HashMap<String, Long>();
    private long ttlMs = 5000;
  private final LevelService level;
  public BlockXPListener(com.signition.samskybridge.Main plugin, LevelService level){ this.level=level; }
  @EventHandler(ignoreCancelled=true)
  public void onPlace(BlockPlaceEvent e){ level.onPlace(e); }
}


public void purgeExpired(){
    long cutoff = System.currentTimeMillis() - ttlMs;
    java.util.Iterator<java.util.Map.Entry<String,Long>> it = recentBreaks.entrySet().iterator();
    while (it.hasNext()){
        java.util.Map.Entry<String,Long> en = it.next();
        if (en.getValue() < cutoff) it.remove();
    }
    // cap map size
    int max = 20000;
    if (recentBreaks.size() > max){
        int toRemove = recentBreaks.size() - max;
        java.util.Iterator<String> keys = recentBreaks.keySet().iterator();
        while (toRemove > 0 && keys.hasNext()){ keys.remove(); toRemove--; }
    }
}
