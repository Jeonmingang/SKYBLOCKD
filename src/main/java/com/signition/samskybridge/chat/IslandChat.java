
package com.signition.samskybridge.chat;

import com.signition.samskybridge.Main;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.database.objects.Island;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class IslandChat implements Listener {
    private final Main plugin;
    private final Set<UUID> toggled = ConcurrentHashMap.newKeySet();
    private final String leaderPrefix;
    private final String memberPrefix;
    private final String fmt;
    private final boolean spyEnabled;
    private final String spyPerm;
    private final String spyPrefix;

    public IslandChat(Main plugin){
        this.plugin = plugin;
        FileConfiguration c = plugin.getConfig();
        leaderPrefix = color(c.getString("island-chat.leader-prefix","&6[섬장]"));
        memberPrefix = color(c.getString("island-chat.member-prefix","&a[섬]"));
        fmt = c.getString("island-chat.format", "{prefix}&f {name}: &7{message}");
        spyEnabled = c.getBoolean("island-chat.spy.enabled", true);
        spyPerm = c.getString("island-chat.spy.permission", "samsky.chat.spy");
        spyPrefix = color(c.getString("island-chat.spy.prefix", "&8[Spy]&7 "));
    }

    public boolean toggle(Player p){
        UUID id = p.getUniqueId();
        if (toggled.contains(id)){ toggled.remove(id); return false; }
        toggled.add(id); return true;
    }
    public boolean isToggled(Player p){ return toggled.contains(p.getUniqueId()); }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        Player p = e.getPlayer();
        if (!isToggled(p)) return;
        IslandsManager im = BentoBox.getInstance().getIslands();
        Island is = im.getIsland(p.getWorld(), p.getUniqueId());
        if (is == null){ toggled.remove(p.getUniqueId()); p.sendMessage("§c섬이 없어 섬 채팅이 꺼졌습니다."); return; }

        e.setCancelled(true);
        String prefix = is.getOwner()!=null && is.getOwner().equals(p.getUniqueId()) ? leaderPrefix : memberPrefix;
        String msg = fmt.replace("{prefix}", prefix).replace("{name}", p.getName()).replace("{message}", e.getMessage());

        // send to island members + owner online
        Set<UUID> targets = new HashSet<>();
        targets.add(is.getOwner());
        targets.addAll(is.getMemberSet());
        for (UUID t : targets){
            if (t == null) continue;
            Player op = Bukkit.getPlayer(t);
            if (op != null && op.isOnline()){
                op.sendMessage(color(msg));
            }
        }
        // spy
        if (spyEnabled){
            for (Player op : Bukkit.getOnlinePlayers()){
                if (op.hasPermission(spyPerm) && (targets.isEmpty() || !targets.contains(op.getUniqueId()))){
                    op.sendMessage(spyPrefix + color(msg));
                }
            }
        }
    }

    private static String color(String s){ return s.replace("&","§"); }
}
