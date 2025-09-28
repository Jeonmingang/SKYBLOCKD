
package kr.minkang.samskybridge;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * BentoBox/BSkyBlock reflection helpers.
 * This class does NOT require BentoBox at compile-time.
 */
public final class BentoBridge {

    private BentoBridge() {}

    public static boolean isAvailable() {
        try {
            Class.forName("world.bentobox.bentobox.BentoBox");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static IslandData resolveFromBento(Main plugin, Player p) {
        try {
            if (!isAvailable()) return null;
            Class<?> bbClazz = Class.forName("world.bentobox.bentobox.BentoBox");
            Object bb = bbClazz.getMethod("getInstance").invoke(null);

            Method getIslandsManager;
            try { getIslandsManager = bbClazz.getMethod("getIslandsManager"); }
            catch (NoSuchMethodException e) { getIslandsManager = bbClazz.getMethod("getIslands"); }
            Object islandsManager = getIslandsManager.invoke(bb);

            UUID owner = p.getUniqueId();

            Object island = findIslandByOwnerAnyGameMode(islandsManager, owner);
            if (island == null) return null;

            IslandData d = plugin.storage.getIslandByPlayer(owner);
            if (d == null) d = new IslandData(owner);
            // Try read protection size & team size if available through getters
            d.sizeRadius = safeInt(island, "getProtectionRange", d.sizeRadius);
            int members = safeInt(island, "getMaxMembers", -1);
            if (members >= 0) d.teamMax = members;
            plugin.storage.write(d);
            plugin.storage.save();
            // Ensure runtime owner permission for team size
            plugin.applyOwnerTeamPerm(owner, d.teamMax);
            return d;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Object findIslandByOwnerAnyGameMode(Object islandsManager, UUID owner) {
        try {
            // BentoBox 1.16+: getIsland(World, UUID) exists per world; scan worlds
            Method getIslandWorldUUID = null;
            try { getIslandWorldUUID = islandsManager.getClass().getMethod("getIsland", World.class, UUID.class); }
            catch (NoSuchMethodException ignored) {}

            if (getIslandWorldUUID != null) {
                for (World w : Bukkit.getWorlds()) {
                    try {
                        Object isl = getIslandWorldUUID.invoke(islandsManager, w, owner);
                        if (isl != null) return isl;
                    } catch (Throwable ignored) {}
                }
            }

            // Alternate: getIslandByOwner(UUID)
            try {
                Method m = islandsManager.getClass().getMethod("getIslandByOwner", UUID.class);
                Object isl = m.invoke(islandsManager, owner);
                if (isl != null) return isl;
            } catch (Throwable ignored) {}

            // Fallback: getIslands(owner) -> first
            try {
                Method m = islandsManager.getClass().getMethod("getIslands", UUID.class);
                Object list = m.invoke(islandsManager, owner);
                if (list instanceof java.util.List) {
                    java.util.List<?> l = (java.util.List<?>) list;
                    if (!l.isEmpty()) return l.get(0);
                }
            } catch (Throwable ignored) {}

        } catch (Throwable ignored) {}
        return null;
    }

    public static void applyProtectionRange(Main plugin, Player owner, int radius) {
        // Command path works with stock BSkyBlock: /bsbadmin range set <ownerName> <radius>
        try {
            String name = owner.getName();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "bsbadmin range set " + name + " " + radius);
        } catch (Throwable ignored) {}
    }

    public static void applyTeamMax(Main plugin, Player owner, int teamMax) {
        // Apply via permission attachment (and refresh at login handled by Main)
        try {
            plugin.applyOwnerTeamPerm(owner.getUniqueId(), teamMax);
        } catch (Throwable ignored) {}
    }

    public static int getProtectionRange(Player p) {
        // get actual applied radius if possible, else -1
        try {
            if (!isAvailable()) return -1;
            Class<?> bbClazz = Class.forName("world.bentobox.bentobox.BentoBox");
            Object bb = bbClazz.getMethod("getInstance").invoke(null);

            Method getIslandsManager;
            try { getIslandsManager = bbClazz.getMethod("getIslandsManager"); }
            catch (NoSuchMethodException e) { getIslandsManager = bbClazz.getMethod("getIslands"); }
            Object islandsManager = getIslandsManager.invoke(bb);

            Object island = findIslandByOwnerAnyGameMode(islandsManager, p.getUniqueId());
            if (island == null) return -1;
            return safeInt(island, "getProtectionRange", -1);
        } catch (Throwable ignored) {
            return -1;
        }
    }

    public static int getTeamMax(Player p) {
        try {
            if (!isAvailable()) return -1;
            Class<?> bbClazz = Class.forName("world.bentobox.bentobox.BentoBox");
            Object bb = bbClazz.getMethod("getInstance").invoke(null);

            Method getIslandsManager;
            try { getIslandsManager = bbClazz.getMethod("getIslandsManager"); }
            catch (NoSuchMethodException e) { getIslandsManager = bbClazz.getMethod("getIslands"); }
            Object islandsManager = getIslandsManager.invoke(bb);

            Object island = findIslandByOwnerAnyGameMode(islandsManager, p.getUniqueId());
            if (island == null) return -1;
            int members = safeInt(island, "getMaxMembers", -1);
            if (members >= 0) return members;
        } catch (Throwable ignored) {}
        // Fallback to permission check
        int max = -1;
        for (int i = 2; i <= 100; i++) {
            if (p.hasPermission("bskyblock.team.maxsize." + i)) max = i;
        }
        return max;
    }

    private static int safeInt(Object target, String methodName, int def) {
        try {
            Method m = target.getClass().getMethod(methodName);
            Object o = m.invoke(target);
            if (o instanceof Number) return ((Number) o).intValue();
        } catch (Throwable ignored) {}
        return def;
    }
}
