package com.signition.samskybridge.tab;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.feature.FeatureService;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * Simple tab prefix updater (owner/member/visitor).
 * Merge updateTabPrefix(...) into your existing scheduler/listener if you already manage tab UI.
 */
public class TabService implements Listener {

    private final Main plugin;
    private final FeatureService features;

    public TabService(Main plugin) {
        this.plugin = plugin;
        this.features = plugin.getFeatureService();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override public void run() {
                updateTabPrefix(e.getPlayer());
            }
        }, 1L);
    }

    @EventHandler
    public void onWorld(PlayerChangedWorldEvent e) {
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override public void run() {
                updateTabPrefix(e.getPlayer());
            }
        }, 1L);
    }

    public void updateTabPrefix(Player p) {
        String ownerPrefix = plugin.getConfig().getString("ranking.tab-prefix.owner", "&7[ &a섬장 &7] &r");
        String memberPrefix = plugin.getConfig().getString("ranking.tab-prefix.member", "&7[ &a섬원 &7] &r");
        String visitorPrefix = plugin.getConfig().getString("ranking.tab-prefix.visitor", "&7[ 등록없음 ] &r");

        String prefix;
        try {
            if (features.islandOwner(p)) prefix = ownerPrefix;
            else if (features.islandMember(p)) prefix = memberPrefix;
            else prefix = visitorPrefix;
        } catch (Throwable t) {
            prefix = visitorPrefix;
        }

        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = board.getTeam("samskybridge_tab");
        if (team == null) {
            team = board.registerNewTeam("samskybridge_tab");
        }
        team.setPrefix(ChatColor.translateAlternateColorCodes('&', prefix));
        if (!team.hasEntry(p.getName())) team.addEntry(p.getName());
        p.setScoreboard(board);
    }
}