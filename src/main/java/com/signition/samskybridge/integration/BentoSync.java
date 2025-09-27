package com.signition.samskybridge.integration;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * BentoBox 연동(리플렉션). 런타임에 BentoBox가 없으면 안전 폴백.
 */
public class BentoSync {
    private final Plugin plugin;
    private final boolean present;

    public BentoSync(Plugin plugin){
        this.plugin = plugin;
        this.present = (Bukkit.getPluginManager().getPlugin("BentoBox") != null);
    }

    /* ==================== 기본 조회 ==================== */

    public UUID getOwnerAt(Location loc){
        if (!present || loc == null) return null;
        try {
            Class<?> bb = Class.forName("world.bentobox.bentobox.BentoBox");
            Object inst = bb.getMethod("getInstance").invoke(null);
            Object islands = bb.getMethod("getIslands").invoke(inst);
            Object island = islands.getClass().getMethod("getIslandAt", Location.class).invoke(islands, loc);
            if (island == null) return null;
            // owner UUID
            try {
                Object owner = island.getClass().getMethod("getOwner").invoke(island);
                if (owner instanceof UUID) return (UUID) owner;
                if (owner != null) return UUID.fromString(String.valueOf(owner));
            } catch (NoSuchMethodException e){
                // older API
                Object owner = island.getClass().getMethod("getOwnerUUID").invoke(island);
                if (owner instanceof UUID) return (UUID) owner;
                if (owner != null) return UUID.fromString(String.valueOf(owner));
            }
        } catch (Throwable ignored){}
        return null;
    }

    public boolean isOwner(Player p, Location loc){
        if (p == null || loc == null) return false;
        UUID owner = getOwnerAt(loc);
        return owner != null && owner.equals(p.getUniqueId());
    }

    public boolean isMember(Player p, Location loc){
        if (!present || p == null || loc == null) return true; // 폴백: 제한하지 않음
        try {
            Class<?> bb = Class.forName("world.bentobox.bentobox.BentoBox");
            Object inst = bb.getMethod("getInstance").invoke(null);
            Object islands = bb.getMethod("getIslands").invoke(inst);
            Object island = islands.getClass().getMethod("getIslandAt", Location.class).invoke(islands, loc);
            if (island == null) return false;
            try {
                Set<?> set = (Set<?>) island.getClass().getMethod("getMemberSet").invoke(island);
                for (Object o : set){
                    if (p.getUniqueId().equals(o)) return true;
                    if (o != null && p.getUniqueId().toString().equals(String.valueOf(o))) return true;
                }
            } catch (NoSuchMethodException e){
                Set<?> set = (Set<?>) island.getClass().getMethod("getMembers").invoke(island);
                for (Object o : set){
                    if (p.getUniqueId().equals(o)) return true;
                    if (o != null && p.getUniqueId().toString().equals(String.valueOf(o))) return true;
                }
            }
        } catch (Throwable ignored){}
        return false;
    }

    public boolean isInOwnIsland(Player p, Location loc){
        return isOwner(p, loc);
    }

    public boolean isMemberOrOwnerAt(Player p, Location loc){
        try { return isOwner(p, loc) || isMember(p, loc); } catch (Throwable t){ return true; }
    }

    /* ==================== 탭/랭킹 표기 보조 ==================== */

    public int getIslandLevel(Player p){
        // BentoBox 레벨 애드온에 의존하지 않고, 정보가 없으면 0
        return 0;
    }

    public int getIslandRank(Player p){
        // 랭킹 서비스와 직접 연결하지 않고 숨김 처리(-1)
        return -1;
    }

    public int getTeamCount(Player p){
        // 멤버 수 추정: owner + members
        if (!present || p == null) return 1;
        try {
            Class<?> bb = Class.forName("world.bentobox.bentobox.BentoBox");
            Object inst = bb.getMethod("getInstance").invoke(null);
            Object islands = bb.getMethod("getIslands").invoke(inst);
            Object island = islands.getClass().getMethod("getIslandAt", Location.class).invoke(islands, p.getLocation());
            if (island == null) return 1;
            try {
                Set<?> set = (Set<?>) island.getClass().getMethod("getMemberSet").invoke(island);
                return set.size() + 1;
            } catch (NoSuchMethodException e){
                Set<?> set = (Set<?>) island.getClass().getMethod("getMembers").invoke(island);
                return set.size() + 1;
            }
        } catch (Throwable t){ return 1; }
    }

    public int getTeamMax(Player p){
        // 퍼미션에서 *.maxsize.<N> 최댓값 파싱 + 업그레이드 컨피그 폴백
        int best = 1;
        try {
            for (org.bukkit.permissions.PermissionAttachmentInfo info : p.getEffectivePermissions()){
                String perm = info.getPermission();
                if (perm == null) continue;
                int idx = perm.lastIndexOf('.');
                if (idx > 0){
                    String last = perm.substring(idx+1);
                    try { best = Math.max(best, Integer.parseInt(last)); } catch (NumberFormatException ignored){}
                }
            }
            org.bukkit.configuration.ConfigurationSection sec = plugin.getConfig().getConfigurationSection("upgrades.team.levels");
            if (sec != null){
                for (String k : sec.getKeys(false)){
                    best = Math.max(best, plugin.getConfig().getInt("upgrades.team.levels."+k+".team", best));
                }
            }
        } catch (Throwable ignored){}
        return best;
    }

    /* ==================== 즉시 적용 유틸 ==================== */

    public void applyRangeInstant(Player owner, int radius){
        try {
            String cmd = plugin.getConfig().getString("bento.range.command", "").replace("{player}", owner.getName()).replace("{radius}", String.valueOf(radius));
            if (cmd != null && !cmd.isEmpty()){
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        } catch (Throwable t){
            plugin.getLogger().log(Level.WARNING, "applyRangeInstant failed", t);
        }
    }

    public void applyTeamMax(Player owner, int max){
        try {
            String fmt = plugin.getConfig().getString("bento.teammax.permission-format", "team.maxsize.{max}")
                    .replace("{player}", owner.getName()).replace("{max}", String.valueOf(max));
            // 간단 폴백: Bukkit Attachment 부여
            owner.addAttachment(plugin, fmt, true);
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
