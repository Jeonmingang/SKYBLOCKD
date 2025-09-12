
package com.signition.samskybridge.tab;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.rank.RankingService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.UUID;

public final class TabPrefixManager {
    private final Main plugin;
    private final DataStore store;
    private final RankingService ranking;
    private BukkitTask task;

    public TabPrefixManager(Main plugin, DataStore store, RankingService ranking){
        this.plugin = plugin;
        this.store = store;
        this.ranking = ranking;
    }

    public void start(String format, int refreshTicks){
        stop();
        task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable(){
            @Override public void run(){ tryUpdate(format); }
        }, 40L, Math.max(20, refreshTicks));
    }

    public void stop(){
        if (task != null){ task.cancel(); task = null; }
    }

    private void tryUpdate(String format){
        try {
            Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
            Team t = board.getTeam("samsky_all");
            if (t == null) t = board.registerNewTeam("samsky_all");
            for (Player p : Bukkit.getOnlinePlayers()){
                String prefix = resolve(format, p.getUniqueId());
                t.setPrefix(color(prefix));
                if (!t.hasEntry(p.getName())) t.addEntry(p.getName());
            }
        } catch (Throwable ex){
            plugin.getLogger().warning("[TabPrefix] update failed: " + ex.getMessage());
        }
    }

    
    
    private String resolve(String fmt, UUID uid){
        long level = 0L;
        int r = ranking.getRank(uid);
        IslandData is = store.findByMember(uid).orElse(null);
        if (is != null) level = is.getLevel();

        String unranked = plugin.getConfig().getString("ranking.unranked-label", "등록안됨");
        boolean templateHasSuffix = fmt != null && fmt.contains("<rank>위");

        String rankLabel;
        if (r < 0) rankLabel = unranked;
        else rankLabel = templateHasSuffix ? String.valueOf(r) : (r + "위");

        String s = (fmt == null ? "" : fmt).replace("<rank>", rankLabel)
                                           .replace("<level>", String.valueOf(level));
        return s;
    }



    private String color(String s){
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }
}
