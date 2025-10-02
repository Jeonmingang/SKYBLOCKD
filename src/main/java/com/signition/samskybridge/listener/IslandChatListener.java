
package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.service.ChatService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;

public class IslandChatListener implements Listener {
    private final ChatService chat;

    public IslandChatListener(Main plugin) {
        this.chat = plugin.getChatService();
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!chat.isIslandChat(p)) return;
        e.setCancelled(true);
        String msg = "§a[섬] §f" + p.getName() + "§7: §f" + e.getMessage();
        chat.sendToIsland(p, msg);
    }
}
