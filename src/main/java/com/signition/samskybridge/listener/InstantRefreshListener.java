package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.rank.RankingService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerKickEvent;

public class InstantRefreshListener implements Listener {

    private final Main plugin;
    private final RankingService ranking;

    public InstantRefreshListener(Main plugin, RankingService ranking){
        this.plugin = plugin;
        this.ranking = ranking;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        // Slight delay to ensure scoreboard initialized by other plugins
        Bukkit.getScheduler().runTaskLater(plugin, () -> { if (plugin.getConfig().getBoolean("tab.apply-on-join", true)) ranking.applyTab(e.getPlayer()); }, 4L);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e){
        if (!plugin.getConfig().getBoolean("tab.apply-on-world-change", true)) return;
        ranking.applyTab(e.getPlayer());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e){
        if (e.getPlayer() instanceof Player){
            ranking.applyTab((Player)e.getPlayer());
        }
    }

    // If player runs island-related commands, rebuild ranks then refresh all
    @EventHandler
    public void onIslandCommands(PlayerCommandPreprocessEvent e){
        String msg = e.getMessage().toLowerCase();
        if (msg.startsWith("/섬") || msg.startsWith("/sky") || msg.startsWith("/island") || msg.startsWith("/is ")){
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try { ranking.rebuildRanks(); } catch (Throwable ignore){}
                try { ranking.refreshTabAll(); } catch (Throwable ignore){}
            }, 4L);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e){
        Bukkit.getScheduler().runTaskLater(plugin, () -> ranking.applyTab(e.getPlayer()), 5L);
    }

    @EventHandler
    public void onKick(PlayerKickEvent e){
        ranking.removeFromTeams(e.getPlayer());
    }
}
