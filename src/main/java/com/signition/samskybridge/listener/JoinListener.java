
package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.rank.RankingService;
import com.signition.samskybridge.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    private final Main plugin;
    private final RankingService rank;
    private final LevelService levels;

    public JoinListener(Main plugin, RankingService rank, LevelService levels){
        this.plugin = plugin;
        this.rank = rank;
        this.levels = levels;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        String pref = rank.tabPrefixFor(p);
        p.setPlayerListName(Text.col(pref + p.getName()));
    }
}
