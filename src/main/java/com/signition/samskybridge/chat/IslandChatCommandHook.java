package com.signition.samskybridge.chat;

import com.signition.samskybridge.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import com.signition.samskybridge.util.Text;

public class IslandChatCommandHook implements Listener {

    private final Main plugin;
    private final IslandChatService service;
    private final Set<String> islandAliases = new HashSet<String>(Arrays.asList("섬","is","island","skyblock","sb"));

    public IslandChatCommandHook(Main plugin, IslandChatService service){
        this.plugin = plugin;
        this.service = service;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPreprocess(PlayerCommandPreprocessEvent e){
        String msg = e.getMessage(); // starts with "/"
        if (msg == null || msg.length() < 2) return;
        // strip leading slash and split by whitespace
        String rest = msg.substring(1);
        String[] parts = rest.split("\\s+");
        if (parts.length == 0) return;
        String root = parts[0].toLowerCase();
        if (!islandAliases.contains(root)) return;

        // expecting: /섬 채팅  or  /is chat
        if (parts.length >= 2){
            String sub = parts[1].toLowerCase();
            if ("채팅".equals(sub) || "chat".equals(sub)){
                e.setCancelled(true);
                Player p = e.getPlayer();
                boolean now = service.toggle(p.getUniqueId());
                p.sendMessage(Text.color((now ? "&a[섬채팅] 활성화" : "&c[섬채팅] 비활성화")));
            }
        }
    }
}