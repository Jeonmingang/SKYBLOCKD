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
    private final org.bukkit.plugin.Plugin plugin;
    private final com.signition.samskybridge.integration.BentoSync bento;
    private int taskId = -1;
    public TablistService(org.bukkit.plugin.Plugin plugin, com.signition.samskybridge.integration.BentoSync bento){
        this.plugin = plugin;
        this.bento = bento;
    }
    public void start(){
        int interval = plugin.getConfig().getInt("tablist.update-interval-ticks", 40);
        if (taskId != -1) return;
        taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable(){
            public void run(){ /* actual update handled elsewhere in listener */ }
        }, interval, interval);
    }

    private String ownerPrefix, memberPrefix, visitorPrefix, sameOwnerPrefix, sameMemberPrefix; 
    private boolean sameHighlight;

    private boolean shouldApply(org.bukkit.entity.Player p){
        if ("all".equalsIgnoreCase(scope)) return true;
        if ("island-only".equalsIgnoreCase(scope)) return islandWorlds.isEmpty() || islandWorlds.contains(p.getWorld().getName());
        if ("allowed".equalsIgnoreCase(scope)) return allowedWorlds.isEmpty() || allowedWorlds.contains(p.getWorld().getName());
        return true;
    }
    
    private final java.util.Set<String> islandWorlds = new java.util.HashSet<String>();
    private final String scope;
    private final java.util.Set<String> allowedWorlds = new java.util.HashSet<String>();


    private final Plugin plugin;
    private final BentoSync bento;
    private final FileConfiguration cfg;
    private BukkitTask task;

    public TablistService(Plugin plugin, BentoSync bento) {
        this.plugin = plugin; this.bento = bento; this.cfg = plugin.getConfig();
        ownerPrefix = cfg.getString("tablist.owner-prefix", "");
        memberPrefix = cfg.getString("tablist.member-prefix", "");
        visitorPrefix = cfg.getString("tablist.visitor-prefix", "");
        sameOwnerPrefix = cfg.getString("tablist.same-island-owner-prefix", ownerPrefix);
        sameMemberPrefix = cfg.getString("tablist.same-island-member-prefix", memberPrefix);
        sameHighlight = cfg.getBoolean("tablist.same-island-highlight", false);
        this.scope = cfg.getString("tablist.scope", "all");
        this.allowedWorlds.addAll(cfg.getStringList("tablist.allowed-worlds"));
        java.util.List<String> iw = cfg.getStringList("features.require.island-worlds");
        if (!iw.isEmpty()) this.islandWorlds.addAll(iw);
        else { String w = cfg.getString("worlds.island-world", ""); if (w != null && !w.isEmpty()) this.islandWorlds.add(w); }
        if (cfg.getBoolean("tablist.enabled", false)) {
            int ticks = cfg.getInt("tablist.update-interval-ticks", 40);
            Bukkit.getPluginManager().registerEvents(this, plugin);
            task = Bukkit.getScheduler().runTaskTimer(plugin, this::refreshAll, 40L, Math.max(1, ticks));
        }
    }

    public void shutdown(){ if (task != null) task.cancel(); }

    private void refreshAll(){ for (Player p : Bukkit.getOnlinePlayers()) apply(p); }

    private void apply(Player p){ if (!shouldApply(p)) return;
        List<String> header = cfg.getStringList("tablist.header");
        List<String> footer = cfg.getStringList("tablist.footer");
        String h = String.join("\n", header);
        String f = String.join("\n", footer);
        h = placeholders(p, h);
        f = placeholders(p, f);
        try { p.setPlayerListHeaderFooter(Text.color(h), Text.color(f)); } catch (Throwable ignored) {}
        String fmt = cfg.getString("tablist.name-format.visitor", "&7[등록없음] %player_name%");
        if (bento.isOwner(p, p.getLocation())) fmt = cfg.getString("tablist.name-format.owner", "&d[섬장] %player_name%");
        else if (bento.isMemberOrOwnerAt(p, p.getLocation())) fmt = cfg.getString("tablist.name-format.member", "&a[섬원] %player_name%");
        String name = Text.color(placeholders(p, fmt));
        if (name.length() > 16) name = name.substring(0, 16);
        try { p.setPlayerListName(name); } catch (Throwable ignored) {}
    }

    private String placeholders(Player p, String s){
        if (s == null) return "";
        String out = s;
        out = out.replace("%player_name%", p.getName());
        out = out.replace("%server_online%", String.valueOf(Bukkit.getOnlinePlayers().size()));
        out = out.replace("%server_max_players%", String.valueOf(Bukkit.getMaxPlayers()));
        int level = bento.getIslandLevel(p);
        out = out.replace("%island_level%", String.valueOf(level));
        out = out.replace("%island_rank%", String.valueOf(bento.getIslandRank(p)));
        out = out.replace("%team_count%", String.valueOf(bento.getTeamCount(p)));
        out = out.replace("%team_max%", String.valueOf(bento.getTeamMax(p)));
        return out;
    }

    @EventHandler public void onJoin(PlayerJoinEvent e){ Bukkit.getScheduler().runTaskLater(plugin, () -> apply(e.getPlayer()), 40L); }
    @EventHandler public void onQuit(PlayerQuitEvent e){}
}
