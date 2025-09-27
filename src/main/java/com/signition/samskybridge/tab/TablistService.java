package com.signition.samskybridge.tab;

import com.signition.samskybridge.integration.BentoSync;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class TablistService implements Listener {

    private final Plugin plugin;
    private final BentoSync bento;
    private BukkitTask task;

    public TablistService(Plugin plugin, BentoSync bento){
        this.plugin = plugin;
        this.bento = bento;
    }

    public void start(){
        stop();
        long period = Math.max(1L, plugin.getConfig().getLong("tablist.update-interval-ticks", 40L));
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, 20L, period);
    }

    public void stop(){
        if (task != null){ task.cancel(); task = null; }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Bukkit.getScheduler().runTaskLater(plugin, () -> update(e.getPlayer()), 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        // no-op
    }

    private void updateAll(){
        for (Player p : Bukkit.getOnlinePlayers()){
            update(p);
        }
    }

    private void update(Player p){
        FileConfiguration c = plugin.getConfig();
        if (!c.getBoolean("tablist.enabled", true)) return;

        // header/footer
        List<String> header = c.getStringList("tablist.header");
        List<String> footer = c.getStringList("tablist.footer");
        String h = Text.color(String.join("\n", header));
        String f = Text.color(String.join("\n", footer));
        try { p.setPlayerListHeaderFooter(h, f); } catch (Throwable ignored){}

        // name-format
        String key;
        if (bento.isOwner(p, p.getLocation())) key = "owner";
        else if (bento.isMember(p, p.getLocation())) key = "member";
        else key = "visitor";
        String fmt = c.getString("tablist.name-format."+key, "%player_name%");

        String name = fmt.replace("%player_name%", p.getName())
                .replace("%island_level%", String.valueOf(Math.max(0, bento.getIslandLevel(p))))
                .replace("%island_rank%", String.valueOf(Math.max(0, bento.getIslandRank(p))));
        p.setPlayerListName(Text.color(name));
    }
}
