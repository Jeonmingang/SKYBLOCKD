
package com.signition.samskybridge.compat;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.lang.reflect.Method;
import java.util.*;

public final class BentoCompat {
    private BentoCompat() {}

    private static Object getBento() {
        try {
            org.bukkit.plugin.Plugin p = Bukkit.getPluginManager().getPlugin("BentoBox");
            if (p == null) return null;
            if (!p.isEnabled()) return null;
            // world.bentobox.bentobox.BentoBox
            return p;
        } catch (Throwable t) { return null; }
    }

    public static boolean isAvailable() { return getBento() != null; }

    public static boolean isIslandWorld(World w) {
        // Fallback: assume yes if Bento present and world name contains "bskyblock"
        try {
            if (!isAvailable()) return true;
            String n = w.getName().toLowerCase(Locale.ROOT);
            return n.contains("bskyblock") || n.contains("island") || n.contains("skyblock");
        } catch (Throwable t) { return true; }
    }

    public enum Role { OWNER, SUB_OWNER, MEMBER, VISITOR }

    public static Role getRole(Player p) {
        try {
            if (!isAvailable()) return Role.VISITOR;
            // Try: IslandsManager#getIsland(World, UUID) and check rank via getRank(User) etc. (best-effort)
            // We avoid hard deps: simple heuristic using permissions or team ownership stored by other plugin parts.
            if (p.hasPermission("bskyblock.island.owner")) return Role.OWNER;
            if (p.hasPermission("bskyblock.island.subowner")) return Role.SUB_OWNER;
            if (p.hasPermission("bskyblock.island.member")) return Role.MEMBER;
            return Role.MEMBER; // optimistic default for Bento environments
        } catch (Throwable t) {
            return Role.VISITOR;
        }
    }

    public static boolean isOwner(Player p) { return getRole(p) == Role.OWNER; }
    public static boolean isMember(Player p) {
        Role r = getRole(p);
        return r == Role.OWNER || r == Role.SUB_OWNER || r == Role.MEMBER;
    }

    public static Set<UUID> getIslandMemberUUIDs(Player p) {
        // Best-effort: if Bento not available, return only self to avoid NPE
        Set<UUID> out = new HashSet<UUID>();
        try {
            if (!isAvailable()) { out.add(p.getUniqueId()); return out; }
            // Without API types at compile-time, we cannot traverse islands safely.
            // Return online players that appear to be on same island by world & perms heuristic.
            for (Player op : Bukkit.getOnlinePlayers()) {
                if (op.getWorld().equals(p.getWorld())) {
                    if (isMember(op)) out.add(op.getUniqueId());
                }
            }
            if (out.isEmpty()) out.add(p.getUniqueId());
        } catch (Throwable t) {
            out.add(p.getUniqueId());
        }
        return out;
    }
}
