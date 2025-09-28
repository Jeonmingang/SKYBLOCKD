
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
    private final Set<UUID> islandChatOn = new HashSet<>();

    public ChatService(Main plugin, Storage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public boolean toggle(UUID uuid) {
        if (islandChatOn.contains(uuid)) {
            islandChatOn.remove(uuid);
            return false;
        } else {
            islandChatOn.add(uuid);
            return true;
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!islandChatOn.contains(p.getUniqueId())) return;

        IslandData d = storage.getIslandByPlayer(p.getUniqueId());
        if (d == null) d = BentoBridge.resolveFromBento(plugin, p);
        if (d == null) {
            islandChatOn.remove(p.getUniqueId());
            return;
        }

        e.setCancelled(true);
        String prefix = plugin.getConfig().getString("messages.chat-prefix", "&a[섬] ");
        String msg = ChatColor.translateAlternateColorCodes('&', prefix) + p.getName() + ChatColor.GRAY + ": " + e.getMessage();

        send(d.owner, msg);
        for (UUID u : d.members) send(u, msg);
        Bukkit.getConsoleSender().sendMessage("[섬채팅] " + p.getName() + ": " + e.getMessage());
    }

    private void send(UUID uuid, String msg) {
        if (uuid == null) return;
        Player t = Bukkit.getPlayer(uuid);
        if (t != null && t.isOnline()) {
            t.sendMessage(msg);
        }
    }
}
