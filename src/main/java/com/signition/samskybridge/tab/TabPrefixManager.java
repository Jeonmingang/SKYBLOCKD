
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
        if (format == null || format.trim().isEmpty()){
            String alt = plugin.getConfig().getString("tab_prefix.format_dynamic", null);
            if (alt == null || alt.trim().isEmpty()){
                alt = plugin.getConfig().getString("tab-prefix", "&7[ &a섬 랭킹 <rank>위 &7| &fLv.&b<level> &7]");
            }
            format = alt;
        }
        final String fmt = format;
        task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable(){
            @Override public void run(){ tryUpdate(fmt); }
        }, 40L, Math.max(20, refreshTicks));
    }
        }, 40L, Math.max(20, refreshTicks));
    }

    public void stop(){
        if (task != null){ task.cancel(); task = null; }
    }

    private void tryUpdate(String format){
        try {
            for (Player p : Bukkit.getOnlinePlayers()){
                String prefix = resolve(format, p.getUniqueId());
                p.setPlayerListName(color(prefix) + " " + p.getName());
            }
        } catch (Throwable ex){
            plugin.getLogger().warning("[TabPrefix] update failed: " + ex.getMessage());
        }
    }
        } catch (Throwable ex){
            plugin.getLogger().warning("[TabPrefix] update failed: " + ex.getMessage());
        }
    }

    private String resolve(String fmt, UUID uid){
        long level = 0L;
        int rank = -1;
        try { rank = ranking.getRank(uid); } catch (Throwable ignore){ rank = -1; }
        String label = plugin.getConfig().getString("tab_prefix.unranked_label", "등록안됨");
        String rankStr = (rank < 1 ? label : String.valueOf(rank));
        com.signition.samskybridge.data.IslandData is = store.findByMember(uid).orElse(null);
        if (is != null) level = is.getLevel();
        String s = fmt.replace("<rank>", rankStr).replace("<level>", String.valueOf(level));
        return s;
    }

    private String color(String s){
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }
}
