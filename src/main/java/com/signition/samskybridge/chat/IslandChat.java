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
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 섬 채팅: 컨피그 기반 포맷/프리픽스/스파이/콘솔로그 지원 (1.16.5/Java11)
 */
public class IslandChat implements Listener {
    private final Main plugin;
    private final DataStore store;

    private String format;
    private String leaderPrefix;
    private String memberPrefix;
    private boolean spyEnabled;
    private String spyPerm;
    private String spyPrefix;
    private boolean logToConsole;

    public IslandChat(Main plugin, DataStore store) {
        this.plugin = plugin;
        this.store = store;
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration c = plugin.getConfig();
        format       = c.getString("chat.format", "<prefix><name>: <message>");
        leaderPrefix = Text.color(c.getString("chat.leader-prefix", "&c[섬장] "));
        memberPrefix = Text.color(c.getString("chat.member-prefix", "&7[섬원] "));
        spyEnabled   = c.getBoolean("chat.spy.enabled", true);
        spyPerm      = c.getString("chat.spy.permission", "samskybridge.chat.spy");
        spyPrefix    = Text.color(c.getString("chat.spy.prefix", "&7[섬채팅-스파이] "));
        logToConsole = c.getBoolean("chat.log-to-console", true);
    }

    public void reload() { loadConfig(); }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        Player sender = e.getPlayer();
        // 기존 채널 판별 로직이 있다면 그대로 사용한다고 가정
        try {
            java.lang.reflect.Method m = plugin.getChatChannel().getClass().getMethod("isIslandChat", Player.class);
            Object r = m.invoke(plugin.getChatChannel(), sender);
            if (r instanceof Boolean && !((Boolean) r)) return;
        } catch (Throwable t) { /* 없으면 전체 채팅으로 사용 안함 */ return; }

        e.setCancelled(true);

        IslandData is = store.getOrCreate(sender.getUniqueId(), sender.getName());
        boolean isLeader = is.getOwner() != null && is.getOwner().equals(sender.getUniqueId());

        String prefix = isLeader ? leaderPrefix : memberPrefix;
        String line = Text.color(
                format.replace("<prefix>", prefix)
                      .replace("<name>", sender.getName())
                      .replace("<message>", e.getMessage())
        );

        for (Player target : sameIslandOnline(is)) target.sendMessage(line);

        if (spyEnabled) {
            String spyLine = spyPrefix + "§7(" + safeName(is.getName()) + ") §f" + sender.getName() + "§7: §f" + e.getMessage();
            for (Player op : Bukkit.getOnlinePlayers()) {
                if (!sameIsland(is, op) && op.hasPermission(spyPerm)) op.sendMessage(spyLine);
            }
        }

        if (logToConsole) {
            Bukkit.getConsoleSender().sendMessage("[IslandChat][" + safeName(is.getName()) + "] " + sender.getName() + ": " + e.getMessage());
        }
    }

    private Collection<Player> sameIslandOnline(IslandData is) {
        Set<UUID> ids = new HashSet<>();
        if (is.getOwner() != null) ids.add(is.getOwner());
        ids.addAll(membersOf(is));
        List<Player> list = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) if (ids.contains(p.getUniqueId())) list.add(p);
        return list;
    }

    private boolean sameIsland(IslandData is, Player p) {
        if (is.getOwner() != null && is.getOwner().equals(p.getUniqueId())) return true;
        return membersOf(is).contains(p.getUniqueId());
    }

    @SuppressWarnings("unchecked")
    private Set<UUID> membersOf(IslandData is) {
        try {
            try { return (Set<UUID>) IslandData.class.getMethod("getMembers").invoke(is); }
            catch (NoSuchMethodException ignore) {}
            java.lang.reflect.Field f = IslandData.class.getDeclaredField("members");
            f.setAccessible(true);
            Object v = f.get(is);
            if (v instanceof Set) return (Set<UUID>) v;
            if (v instanceof Collection) return new HashSet<>((Collection<UUID>) v);
        } catch (Throwable ignored) {}
        return Collections.emptySet();
    }
    private String safeName(String s) { return s == null ? "이름없음" : s; }
}
