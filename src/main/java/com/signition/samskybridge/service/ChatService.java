
package com.signition.samskybridge.service;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatService {
    private final Set<UUID> islandChat = new HashSet<>();

    public boolean toggle(Player p) {
        UUID id = p.getUniqueId();
        if (islandChat.contains(id)) {
            islandChat.remove(id);
            p.sendMessage("§6[섬] §f섬 채팅 §cOFF");
            return false;
        } else {
            islandChat.add(id);
            p.sendMessage("§6[섬] §f섬 채팅 §aON");
            return true;
        }
    }

    public boolean isIslandChat(Player p) {
        return islandChat.contains(p.getUniqueId());
    }

    // Fallback: just send to players who also toggled island chat (if no island API)
    public void sendToIsland(Player sender, String msg) {
        for (Player t : Bukkit.getOnlinePlayers()) {
            if (islandChat.contains(t.getUniqueId())) {
                t.sendMessage(msg);
            }
        }
    }
}
