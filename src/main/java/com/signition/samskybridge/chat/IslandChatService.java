package com.signition.samskybridge.chat;

import com.signition.samskybridge.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class IslandChatService implements Listener {
    private final Main plugin;
    private final Set<UUID> toggled = ConcurrentHashMap.newKeySet();

    // config
    private volatile boolean enabled;
    private volatile boolean membersOnly;
    private volatile List<String> commandAliases;
    private volatile String format;
    private volatile boolean spyEnabled;
    private volatile boolean spyConsole;
    private volatile String spyPermission;

    public IslandChatService(Main plugin){
        this.plugin = plugin;
        reload();
    }

    public void reload(){
        enabled = plugin.getConfig().getBoolean("island-chat.enabled", true);
        membersOnly = plugin.getConfig().getBoolean("island-chat.members-only", true);
        commandAliases = new ArrayList<>(plugin.getConfig().getStringList("island-chat.command-aliases"));
        if (commandAliases.isEmpty()){
            commandAliases = Arrays.asList("섬","is");
        }
        format = plugin.getConfig().getString("island-chat.format", "&a[섬채팅] &f{player}&7: &f{message}");
        spyEnabled = plugin.getConfig().getBoolean("island-chat.spy.enabled", true);
        spyConsole = plugin.getConfig().getBoolean("island-chat.spy.console", true);
        spyPermission = plugin.getConfig().getString("island-chat.spy.permission", "samskybridge.chat.spy");
    }

    public boolean toggle(UUID id){
        if (!enabled) return false;
        if (toggled.contains(id)){ toggled.remove(id); return false; }
        toggled.add(id); return true;
    }

    public boolean isAlias(String root){
        for (String a : commandAliases){
            if (a.equalsIgnoreCase(root)) return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncChat(AsyncPlayerChatEvent e){
        if (!enabled) return;
        Player sender = e.getPlayer();
        if (!toggled.contains(sender.getUniqueId())) return;

        e.setCancelled(true);

        // Build message
        String msg = ChatColor.translateAlternateColorCodes('&',
                format.replace("{player}", sender.getName()).replace("{message}", e.getMessage()));

        Collection<? extends Player> audience = Bukkit.getOnlinePlayers();
        if (membersOnly){
            Set<UUID> members = resolveIslandMembers(sender.getWorld(), sender.getUniqueId());
            if (members != null && !members.isEmpty()){
                // filter audience to member UUIDs
                List<Player> filtered = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()){
                    if (members.contains(p.getUniqueId())){
                        filtered.add(p);
                    }
                }
                audience = filtered;
            } else {
                // if we cannot resolve, fallback to sender only
                audience = Collections.singletonList(sender);
            }
        }
        for (Player p : audience){
            p.sendMessage(msg);
        }
        if (spyEnabled){
            if (spyConsole){ org.bukkit.Bukkit.getConsoleSender().sendMessage(msg); }
            for (Player pl : org.bukkit.Bukkit.getOnlinePlayers()){
                try { if (pl.hasPermission(spyPermission)) pl.sendMessage(msg); } catch(Throwable ignore){}
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Set<UUID> resolveIslandMembers(World world, UUID playerId){
        try{
            // BentoBox.getInstance().getIslands().getMembers(World, UUID)
            Class<?> bbClazz = Class.forName("world.bentobox.bentobox.BentoBox");
            Method getInstance = bbClazz.getMethod("getInstance");
            Object bb = getInstance.invoke(null);
            if (bb == null) return null;
            Method getIslands = null;
            try { getIslands = bbClazz.getMethod("getIslands"); }
            catch (NoSuchMethodException ignored){
                getIslands = bbClazz.getMethod("getIslandsManager");
            }
            Object islandsMgr = getIslands.invoke(bb);
            if (islandsMgr == null) return null;
            Method getMembers = islandsMgr.getClass().getMethod("getMembers", World.class, UUID.class);
            Object result = getMembers.invoke(islandsMgr, world, playerId);
            if (result instanceof Set){
                return (Set<UUID>) result;
            }
        }catch(Throwable ignore){}
        return null;
    }
}