package com.signition.samskybridge.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.PortalCreateEvent;

public class PortalBlocker implements Listener {
    private final org.bukkit.plugin.Plugin plugin;
    public PortalBlocker(org.bukkit.plugin.Plugin plugin){ this.plugin = plugin; }

    @EventHandler
    public void onPortal(PlayerPortalEvent e){
        if (!plugin.getConfig().getBoolean("portal-block.enabled", false)) return;
        java.util.List<String> worlds = plugin.getConfig().getStringList("portal-block.worlds");
        if (worlds.isEmpty()) return;
        if (worlds.contains(e.getFrom().getWorld().getName())){
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onCreate(PortalCreateEvent e){
        if (!plugin.getConfig().getBoolean("portal-block.enabled", false)) return;
        java.util.List<String> worlds = plugin.getConfig().getStringList("portal-block.worlds");
        if (worlds.isEmpty() || e.getWorld() == null) return;
        if (worlds.contains(e.getWorld().getName())){
            e.setCancelled(true);
        }
    }
}
