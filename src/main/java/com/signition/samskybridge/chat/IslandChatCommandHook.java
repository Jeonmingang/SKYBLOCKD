package com.signition.samskybridge.chat;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class IslandChatCommandHook implements Listener {

    private final IslandChatService service;

    public IslandChatCommandHook(IslandChatService service){
        this.service = service;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPreprocess(PlayerCommandPreprocessEvent e){
        String[] parts = e.getMessage().split("\\s+");
        if (parts.length == 0) return;
        String root = parts[0].replaceFirst("^/","").toLowerCase();
        if (!service.isAlias(root)) return;

        if (parts.length >= 2){
            String sub = parts[1].toLowerCase();
            if ("채팅".equals(sub) || "chat".equals(sub)){
                e.setCancelled(true);
                Player p = e.getPlayer();
                boolean now = service.toggle(p.getUniqueId());
                p.sendMessage(Text.color(now ? "&a[섬채팅] 활성화" : "&c[섬채팅] 비활성화"));
            }
        }
    }
}