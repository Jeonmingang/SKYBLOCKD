
package com.signition.samskybridge.chat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

public class IslandChatCommandHook implements Listener {
    private final Plugin plugin;
    private final IslandChatService service;
    private final List<String> baseAliases = Arrays.asList("/섬", "/is", "/island", "/skyblock", "/sb");

    public IslandChatCommandHook(Plugin plugin, IslandChatService service) {
        this.plugin = plugin;
        this.service = service;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCmd(PlayerCommandPreprocessEvent e) {
        if (!plugin.getConfig().getBoolean("island-chat.enabled", true)) return;
        String msg = e.getMessage().trim();
        String lower = msg.toLowerCase();
        // check matches: /섬 채팅 or /is chat
        for (String base : baseAliases) {
            if (lower.startsWith(base)) {
                String rest = lower.substring(base.length()).trim();
                if (rest.isEmpty()) return;
                String[] parts = rest.split("\s+");
                if (parts.length >= 1) {
                    String sub = parts[0];
                    if (isChatAlias(sub)) {
                        e.setCancelled(true);
                        Player p = e.getPlayer();
                        service.toggle(p);
                        return;
                    }
                }
            }
        }
    }

    private boolean isChatAlias(String s) {
        java.util.List<String> aliases = plugin.getConfig().getStringList("island-chat.command-aliases");
        if (aliases == null || aliases.isEmpty()) aliases = java.util.Arrays.asList("채팅","chat");
        for (String a : aliases) {
            if (s.equalsIgnoreCase(a)) return true;
        }
        return false;
    }
}
