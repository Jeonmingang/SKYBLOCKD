
package com.signition.samskybridge.tab;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public final class TabPrefixRefresher {
    private final Plugin plugin;
    private BukkitTask task;

    public TabPrefixRefresher(Plugin plugin){ this.plugin = plugin; }

    public void start(String format, int refreshTicks){
        stop();
        task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable(){
            @Override public void run(){
                try {
                    Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
                    Team t = board.getTeam("samsky_all");
                    if (t == null) t = board.registerNewTeam("samsky_all");
                    t.setPrefix(color(format));
                    for (Player p : Bukkit.getOnlinePlayers()){
                        if (!t.hasEntry(p.getName())){
                            t.addEntry(p.getName());
                        }
                    }
                } catch (Throwable ex){
                    plugin.getLogger().warning("[TabPrefix] update failed: " + ex.getMessage());
                }
            }
        }, 40L, Math.max(20, refreshTicks));
    }

    public void stop(){
        if (task != null){
            task.cancel();
            task = null;
        }
    }

    private String color(String s){
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }
}
