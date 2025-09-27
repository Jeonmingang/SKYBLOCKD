
package com.signition.samskybridge.integration;

import com.signition.samskybridge.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Safe BentoBox/BSkyBlock integration without compile-time API dependency.
 * - Range sync: uses admin command "/bsbadmin range set <owner> <radius>"
 * - Team size sync: uses permission "[gamemode].team.maxsize.X" on owner
 *
 * Notes:
 * - Range via permission requires relog, so we use the admin command to apply instantly.
 * - Team size via permission is applied with a runtime attachment and re-applied on join.
 */
public class BentoSync {
    private final org.bukkit.plugin.Plugin plugin;
    private final com.signition.samskybridge.data.DataStore store;
    public BentoSync(org.bukkit.plugin.Plugin plugin){
        this.plugin = plugin;
        com.signition.samskybridge.data.DataStore s = null;
        try{
            if (plugin instanceof com.signition.samskybridge.Main){
                java.lang.reflect.Method m = plugin.getClass().getMethod("getDataStore");
                s = (com.signition.samskybridge.data.DataStore) m.invoke(plugin);
            }
        }catch(Throwable ignored){}
        this.store = s;
    }


    private final Main plugin;
    private final String gamemodeId; // e.g. "bskyblock"
    private final boolean enabled;
    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    public BentoSync(Main plugin){
        this.plugin = plugin;
        this.enabled = plugin.getConfig().getBoolean("integration.bentobox.enabled", true);
        this.gamemodeId = plugin.getConfig().getString("integration.bentobox.gamemode-id","bskyblock");
    }

    public boolean isEnabled(){
        if (!enabled) return false;
        return Bukkit.getPluginManager().getPlugin("BentoBox") != null;
    }

    /**
     * Apply island protected range instantly by dispatching admin command.
     * Owner name is used as target to comply with BSkyBlock's admin command.
     */
    public void applyRangeInstant(OfflinePlayer owner, int radius){
        if (!isEnabled()) return;
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        String cmd = "bsbadmin range set " + owner.getName() + " " + radius;
        Bukkit.dispatchCommand(console, cmd);
        plugin.getLogger().info("[BentoSync] Ran: " + cmd);
    }

    /**
     * Apply team max size to island owner via numbered permission.
     * [gamemode].team.maxsize.X (docs)
     */
    public void applyTeamMax(Player ownerOnline, int max){
        if (!isEnabled() || ownerOnline == null) return;
        // Remove prior attachment if any
        PermissionAttachment old = attachments.remove(ownerOnline.getUniqueId());
        if (old != null) {
            try { ownerOnline.removeAttachment(old); } catch (Throwable ignored) {}
        }
        PermissionAttachment att = ownerOnline.addAttachment(plugin);
        String node = gamemodeId + ".team.maxsize." + max;
        att.setPermission(node, true);
        attachments.put(ownerOnline.getUniqueId(), att);
        plugin.getLogger().info("[BentoSync] Granted runtime permission: " + node + " to " + ownerOnline.getName());
    }

    public void reapplyOnJoin(Player player, int teamMax){
        if (!isEnabled()) return;
        applyTeamMax(player, teamMax);
    }

    /**
     * Checks via reflection whether the given location is inside the caller's own island.
     * If BentoBox is not present or any reflection fails, this method returns true (do not block),
     * so that servers without Bento/BSkyBlock keep working.
     */
    public boolean isInOwnIsland(org.bukkit.entity.Player player, org.bukkit.Location loc){
        try{
            if (!isEnabled() || player == null || loc == null) return true;
            Object bento = getBentoPlugin();
            if (bento == null) return true;
            // BentoBox#getIslands()
            java.lang.reflect.Method getIslands = bento.getClass().getMethod("getIslands");
            Object islands = getIslands.invoke(bento);
            // IslandsManager#getIslandAt(Location)
            java.lang.reflect.Method getIslandAt = islands.getClass().getMethod("getIslandAt", org.bukkit.Location.class);
            Object optionalIsland = getIslandAt.invoke(islands, loc);
            // Optional#isPresent()
            java.lang.reflect.Method isPresent = optionalIsland.getClass().getMethod("isPresent");
            boolean present = (boolean) isPresent.invoke(optionalIsland);
            if (!present) return false;
            // Optional#get()
            Object island = optionalIsland.getClass().getMethod("get").invoke(optionalIsland);
            // Island#getOwner()
            java.util.UUID owner = (java.util.UUID) island.getClass().getMethod("getOwner").invoke(island);
            return owner != null && owner.equals(player.getUniqueId());
        }catch(Throwable ignore){
            return true;
        }
    }

    private Object getBentoPlugin(){
        try{
            org.bukkit.plugin.Plugin p = org.bukkit.Bukkit.getPluginManager().getPlugin("BentoBox");
            if (p != null && p.isEnabled()) return p;
            // Fall back to static BentoBox.getInstance() if available
            Class<?> clazz = Class.forName("world.bentobox.bentobox.BentoBox");
            try{
                Object inst = clazz.getMethod("getInstance").invoke(null);
                return inst;
            }catch(Throwable ignore){}
            return null;
        }catch(Throwable e){
            return null;
        }
    }


    public boolean isOwner(org.bukkit.entity.Player p, org.bukkit.Location loc){ return store != null && p != null && store.get(p.getUniqueId()) != null; }

    public boolean isMember(org.bukkit.entity.Player p, org.bukkit.Location loc){ return false; }

    public int getIslandLevel(org.bukkit.entity.Player p){ com.signition.samskybridge.data.IslandData d = (store==null||p==null)?null:store.get(p.getUniqueId()); return d==null?0:d.getLevel(); }

    public int getIslandRank(org.bukkit.entity.Player p){ if (store==null||p==null) return 0; java.util.List<com.signition.samskybridge.data.IslandData> list = new java.util.ArrayList<com.signition.samskybridge.data.IslandData>(store.all()); java.util.Collections.sort(list, new java.util.Comparator<com.signition.samskybridge.data.IslandData>(){ public int compare(com.signition.samskybridge.data.IslandData a, com.signition.samskybridge.data.IslandData b){ long xa=a.getXp(); long xb=b.getXp(); return xa==xb?0:(xa<xb?1:-1); }}); int r=1; for (com.signition.samskybridge.data.IslandData d:list){ if (p.getUniqueId().equals(d.getId())) return r; r++; } return 0; }
}
