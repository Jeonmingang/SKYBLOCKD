package com.signition.samskybridge.chat;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class IslandChat implements Listener {
    private final Main plugin;
    private final DataStore store;

    private final Set<UUID> chatToggled = new HashSet<>();

    public IslandChat(Main plugin, DataStore store) {
        this.plugin = plugin;
        this.store = store;
    }
    // Backward-compat: allow constructor with only plugin, try to discover DataStore via Main getter if exists
    public IslandChat(Main plugin) {
        this.plugin = plugin;
        this.store = tryGetStore(plugin);
    }

    public void toggle(Player p) {
        UUID u = p.getUniqueId();
        if (chatToggled.contains(u)) chatToggled.remove(u); else chatToggled.add(u);
    }

    public void reload() {
        // nothing to cache now; kept for API compatibility
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!chatToggled.contains(p.getUniqueId())) return; // only when toggled

        e.setCancelled(true);

        IslandData is = store != null ? store.getIsland(p.getUniqueId()) : null;
        String msg = e.getMessage();

        String format = plugin.getConfig().getString("chat.format", "<prefix><name>: <message>");
        boolean logConsole = plugin.getConfig().getBoolean("chat.log-to-console", true);

        boolean isLeader = is != null && Objects.equals(ownerOf(is), p.getUniqueId());
        String prefix = Text.color(isLeader
                ? plugin.getConfig().getString("chat.leader-prefix", "&c[섬장] ")
                : plugin.getConfig().getString("chat.member-prefix", "&7[섬원] "));

        String rendered = Text.color(format
                .replace("<prefix>", prefix)
                .replace("<name>", p.getName())
                .replace("<message>", msg));

        // send to island members (including leader)
        if (is != null) {
            for (UUID m : membersOf(is)) {
                Player t = Bukkit.getPlayer(m);
                if (t != null && t.isOnline()) t.sendMessage(rendered);
            }
            // also ensure leader gets it
            UUID owner = ownerOf(is);
            if (owner != null) {
                Player t = Bukkit.getPlayer(owner);
                if (t != null && t.isOnline()) t.sendMessage(rendered);
            }
        } else {
            // no island -> only sender feedback
            p.sendMessage(rendered);
        }

        // spy
        if (plugin.getConfig().getBoolean("chat.spy.enabled", true)) {
            String spyPerm = plugin.getConfig().getString("chat.spy.permission", "samskybridge.chat.spy");
            String spyPrefix = Text.color(plugin.getConfig().getString("chat.spy.prefix", "&7[섬채팅-스파이] "));
            for (Player tp : Bukkit.getOnlinePlayers()) {
                if (tp.equals(p)) continue;
                if (tp.hasPermission(spyPerm)) tp.sendMessage(spyPrefix + rendered);
            }
        }

        if (logConsole) Bukkit.getLogger().info(Text.stripColor(rendered));
    }

    /* ===== helpers ===== */
    private DataStore tryGetStore(Main plugin) {
        try {
            Method m = Main.class.getMethod("getDataStore");
            Object r = m.invoke(plugin);
            if (r instanceof DataStore) return (DataStore) r;
        } catch (Throwable ignored) {}
        return null;
    }

    private UUID ownerOf(IslandData is) {
        try {
            try { return (UUID) IslandData.class.getMethod("getOwner").invoke(is); }
            catch (NoSuchMethodException ignore) {}
            java.lang.reflect.Field f = IslandData.class.getDeclaredField("owner");
            f.setAccessible(true);
            Object v = f.get(is);
            if (v instanceof java.util.UUID) return (java.util.UUID) v;
        } catch (Throwable ignored) {}
        return null;
    }

    @SuppressWarnings("unchecked")
    private Set<UUID> membersOf(IslandData is) {
        try {
            try { return (Set<UUID>) IslandData.class.getMethod("getMembers").invoke(is); }
            catch (NoSuchMethodException ignore) {}
            Field f = IslandData.class.getDeclaredField("members");
            f.setAccessible(true);
            Object v = f.get(is);
            if (v instanceof Set) return (Set<UUID>) v;
            if (v instanceof Collection) return new HashSet<>((Collection<UUID>) v);
        } catch (Throwable ignored) {}
        return Collections.emptySet();
    }
}
