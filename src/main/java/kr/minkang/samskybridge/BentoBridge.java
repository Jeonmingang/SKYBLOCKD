
package kr.minkang.samskybridge;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.UUID;

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

    /** Try to resolve & import island data from BentoBox to our Storage. */
    public static IslandData resolveFromBento(Main plugin, Player p) {
        try {
            if (!isAvailable()) return null;
            Class<?> bbClazz = Class.forName("world.bentobox.bentobox.BentoBox");
            Object bb = bbClazz.getMethod("getInstance").invoke(null);

            Method getIslandsManager;
            try { getIslandsManager = bbClazz.getMethod("getIslandsManager"); }
            catch (NoSuchMethodException e) { getIslandsManager = bbClazz.getMethod("getIslands"); }
            Object islandsManager = getIslandsManager.invoke(bb);

            Method getIsland = islandsManager.getClass().getMethod("getIsland", World.class, UUID.class);
            Object island = getIsland.invoke(islandsManager, p.getWorld(), p.getUniqueId());
            if (island == null) return null;

            // owner
            UUID owner = (UUID) island.getClass().getMethod("getOwner").invoke(island);

            // teamMax
            int teamMax = 0;
            try {
                Class<?> ranksClazz = Class.forName("world.bentobox.bentobox.managers.RanksManager");
                int MEMBER_RANK = ranksClazz.getField("MEMBER_RANK").getInt(null);
                Object limit = island.getClass().getMethod("getMaxMembers", int.class).invoke(island, MEMBER_RANK);
                if (limit instanceof Integer) teamMax = (Integer) limit;
            } catch (Throwable ignored) {}

            // size
            int size = 0;
            try {
                Object val = island.getClass().getMethod("getProtectionRange").invoke(island);
                if (val instanceof Integer) size = (Integer) val;
            } catch (Throwable ignored) {}

            IslandData d = plugin.storage.getIslandByPlayer(owner);
            if (d == null) {
                d = new IslandData(owner);
                d.teamMax = teamMax > 0 ? teamMax : plugin.getConfig().getInt("upgrade.team.base-members", 2);
                d.sizeRadius = size > 0 ? size : plugin.getConfig().getInt("upgrade.size.base-radius", 50);
                d.level = 1; d.xp = 0;
                plugin.storage.write(d);
            } else {
                if (teamMax > 0) d.teamMax = teamMax;
                if (size > 0) d.sizeRadius = size;
            }
            return d;
        } catch (Throwable t) {
            return null;
        }
    }

    /** Read protection range from BentoBox island, -1 if unknown. */
    public static int getProtectionRange(Player p) {
        try {
            if (!isAvailable()) return -1;
            Class<?> bbClazz = Class.forName("world.bentobox.bentobox.BentoBox");
            Object bb = bbClazz.getMethod("getInstance").invoke(null);

            Method getIslandsManager;
            try { getIslandsManager = bbClazz.getMethod("getIslandsManager"); }
            catch (NoSuchMethodException e) { getIslandsManager = bbClazz.getMethod("getIslands"); }
            Object islandsManager = getIslandsManager.invoke(bb);

            Method getIsland = islandsManager.getClass().getMethod("getIsland", World.class, UUID.class);
            Object island = getIsland.invoke(islandsManager, p.getWorld(), p.getUniqueId());
            if (island == null) return -1;
            Object val = island.getClass().getMethod("getProtectionRange").invoke(island);
            return (val instanceof Integer) ? (Integer) val : -1;
        } catch (Throwable t) {
            return -1;
        }
    }

    /** Read team max from BentoBox island, -1 if unknown. */
    public static int getTeamMax(Player p) {
        try {
            if (!isAvailable()) return -1;
            Class<?> bbClazz = Class.forName("world.bentobox.bentobox.BentoBox");
            Object bb = bbClazz.getMethod("getInstance").invoke(null);

            Method getIslandsManager;
            try { getIslandsManager = bbClazz.getMethod("getIslandsManager"); }
            catch (NoSuchMethodException e) { getIslandsManager = bbClazz.getMethod("getIslands"); }
            Object islandsManager = getIslandsManager.invoke(bb);

            Method getIsland = islandsManager.getClass().getMethod("getIsland", World.class, UUID.class);
            Object island = getIsland.invoke(islandsManager, p.getWorld(), p.getUniqueId());
            if (island == null) return -1;
            Class<?> ranksClazz = Class.forName("world.bentobox.bentobox.managers.RanksManager");
            int MEMBER_RANK = ranksClazz.getField("MEMBER_RANK").getInt(null);
            Object limit = island.getClass().getMethod("getMaxMembers", int.class).invoke(island, MEMBER_RANK);
            return (limit instanceof Integer) ? (Integer) limit : -1;
        } catch (Throwable t) {
            return -1;
        }
    }

    /** Apply protection range via command or API based on config. */
    public static void applyProtectionRange(Main plugin, Player p, int newRange) {
        try {
            String mode = plugin.getConfig().getString("integration.size.apply", "command");
            if ("command".equalsIgnoreCase(mode)) {
                String ownerName = p.getName();
                String cmd = plugin.getConfig().getString("integration.size.command", "bsbadmin range set <owner> <size>")
                        .replace("<owner>", ownerName).replace("<size>", String.valueOf(newRange));
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            } else {
                // API path (best-effort reflection)
                try {
                    Class<?> bbClazz = Class.forName("world.bentobox.bentobox.BentoBox");
                    Object bb = bbClazz.getMethod("getInstance").invoke(null);
                    Method getIslandsManager;
                    try { getIslandsManager = bbClazz.getMethod("getIslandsManager"); }
                    catch (NoSuchMethodException e) { getIslandsManager = bbClazz.getMethod("getIslands"); }
                    Object islandsManager = getIslandsManager.invoke(bb);
                    Method getIsland = islandsManager.getClass().getMethod("getIsland", World.class, UUID.class);
                    Object island = getIsland.invoke(islandsManager, p.getWorld(), p.getUniqueId());
                    if (island != null) {
                        Method setRange = island.getClass().getMethod("setProtectionRange", int.class);
                        setRange.invoke(island, newRange);
                    }
                } catch (Throwable ignore) {}
            }
        } catch (Throwable ignored) {}
    }

    /** Apply team max: via permission or API based on config. */
    public static void applyTeamMax(Main plugin, Player p, int newMax) {
        try {
            String mode = plugin.getConfig().getString("integration.team.apply", "permission");
            if ("permission".equalsIgnoreCase(mode)) {
                plugin.applyOwnerTeamPerm(p.getUniqueId(), newMax);
            } else {
                // API path (best-effort reflection)
                try {
                    Class<?> bbClazz = Class.forName("world.bentobox.bentobox.BentoBox");
                    Object bb = bbClazz.getMethod("getInstance").invoke(null);
                    Method getIslandsManager;
                    try { getIslandsManager = bbClazz.getMethod("getIslandsManager"); }
                    catch (NoSuchMethodException e) { getIslandsManager = bbClazz.getMethod("getIslands"); }
                    Object islandsManager = getIslandsManager.invoke(bb);
                    Method getIsland = islandsManager.getClass().getMethod("getIsland", World.class, UUID.class);
                    Object island = getIsland.invoke(islandsManager, p.getWorld(), p.getUniqueId());
                    if (island != null) {
                        Class<?> ranksClazz = Class.forName("world.bentobox.bentobox.managers.RanksManager");
                        int MEMBER_RANK = ranksClazz.getField("MEMBER_RANK").getInt(null);
                        Method setMaxMembers = island.getClass().getMethod("setMaxMembers", int.class, int.class);
                        setMaxMembers.invoke(island, MEMBER_RANK, newMax);
                    }
                } catch (Throwable ignore) {}
            }
        } catch (Throwable ignored) {}
    }
}
