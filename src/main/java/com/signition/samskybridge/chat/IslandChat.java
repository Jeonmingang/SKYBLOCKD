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
    private boolean consoleLog = true;

    private final Main plugin;
    private final DataStore store;
    private final Map<UUID, Boolean> toggled = new ConcurrentHashMap<>();
    private String leaderPrefix = "&6[섬장]";
    private String memberPrefix = "&a[섬]";
    private String format = "{prefix}&f {name}: &7{message}";
    private boolean spyEnabled = true;
    private String spyPerm = "samsky.chat.spy";
    private String spyPrefix = "&8[Spy]&7 ";

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
    public 
void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        // Only reroute if toggled ON
        if (!toggled.getOrDefault(p.getUniqueId(), false)) return;

        // Cancel vanilla chat
        e.setCancelled(true);
        String msg = ChatColor.translateAlternateColorCodes('&', e.getMessage());

        // Try BentoBox first via reflection (no direct API dependency)
        try {
            Class<?> bbClass = Class.forName("world.bentobox.bentobox.BentoBox");
            Object bbInstance = bbClass.getMethod("getInstance").invoke(null);
            Object islandsManager = bbClass.getMethod("getIslands").invoke(bbInstance);

            java.lang.reflect.Method getIslandMethod =
                    islandsManager.getClass().getMethod("getIsland", org.bukkit.World.class, java.util.UUID.class);
            Object islandObj = getIslandMethod.invoke(islandsManager, p.getWorld(), p.getUniqueId());

            if (islandObj != null) {
                Class<?> islandClass = islandObj.getClass();
                java.util.UUID owner = (java.util.UUID) islandClass.getMethod("getOwner").invoke(islandObj);
                @SuppressWarnings("unchecked")
                java.util.Set<java.util.UUID> members = new java.util.HashSet<>((java.util.Set<java.util.UUID>)
                        islandClass.getMethod("getMemberSet").invoke(islandObj));

                // include owner too
                if (owner != null) {
                    members.add(owner);
                }

                // build prefix by role
                boolean isLeader = owner != null && owner.equals(p.getUniqueId());
                String pref = isLeader ? leaderPrefix : memberPrefix;
                String out = format.replace("{prefix}", ChatColor.translateAlternateColorCodes('&', pref))
                                   .replace("{name}", p.getName())
                                   .replace("{message}", msg);
                out = ChatColor.translateAlternateColorCodes('&', out);

                // send to team
                for (java.util.UUID id : members) {
                    org.bukkit.entity.Player rec = org.bukkit.Bukkit.getPlayer(id);
                    if (rec != null) {
                        final org.bukkit.entity.Player target = rec;
                        final String msgOut = out;
                        org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> target.sendMessage(msgOut));
                    }
                }

                // console log (optional)
                if (consoleLog) {
                    final String consoleLine = ChatColor.stripColor(out);
                    org.bukkit.Bukkit.getScheduler().runTask(plugin,
                            () -> org.bukkit.Bukkit.getConsoleSender().sendMessage(consoleLine));
                }

                // spies
                if (spyEnabled) {
                    final String spyMsg = ChatColor.translateAlternateColorCodes('&', spyPrefix) + ChatColor.stripColor(out);
                    for (org.bukkit.entity.Player online : org.bukkit.Bukkit.getOnlinePlayers()) {
                        if (online.hasPermission(spyPerm) && !members.contains(online.getUniqueId())) {
                            online.sendMessage(spyMsg);
                        }
                    }
                }
                return;
            }
        } catch (Throwable ignore) {
            // ignore and fall back to DataStore
        }

        // Fallback to DataStore
        IslandData is = resolveIsland(p.getUniqueId());
        if (is == null) {
            p.sendMessage("§c섬이 없습니다.");
            return;
        }

        boolean isLeader = is.getOwner() != null && is.getOwner().equals(p.getUniqueId());
        String pref = isLeader ? leaderPrefix : memberPrefix;
        String out = format.replace("{prefix}", ChatColor.translateAlternateColorCodes('&', pref))
                           .replace("{name}", p.getName())
                           .replace("{message}", msg);
        out = ChatColor.translateAlternateColorCodes('&', out);

        // owner
        if (is.getOwner() != null){
            org.bukkit.entity.Player owner = org.bukkit.Bukkit.getPlayer(is.getOwner());
            if (owner != null) owner.sendMessage(out);
        }
        // members
        if (is.getMembers() != null){
            for (java.util.UUID id : is.getMembers()){
                if (id.equals(p.getUniqueId())) continue;
                org.bukkit.entity.Player mem = org.bukkit.Bukkit.getPlayer(id);
                if (mem != null) mem.sendMessage(out);
            }
        }
        // spies
        if (spyEnabled){
            String spy = ChatColor.translateAlternateColorCodes('&', spyPrefix) + ChatColor.stripColor(out);
            for (org.bukkit.entity.Player online : org.bukkit.Bukkit.getOnlinePlayers()){
                if (online.hasPermission(spyPerm) || online.isOp()){
                    online.sendMessage(spy);
                }
            }
        }
    }


    public void reload() {
        String base = "island-chat.";
        this.leaderPrefix = plugin.getConfig().getString(base + "leader-prefix", this.leaderPrefix != null ? this.leaderPrefix : "&6[섬장]");
        this.memberPrefix = plugin.getConfig().getString(base + "member-prefix", this.memberPrefix != null ? this.memberPrefix : "&a[섬]");
        this.format = plugin.getConfig().getString(base + "format", this.format != null ? this.format : "{prefix}&f {name}: &7{message}");
        this.spyEnabled = plugin.getConfig().getBoolean(base + "spy.enabled", this.spyEnabled);
        this.spyPerm = plugin.getConfig().getString(base + "spy.permission", this.spyPerm != null ? this.spyPerm : "samsky.chat.spy");
        this.spyPrefix = plugin.getConfig().getString(base + "spy.prefix", this.spyPrefix != null ? this.spyPrefix : "&8[Spy]&7 ");
        this.consoleLog = plugin.getConfig().getBoolean(base + "console-log", this.consoleLog);
    }


    private com.signition.samskybridge.data.IslandData resolveIsland(java.util.UUID uuid){
        try {
            return plugin.getDataStore().getIsland(uuid);
        } catch (Throwable t){
            return null;
        }
    }

}