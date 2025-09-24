package com.signition.samskybridge.tab;

import com.signition.samskybridge.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class TablistService implements Runnable {
    private final Main plugin;
    private BukkitTask task;
    private volatile boolean enabled;
    private volatile boolean onlyIslandWorlds;
    private volatile boolean sameIslandHighlight;
    private volatile String ownerPrefix;
    private volatile String memberPrefix;
    private volatile String visitorPrefix;
    private volatile String sameOwnerPrefix;
    private volatile String sameMemberPrefix;
    private volatile long refreshTicks;
    private volatile String ownerTpl;
    private volatile String memberTpl;
    private volatile String visitorTpl;

    public TablistService(Main plugin){ this.plugin = plugin; reload(); }

    public void start(){
        stop();
        if (!enabled) return;
        long ticks = refreshTicks <= 0 ? 200L : refreshTicks;
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, this, 20L, ticks);
    }
    public void stop(){
        if (task != null){ try { task.cancel(); } catch(Throwable ignore){} task = null; }
    }
    public void reload(){
        enabled = plugin.getConfig().getBoolean("tablist.enabled", false);
        onlyIslandWorlds = plugin.getConfig().getBoolean("tablist.only-in-island-worlds", true);
        sameIslandHighlight = plugin.getConfig().getBoolean("tablist.same-island-highlight", true);
        ownerPrefix = plugin.getConfig().getString("tablist.owner-prefix", "&6[섬장]&f ");
        memberPrefix = plugin.getConfig().getString("tablist.member-prefix", "&a[섬원]&f ");
        visitorPrefix = plugin.getConfig().getString("tablist.visitor-prefix", "&7[방문자]&f ");
        sameOwnerPrefix = plugin.getConfig().getString("tablist.same-island-owner-prefix", "&e[우리섬장]&f ");
        sameMemberPrefix = plugin.getConfig().getString("tablist.same-island-member-prefix", "&b[우리섬원]&f ");
        refreshTicks = plugin.getConfig().getLong("tablist.refresh_ticks", 200L);
        // role-aware tab prefix templates (optional)
        ownerTpl = plugin.getConfig().getString("ranking.tab-prefix.owner", plugin.getConfig().getString("ranking.tab-prefix", "&7[ &a섬 랭킹 &f<rank>위 &7| &blv.<level> &7] &r"));
        memberTpl = plugin.getConfig().getString("ranking.tab-prefix.member", "&7[ &b우리섬 &7] &r");
        visitorTpl = plugin.getConfig().getString("ranking.tab-prefix.visitor", visitorPrefix);
        if (enabled && task == null){ start(); }
        if (!enabled && task != null){ stop(); }
    }

    private String buildPrefix(Player p, Role role){
        String tpl = role==Role.OWNER? ownerTpl : role==Role.MEMBER? memberTpl : visitorTpl;
        try {
            // Resolve island at location via BentoBox
            Class<?> bbClazz = Class.forName("world.bentobox.bentobox.BentoBox");
            Object bb = bbClazz.getMethod("getInstance").invoke(null);
            java.lang.reflect.Method getIslands;
            try { getIslands = bbClazz.getMethod("getIslands"); }
            catch (NoSuchMethodException e){ getIslands = bbClazz.getMethod("getIslandsManager"); }
            Object mgr = getIslands.invoke(bb);
            java.lang.reflect.Method getIslandAt = mgr.getClass().getMethod("getIslandAt", org.bukkit.Location.class);
            Object opt = getIslandAt.invoke(mgr, p.getLocation());
            if (opt != null){
                boolean present = (boolean) opt.getClass().getMethod("isPresent").invoke(opt);
                if (present){
                    Object island = opt.getClass().getMethod("get").invoke(opt);
                    java.util.UUID owner = (java.util.UUID) island.getClass().getMethod("getOwner").invoke(island);
                    // Fetch stats from DataStore / Features / Ranking
                    com.signition.samskybridge.data.IslandData is = plugin.getDataStore().getOrCreate(owner, "섬");
                    int level = is.getLevel();
                    int rank = 0;
                    try {
                        java.util.List<com.signition.samskybridge.data.IslandData> list = plugin.getRankingService().getSortedIslands();
                        for (int i=0;i<list.size();i++){ if (list.get(i).getId().equals(owner)){ rank = i+1; break; } }
                    } catch(Throwable ignore){}
                    String islandName = is.getName()==null? "-" : is.getName();
                    String ownerName = p.getServer().getOfflinePlayer(owner).getName();
                    tpl = tpl.replace("<level>", String.valueOf(level))
                             .replace("<rank>", String.valueOf(rank))
                             .replace("<owner>", ownerName==null?"-":ownerName)
                             .replace("<island>", islandName);
                }
            }
        } catch(Throwable ignore){}
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', tpl);
    }

@Override
    public void run() {
        try{
            for (Player p : Bukkit.getOnlinePlayers()){
                if (onlyIslandWorlds){
                    if (!isIslandWorld(p.getWorld())){
                        // Reset to plain name when out of island worlds
                        p.setPlayerListName(p.getName());
                        continue;
                    }
                }
                String name = p.getName();
                Role r = roleAtLocation(p);
                String prefix = buildPrefix(p, r);
                p.setPlayerListName(prefix + name);
            }
        }catch(Throwable ignore){}
    }

    private boolean isIslandWorld(World w){
        try{
            java.util.List<String> worlds = plugin.getConfig().getStringList("xp.allowed-worlds");
            if (worlds != null && !worlds.isEmpty()){
                return worlds.contains(w.getName());
            }
        }catch(Throwable ignore){}
        return true;
    }

    enum Role { OWNER, MEMBER, VISITOR }

    private Role roleAtLocation(Player p){
        try{
            Class<?> bbClazz = Class.forName("world.bentobox.bentobox.BentoBox");
            Object bb = bbClazz.getMethod("getInstance").invoke(null);
            if (bb == null) return Role.VISITOR;
            Method getIslands;
            try { getIslands = bbClazz.getMethod("getIslands"); }
            catch (NoSuchMethodException e){ getIslands = bbClazz.getMethod("getIslandsManager"); }
            Object mgr = getIslands.invoke(bb);
            if (mgr == null) return Role.VISITOR;
            Method getIslandAt = mgr.getClass().getMethod("getIslandAt", org.bukkit.Location.class);
            Object opt = getIslandAt.invoke(mgr, p.getLocation());
            if (opt == null) return Role.VISITOR;
            // Optional handling
            boolean present = (boolean) opt.getClass().getMethod("isPresent").invoke(opt);
            if (!present) return Role.VISITOR;
            Object island = opt.getClass().getMethod("get").invoke(opt);
            // owner?
            java.util.UUID owner = (java.util.UUID) island.getClass().getMethod("getOwner").invoke(island);
            if (owner != null && owner.equals(p.getUniqueId())) return Role.OWNER;
            // member?
            java.util.Set<java.util.UUID> set = (java.util.Set<java.util.UUID>) island.getClass().getMethod("getMemberSet").invoke(island);
            if (set != null && set.contains(p.getUniqueId())) return Role.MEMBER;
            return Role.VISITOR;
        }catch(Throwable ignore){
            return Role.VISITOR;
        }
    }
}