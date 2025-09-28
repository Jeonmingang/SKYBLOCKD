package com.signition.samskybridge.chat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class IslandChatService implements Listener {

    private final Plugin plugin;
    private final Set<UUID> toggled = new HashSet<UUID>();

    public IslandChatService(Plugin plugin){
        this.plugin = plugin;
    }

    public boolean toggle(Player player){
        return toggle(player.getUniqueId());
    }

    public boolean toggle(UUID id){
        if (id == null) return false;
        if (toggled.contains(id)) { toggled.remove(id); return false; }
        else { toggled.add(id); return true; }
    }

    public boolean isAlias(String s){
        List<String> list = plugin.getConfig().getStringList("chat.aliases");
        if (list == null || list.isEmpty()) list = java.util.Arrays.asList("섬채팅","islandchat");
        if (s == null) return false;
        for (String a : list){ if (s.equalsIgnoreCase(a)) return true; }
        return false;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        Player player = e.getPlayer();
        if (!toggled.contains(player.getUniqueId())) return;
        String msg = e.getMessage();
        for (Player online : Bukkit.getOnlinePlayers()){
            if (online.getWorld().equals(player.getWorld())){
                online.sendMessage("§a[섬채팅] §f" + player.getName() + ": §7" + msg);
            }
        }
        e.setCancelled(true);
    }

    public void sendToIsland(UUID islandOwnerUUID, Player sender, String msg){
        try {
            Player owner = Bukkit.getPlayer(islandOwnerUUID);
            if (owner != null){
                owner.sendMessage(msg);
            }
        } catch (Throwable ignored){}
    }

    public void reload(){
        // nothing to cache permanently; kept for compatibility with /섬 리로드 명령
        // if aliases or other state needed, it will read from config at use time.
    }
}
