
package kr.minkang.samskybridge;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatService implements Listener {

    private final Main plugin;
    private final Storage storage;
    private final Set<UUID> enabled = new HashSet<>();

    public ChatService(Main plugin, Storage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public void toggle(Player p) {
        if (enabled.contains(p.getUniqueId())) {
            enabled.remove(p.getUniqueId());
            p.sendMessage(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.chat-off"));
        } else {
            enabled.add(p.getUniqueId());
            p.sendMessage(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.chat-on"));
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!enabled.contains(p.getUniqueId())) return;
        IslandData d = storage.getIslandByPlayer(p.getUniqueId());
        if (d == null) {
            p.sendMessage(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.no-island"));
            return;
        }
        e.setCancelled(true);
        String msg = ChatColor.translateAlternateColorCodes('&', "&a[섬] &r" + p.getName() + ": " + e.getMessage());
        // send to owner + members online
        send(d.owner, msg);
        for (UUID u : d.members) send(u, msg);
        // also log to console
        Bukkit.getConsoleSender().sendMessage("[섬채팅] " + p.getName() + ": " + e.getMessage());
    }

    private void send(UUID uuid, String msg) {
        Player t = Bukkit.getPlayer(uuid);
        if (t != null && t.isOnline()) {
            t.sendMessage(msg);
        }
    }
}
