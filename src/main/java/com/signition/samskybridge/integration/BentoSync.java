package com.signition.samskybridge.integration;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.logging.Level;

public class BentoSync {
    private final Plugin plugin;
    private final boolean present;
    public BentoSync(Plugin plugin){
        this.plugin = plugin;
        this.present = Bukkit.getPluginManager().getPlugin("BentoBox") != null ||
                       Bukkit.getPluginManager().getPlugin("BSkyBlock") != null;
        plugin.getLogger().info("[Bento] present=" + present);
    }
    public boolean isPresent(){ return present; }

    public boolean isOwner(Player p, Location loc){
        if (!present) return true;
        try {
            Class<?> bb = Class.forName("world.bentobox.bentobox.BentoBox");
            Object inst = bb.getMethod("getInstance").invoke(null);
            Object islands = bb.getMethod("getIslands").invoke(inst);
            Object island = islands.getClass().getMethod("getIslandAt", Location.class).invoke(islands, loc);
            if (island == null) return false;
            Object owner = island.getClass().getMethod("getOwner").invoke(island);
            if (owner instanceof UUID) return owner.equals(p.getUniqueId());
        } catch (Throwable t){
            plugin.getLogger().log(Level.FINE, "isOwner reflection failed", t);
        }
        return true;
    }

    public boolean isMemberOrOwnerAt(Player p, Location loc){
        if (!present) return true;
        try {
            Class<?> bb = Class.forName("world.bentobox.bentobox.BentoBox");
            Object inst = bb.getMethod("getInstance").invoke(null);
            Object islands = bb.getMethod("getIslands").invoke(inst);
            Object island = islands.getClass().getMethod("getIslandAt", Location.class).invoke(islands, loc);
            if (island == null) return false;
            Object owner = island.getClass().getMethod("getOwner").invoke(island);
            if (owner instanceof java.util.UUID && owner.equals(p.getUniqueId())) return true;
            try {
                Object set = island.getClass().getMethod("getMemberSet").invoke(island);
                return (boolean) set.getClass().getMethod("contains", Object.class).invoke(set, p.getUniqueId());
            } catch (NoSuchMethodException nsme) {
                Object set = island.getClass().getMethod("getMembers").invoke(island);
                return (boolean) set.getClass().getMethod("contains", Object.class).invoke(set, p.getUniqueId());
            }
        } catch (Throwable t){
            plugin.getLogger().log(Level.FINE, "isMemberOrOwnerAt reflection failed", t);
        }
        return true;
    }

    public int getIslandLevel(Player p){
        if (!present) return 0;
        try {
            Class<?> bb = Class.forName("world.bentobox.bentobox.BentoBox");
            Object inst = bb.getMethod("getInstance").invoke(null);
            Object players = bb.getMethod("getPlayers").invoke(inst);
            try {
                Object res = players.getClass().getMethod("getIslandLevel", java.util.UUID.class).invoke(players, p.getUniqueId());
                if (res instanceof Number) return ((Number) res).intValue();
            } catch (NoSuchMethodException ignore){}
        } catch (Throwable t){
            plugin.getLogger().log(Level.FINE, "getIslandLevel reflection failed", t);
        }
        return 0;
    }

    public int getIslandRank(Player p){ return 0; }
    public int getTeamCount(Player p){ return 1; }
    public int getTeamMax(Player p){ return 8; }
}
