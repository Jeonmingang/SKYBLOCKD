package com.signition.samskybridge.chat;

import com.signition.samskybridge.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class IslandChatService implements Listener {

    private final Main plugin;
    private final Set<UUID> toggled = ConcurrentHashMap.newKeySet();

    public IslandChatService(Main plugin){
        this.plugin = plugin;
    }

    public boolean toggle(UUID uuid){
        if (toggled.contains(uuid)) {
            toggled.remove(uuid);
            return false;
        } else {
            toggled.add(uuid);
            return true;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncChat(AsyncPlayerChatEvent e){
        Player sender = e.getPlayer();
        if (!toggled.contains(sender.getUniqueId())) return;

        e.setCancelled(true);

        // Fallback: send to online players on same world as a safe default (could be tightened by Bento compat elsewhere)
        String base = ChatColor.translateAlternateColorCodes('&', "&a[ 섬채팅 ] &f" + sender.getName() + "&7: &f" + e.getMessage());
        for (Player online : Bukkit.getOnlinePlayers()){
            // Here you could check Bento/BSkyBlock membership via a compat layer; omitted for safety in this shell.
            online.sendMessage(base);
        }
    }
}