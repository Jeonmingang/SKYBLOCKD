
package com.signition.samskybridge.level;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Hud;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

public class LevelService {
    private final Main plugin;
    private final DataStore store;

    public LevelService(Main plugin, DataStore store){
        this.plugin = plugin;
        this.store = store;
    }

    public long requiredXp(int level){
        long base = plugin.getConfig().getLong("level.base", 100);
        double percent = plugin.getConfig().getDouble("level.percent-increase", 10.0);
        long cap = plugin.getConfig().getLong("level.cap", Long.MAX_VALUE);
        if (level <= 0) return Math.min(base, cap);
        long xp = base;
        for (int i=1;i<=level;i++){
            xp = (long)Math.floor(xp * (1.0 + percent/100.0));
            if (xp > cap){ xp = cap; break; }
        }
        return xp;
    }

    public String progressBar(long cur, long need){
        int bars = 10;
        double pct = need <= 0 ? 1.0 : Math.min(1.0, cur * 1.0 / need);
        int filled = (int)Math.floor(pct * bars);
        StringBuilder sb = new StringBuilder("&8[");
        for (int i=0;i<bars;i++) sb.append(i<filled? "&a|" : "&7|");
        sb.append("&8] &f").append(cur).append("&7/&f").append(need).append(" &7(").append((int)(pct*100)).append("%)");
        return Text.color(sb.toString());
    }

    public void addIslandXp(IslandData is, long add){
        if (add <= 0) return;
        long cur = is.getXp() + add;
        int lv = is.getLevel();
        long need = requiredXp(lv);
        while (cur >= need){
            cur -= need; lv++;
            long nextNeed = requiredXp(lv);
            is.setLevel(lv); is.setXp(cur);
            Hud.levelUp(plugin, is, lv, nextNeed);
            need = nextNeed;
        }
        is.setXp(cur);
        store.saveAsync();
        // ranking may change
        Bukkit.getScheduler().runTask(plugin, new Runnable(){ public void run(){ plugin.getRankingService().refresh(); }});
    }

    public void onPlace(BlockPlaceEvent e){
        Player p = e.getPlayer();
        if (p == null) return;
        // simple XP example: grant 1 xp per block placed (can extend with blocks.yml)
        store.findByMember(p.getUniqueId()).ifPresent(is -> {
            addIslandXp(is, 1);
        });
    }
}
