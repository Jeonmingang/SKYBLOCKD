
package com.signition.samskybridge.chat;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class IslandChatService implements Listener {
    private final Plugin plugin;
    private final Set<UUID> toggled = ConcurrentHashMap.newKeySet();

    public IslandChatService(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean isOn(Player p) {
        return toggled.contains(p.getUniqueId());
    }

    public boolean toggle(Player p) {
        if (isOn(p)) {
            toggled.remove(p.getUniqueId());
            p.sendMessage(color("&7[섬채팅] 비활성화 되었습니다."));
            return false;
        } else {
            toggled.add(p.getUniqueId());
            p.sendMessage(color("&7[섬채팅] 활성화 되었습니다."));
            return true;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!plugin.getConfig().getBoolean("island-chat.enabled", true)) return;
        final Player p = e.getPlayer();
        if (!isOn(p)) return;

        // build recipients = island mates only (roles filtered)
        Set<UUID> ids = com.signition.samskybridge.compat.BentoCompat.getIslandMemberUUIDs(p);
        List<Player> targets = new ArrayList<Player>();
        List<String> allowed = plugin.getConfig().getStringList("island-chat.allowed-roles");
        if (allowed == null || allowed.isEmpty()) {
            allowed = Arrays.asList("OWNER","SUB_OWNER","MEMBER");
        }

        for (UUID id : ids) {
            Player op = Bukkit.getPlayer(id);
            if (op == null) continue;
            com.signition.samskybridge.compat.BentoCompat.Role role = com.signition.samskybridge.compat.BentoCompat.getRole(op);
            if (allowed.contains(role.name())) {
                targets.add(op);
            }
        }
        if (targets.isEmpty()) {
            // no recipients - fallback to self only
            targets.add(p);
        }

        // format message
        String fmt = getFormatFor(com.signition.samskybridge.compat.BentoCompat.getRole(p));
        final String out = fmt.replace("{name}", p.getName()).replace("{message}", e.getMessage());

        // cancel public chat
        e.setCancelled(true);
        // send to island recipients
        for (Player r : targets) {
            r.sendMessage(color(out));
        }
    }

    private String getFormatFor(com.signition.samskybridge.compat.BentoCompat.Role role) {
        FileConfiguration c = plugin.getConfig();
        String path = "island-chat.formats." + role.name();
        String def;
        switch (role) {
            case OWNER: def = "&6[ 섬장 ] &f{name}&7: &f{message}"; break;
            case SUB_OWNER: def = "&d[ 부섬장 ] &f{name}&7: &f{message}"; break;
            case MEMBER: def = "&a[ 섬원 ] &f{name}&7: &f{message}"; break;
            default: def = "&7[ 방문자 ] &f{name}&7: &f{message}"; break;
        }
        return c.getString(path, def);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        toggled.remove(e.getPlayer().getUniqueId());
    }

    private String color(String s) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
    }
}
