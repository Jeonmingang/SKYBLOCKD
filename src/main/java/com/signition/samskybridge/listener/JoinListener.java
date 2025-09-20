package com.signition.samskybridge.listener;

import com.signition.samskybridge.rank.RankingService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    private final RankingService ranking;
    public JoinListener(RankingService ranking){
        this.ranking = ranking;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        try {
            int rank = ranking.getRankOf(e.getPlayer().getUniqueId());
            ranking.applyPrefix(e.getPlayer(), rank);
        } catch (Throwable ignored){}
    }
}
