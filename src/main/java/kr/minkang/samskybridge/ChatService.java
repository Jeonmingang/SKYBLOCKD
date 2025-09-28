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
    private final Set<UUID> toggled = new HashSet<>();

    public ChatService(Main plugin) {
        this.plugin = plugin;
    }

    public boolean toggle(UUID uuid) {
        if (toggled.contains(uuid)) {
            toggled.remove(uuid);
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendMessage(color(prefix() + plugin.getConfig().getString("messages.chat-off", "&e섬 채팅이 꺼졌습니다.")));
            return false;
        } else {
            toggled.add(uuid);
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendMessage(color(prefix() + plugin.getConfig().getString("messages.chat-on", "&a섬 채팅이 켜졌습니다.")));
            return true;
        }
    }

    public boolean isOn(UUID uuid) { return toggled.contains(uuid); }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!toggled.contains(p.getUniqueId())) return;

        IslandData d = plugin.storage.getIslandByPlayer(p.getUniqueId());
        if (d == null) d = BentoBridge.resolveFromBento(plugin, p);
        if (d == null) return;

        String tag = plugin.getConfig().getString("chat.member-tag", "&b[ 섬원 ]");
        if (d.owner != null && d.owner.equals(p.getUniqueId())) {
            tag = plugin.getConfig().getString("chat.owner-tag", "&6[ 섬장 ]");
        }
        String fmt = plugin.getConfig().getString("chat.format", "{tag} &f{player} &7: {message}");
        String msg = fmt.replace("{tag}", tag).replace("{player}", p.getName()).replace("{message}", e.getMessage());
        msg = color(msg);

        e.setCancelled(true);
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

    private String prefix() { return plugin.getConfig().getString("messages.prefix", "&a[섬]&r "); }
    private String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
