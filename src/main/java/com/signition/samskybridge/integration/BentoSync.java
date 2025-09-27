package com.signition.samskybridge.integration;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Level;

public class BentoSync {
    private final org.bukkit.plugin.Plugin plugin;
    private final boolean present;

    public BentoSync(org.bukkit.plugin.Plugin plugin){
        this.plugin = plugin;
        boolean ok;
        try {
            Class.forName("world.bentobox.bentobox.BentoBox");
            ok = true;
        } catch (Throwable t) {
            ok = false;
        }
        this.present = ok;
    }

    public boolean isPresent(){ return present; }

    public boolean isOwner(Player p, Location loc){
        if (!present) return true;
        try {
            UUID owner = getOwnerAt(loc);
            return owner != null && owner.equals(p.getUniqueId());
        } catch (Throwable t){
            return true;
        }
    }

    public boolean isMember(Player p, Location loc){
        if (!present) return true;
        try {
            Class<?> bb = Class.forName("world.bentobox.bentobox.BentoBox");
            Object inst = bb.getMethod("getInstance").invoke(null);
            Object islands = bb.getMethod("getIslands").invoke(inst);
            Object island = islands.getClass().getMethod("getIslandAt", Location.class).invoke(islands, loc);
            if (island == null) return false;
            try {
                Object set = island.getClass().getMethod("getMemberSet").invoke(island);
                Object contains = set.getClass().getMethod("contains", Object.class).invoke(set, p.getUniqueId());
                return (Boolean) contains;
            } catch (NoSuchMethodException nsme){
                Object set = island.getClass().getMethod("getMembers").invoke(island);
                Object contains = set.getClass().getMethod("contains", Object.class).invoke(set, p.getUniqueId());
                return (Boolean) contains;
            }
        } catch (Throwable t){
            return true;
        }
    }

    public boolean isInOwnIsland(Player p, Location loc){
        if (!present) return true;
        try {
            UUID owner = getOwnerAt(loc);
            if (owner == null) return false;
            if (owner.equals(p.getUniqueId())) return true;
            return isMember(p, loc);
        } catch (Throwable t){
            plugin.getLogger().log(Level.FINE, "isInOwnIsland reflection failed", t);
            return true;
        }
    }

    public Integer getIslandLevel(Player p){
        if (!present) return null;
        try {
            Class<?> bb = Class.forName("world.bentobox.bentobox.BentoBox");
            Object inst = bb.getMethod("getInstance").invoke(null);
            Object lvls = bb.getMethod("getLevelsManager").invoke(inst);
            Object island = bb.getMethod("getIslands").invoke(inst)
                    .getClass().getMethod("getIsland", java.util.UUID.class).invoke(bb.getMethod("getPlayers").invoke(inst)
                            .getClass().getMethod("getIslandUUID", java.util.UUID.class).invoke(bb.getMethod("getPlayers").invoke(inst), p.getUniqueId()));
            if (lvls == null || island == null) return null;
            Object level = lvls.getClass().getMethod("getIslandLevel", island.getClass()).invoke(lvls, island);
            if (level instanceof Number) return ((Number) level).intValue();
        } catch (Throwable ignored){}
        return null;
    }

    public UUID getOwnerAt(Location loc){
        if (!present) return null;
        try {
            Class<?> bb = Class.forName("world.bentobox.bentobox.BentoBox");
            Object inst = bb.getMethod("getInstance").invoke(null);
            Object islands = bb.getMethod("getIslands").invoke(inst);
            Object island = islands.getClass().getMethod("getIslandAt", Location.class).invoke(islands, loc);
            if (island == null) return null;
            Object owner = island.getClass().getMethod("getOwner").invoke(island);
            if (owner instanceof UUID) return (UUID) owner;
        } catch (Throwable t){
            plugin.getLogger().log(Level.FINE, "getOwnerAt reflection failed", t);
        }
        return null;
    }

    public Location getIslandCenter(Location loc){
        if (!present) return null;
        try {
            Class<?> bb = Class.forName("world.bentobox.bentobox.BentoBox");
            Object inst = bb.getMethod("getInstance").invoke(null);
            Object islands = bb.getMethod("getIslands").invoke(inst);
            Object island = islands.getClass().getMethod("getIslandAt", Location.class).invoke(islands, loc);
            if (island == null) return null;
            Object center = island.getClass().getMethod("getCenter").invoke(island);
            return (Location) center;
        } catch (Throwable t){
            plugin.getLogger().log(Level.FINE, "getIslandCenter reflection failed", t);
        }
        return null;
    }

    // --- Application hooks ---

    public void applyRangeInstant(OfflinePlayer owner, int radius){
        if (!present) return;
        try {
            String fmt = plugin.getConfig().getString("bento.range.command", "bsbadmin range set {owner} {radius}");
            String cmd = fmt.replace("{owner}", owner.getName() == null ? owner.getUniqueId().toString() : owner.getName())
                    .replace("{radius}", String.valueOf(radius));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        } catch (Throwable t){
            plugin.getLogger().log(Level.WARNING, "applyRangeInstant failed", t);
        }
    }

    public void applyTeamMax(Player ownerOnline, int max){
        if (!present) return;
        try {
            String gm = plugin.getConfig().getString("bento.gamemode-id", "bskyblock");
            String format = plugin.getConfig().getString("bento.teammax.permission-format", "{gm}.team.maxsize.{max}")
                    .replace("{gm}", gm).replace("{max}", String.valueOf(max));
            String perm = format;
            // simple grant via LuckPerms/perm plugins: fallback to Bukkit built-in
            ownerOnline.addAttachment(plugin, perm, true);
        } catch (Throwable t){
            plugin.getLogger().log(Level.WARNING, "applyTeamMax failed", t);
        }
    }

    public void reapplyOnJoin(Player p, int max){
        try {
            applyTeamMax(p, max);
        } catch (Throwable ignored){}
    }
}
