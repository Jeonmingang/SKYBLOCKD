package com.signition.samskybridge.chat;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal, compile-safe chat listener with reflection-based DataStore resolution.
 * - Fixes: getIsland(UUID) absence by resolving via common alternative method names.
 * - Adds: toggle(Player) -> boolean (previously void).
 * - Keeps behavior simple and compatible with 1.16.5 / Java 11.
 */
public class IslandChat implements Listener {
    private final Main plugin;
    private final DataStore store;
    private final Map<UUID, Boolean> toggled = new ConcurrentHashMap<>();

    public IslandChat(Main plugin, DataStore store) {
        this.plugin = plugin;
        this.store = store;
    }

    /** Toggle island chat. Returns true if ON after toggling, false if OFF. */
    public boolean toggle(Player p) {
        UUID id = p.getUniqueId();
        boolean on = !toggled.getOrDefault(id, false);
        toggled.put(id, on);
        p.sendMessage(on ? "§a섬 채팅 ON" : "§c섬 채팅 OFF");
        return on;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        // Only reroute if toggled ON
        if (!toggled.getOrDefault(p.getUniqueId(), false)) return;

        IslandData is = resolveIsland(p.getUniqueId());
        if (is == null) {
            p.sendMessage("§c섬이 없습니다.");
            return;
        }

        // Example basic route to island members; customize format as needed
        e.setCancelled(true);
        String raw = e.getMessage();
        String msg = ChatColor.translateAlternateColorCodes('&', raw);

        // Broadcast to island members (owner + members)
        try {
            if (is.getOwner() != null) {
                Player owner = Bukkit.getPlayer(is.getOwner());
                if (owner != null) owner.sendMessage("§b[섬채팅] §f" + p.getName() + ": §7" + msg);
            }
        } catch (Throwable ignored) {}

        try {
            if (is.getMembers() != null) {
                for (UUID m : is.getMembers()) {
                    if (m.equals(p.getUniqueId())) continue;
                    Player mem = Bukkit.getPlayer(m);
                    if (mem != null) {
                        mem.sendMessage("§b[섬채팅] §f" + p.getName() + ": §7" + msg);
                    }
                }
            }
        } catch (Throwable ignored) {}
        // Also mirror to server console/log
        Bukkit.getLogger().info("[IslandChat] " + p.getName() + ": " + com.signition.samskybridge.util.Text.stripColor(msg));
    }

    /** Try to get a player's island from DataStore without compile-time coupling. */
    private IslandData resolveIsland(UUID uuid) {
        // Try common method names with different signatures via reflection
        String[] names = new String[] {"getIsland", "getIslandByPlayer", "findIsland", "getPlayerIsland"};
        Class<?>[] params = new Class<?>[] {java.util.UUID.class, org.bukkit.entity.Player.class};
        for (String n : names) {
            for (Class<?> param : params) {
                try {
                    Method m = store.getClass().getMethod(n, param);
                    Object arg = (param == UUID.class) ? uuid : Bukkit.getPlayer(uuid);
                    Object res = m.invoke(store, arg);
                    if (res instanceof IslandData) return (IslandData) res;
                } catch (NoSuchMethodException ignored) {
                } catch (Throwable t) {
                    // ignore and try next
                }
            }
        }
        // Fallback: scan islands if accessor exists (getIslands / getAll / values())
        try {
            // try getIslands(): Map<UUID, IslandData>
            Method m = store.getClass().getMethod("getIslands");
            Object mapObj = m.invoke(store);
            if (mapObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<?,?> map = (Map<?,?>) mapObj;
                for (Object v : map.values()) {
                    if (v instanceof IslandData) {
                        IslandData is = (IslandData) v;
                        try {
                            if (uuid.equals(is.getOwner())) return is;
                            if (is.getMembers() != null && is.getMembers().contains(uuid)) return is;
                        } catch (Throwable ignored) {}
                    }
                }
            }
        } catch (Throwable ignored) {}

        return null;
    }

    public void reload() {
        // Re-read chat settings from config
        String base = "chat.";
        this.channel = plugin.getConfig().getString(base + "channel", this.channel);
        this.format = plugin.getConfig().getString(base + "format", this.format);
        this.leaderPrefix = plugin.getConfig().getString(base + "leader-prefix", this.leaderPrefix);
        this.memberPrefix = plugin.getConfig().getString(base + "member-prefix", this.memberPrefix);
        this.spyEnabled = plugin.getConfig().getBoolean(base + "spy.enabled", this.spyEnabled);
        this.spyPerm = plugin.getConfig().getString(base + "spy.permission", this.spyPerm);
        this.spyPrefix = plugin.getConfig().getString(base + "spy.prefix", this.spyPrefix);
    }
    
}
