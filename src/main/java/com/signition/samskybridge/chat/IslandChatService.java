package com.signition.samskybridge.chat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;

public class IslandChatService implements Listener {

    private final Plugin plugin;
    private final Set<java.util.UUID> toggled = new HashSet<java.util.UUID>();

    public IslandChatService(Plugin plugin){
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public boolean toggle(Player p){
        if (toggled.contains(p.getUniqueId())) { toggled.remove(p.getUniqueId()); return false; }
        toggled.add(p.getUniqueId()); return true;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        if (!toggled.contains(e.getPlayer().getUniqueId())) return;
        e.setCancelled(true);
        for (Player t : Bukkit.getOnlinePlayers()){
            if (t.getWorld().equals(e.getPlayer().getWorld())){
                t.sendMessage("§d[섬채팅] §f" + e.getPlayer().getName() + "§7: §f" + e.getMessage());
            }
        }
    }
}

    public boolean isAlias(String s){
        java.util.List<String> list = plugin.getConfig().getStringList("chat.aliases");
        if (list == null) list = java.util.Arrays.asList("섬채팅","islandchat");
        if (s == null) return false;
        for (String a : list){ if (s.equalsIgnoreCase(a)) return true; }
        return false;
    }

    public void sendToIsland(java.util.UUID islandOwnerUUID, org.bukkit.entity.Player sender, String msg){
        try {
            org.bukkit.entity.Player owner = org.bukkit.Bukkit.getPlayer(islandOwnerUUID);
            if (owner != null){
                owner.sendMessage(msg);
            }
        } catch (Throwable ignored){}
    }
