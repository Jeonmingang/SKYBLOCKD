
package com.signition.samskybridge.tab;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class TabService {
    private final Plugin plugin;
    private BukkitTask task;
    public TabService(Plugin plugin) { this.plugin = plugin; }
    public void start() {
        if (!plugin.getConfig().getBoolean("tablist.enabled", false)) return;
        stop();
        task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override public void run() { tick(); }
        }, 40L, 80L);
    }
    public void stop() { if (task != null) { task.cancel(); task = null; } }
    private void tick() {
        boolean onlyIslandWorlds = plugin.getConfig().getBoolean("tablist.only-in-island-worlds", true);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (onlyIslandWorlds && !isInIslandWorld(p)) continue;
            boolean owner = isIslandOwner(p);
            boolean same = true; // simplified
            String prefix;
            if (owner) {
                prefix = plugin.getConfig().getString(same ? "tablist.same-island-owner-prefix" : "tablist.owner-prefix", "");
            } else if (isVisitor(p)) {
                prefix = plugin.getConfig().getString("tablist.visitor-prefix", "");
            } else {
                prefix = plugin.getConfig().getString(same ? "tablist.same-island-member-prefix" : "tablist.member-prefix", "");
            }
            String base = p.getName();
            String listName = org.bukkit.ChatColor.translateAlternateColorCodes('&', prefix + base);
            if (listName.length() > 16) listName = listName.substring(0, 16);
            p.setPlayerListName(listName);
        }
    }
    private boolean isInIslandWorld(Player p) {
        try { return com.signition.samskybridge.feature.FeatureService.isIslandWorld(p.getWorld()); }
        catch (Throwable t) { return true; }
    }
    private boolean isIslandOwner(Player p) {
        try { return com.signition.samskybridge.feature.FeatureService.islandOwner(p); }
        catch (Throwable t) { return p.hasPermission("samskybridge.island.owner"); }
    }
    private boolean isVisitor(Player p) {
        try { return !com.signition.samskybridge.feature.FeatureService.islandMember(p); }
        catch (Throwable t) { return false; }
    }
}
