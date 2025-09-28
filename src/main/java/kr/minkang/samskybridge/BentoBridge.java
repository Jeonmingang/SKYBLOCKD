
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

    public static IslandData resolveFromBento(Main plugin, Player p) {
        try {
            if (!isAvailable()) return null;
            Class<?> bbClazz = Class.forName("world.bentobox.bentobox.BentoBox");
            Object bb = bbClazz.getMethod("getInstance").invoke(null);

            Method getIslandsManager;
            try { getIslandsManager = bbClazz.getMethod("getIslandsManager"); }
            catch (NoSuchMethodException e) { getIslandsManager = bbClazz.getMethod("getIslands"); }
            Object islandsManager = getIslandsManager.invoke(bb);

            Method getIsland = islandsManager.getClass().getMethod("getIsland", World.class, java.util.UUID.class);
            Object island = getIsland.invoke(islandsManager, p.getWorld(), p.getUniqueId());
            if (island == null) return null;

            java.util.UUID owner = (java.util.UUID) island.getClass().getMethod("getOwner").invoke(island);

            int teamMax = 0;
            try {
                Class<?> ranksClazz = Class.forName("world.bentobox.bentobox.managers.RanksManager");
                int MEMBER_RANK = ranksClazz.getField("MEMBER_RANK").getInt(null);
                Object limit = island.getClass().getMethod("getMaxMembers", int.class).invoke(island, MEMBER_RANK);
                if (limit instanceof Integer) teamMax = (Integer) limit;
            } catch (Throwable ignored) {}

            int size = 0;
            try {
                Object val = island.getClass().getMethod("getProtectionRange").invoke(island);
                if (val instanceof Integer) size = (Integer) val;
            } catch (Throwable ignored) {}

            IslandData d = plugin.storage.getIslandByPlayer(owner);
            if (d == null) {
                d = new IslandData();
                d.owner = owner;
                d.teamMax = teamMax > 0 ? teamMax : plugin.getConfig().getInt("upgrade.team.base-members", 2);
                d.sizeRadius = size > 0 ? size : plugin.getConfig().getInt("upgrade.size.base-radius", 50);
                d.level = 0; d.xp = 0;
                plugin.storage.put(d);
                plugin.storage.save();
            } else {
                if (teamMax > 0) d.teamMax = teamMax;
                if (size > 0) d.sizeRadius = size;
            }
            return d;
        } catch (Throwable t) {
            return null;
        }
    }

    public static int getProtectionRange(Player p) {
        try {
            if (!isAvailable()) return -1;
            Class<?> bbClazz = Class.forName("world.bentobox.bentobox.BentoBox");
            Object bb = bbClazz.getMethod("getInstance").invoke(null);

            Method getIslandsManager;
            try { getIslandsManager = bbClazz.getMethod("getIslandsManager"); }
            catch (NoSuchMethodException e) { getIslandsManager = bbClazz.getMethod("getIslands"); }
            Object islandsManager = getIslandsManager.invoke(bb);

            Method getIsland = islandsManager.getClass().getMethod("getIsland", World.class, java.util.UUID.class);
            Object island = getIsland.invoke(islandsManager, p.getWorld(), p.getUniqueId());
            if (island == null) return -1;
            Object val = island.getClass().getMethod("getProtectionRange").invoke(island);
            return (val instanceof Integer) ? (Integer) val : -1;
        } catch (Throwable t) { return -1; }
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

            Method getIsland = islandsManager.getClass().getMethod("getIsland", World.class, java.util.UUID.class);
            Object island = getIsland.invoke(islandsManager, p.getWorld(), p.getUniqueId());
            if (island == null) return -1;
            Class<?> ranksClazz = Class.forName("world.bentobox.bentobox.managers.RanksManager");
            int MEMBER_RANK = ranksClazz.getField("MEMBER_RANK").getInt(null);
            Object limit = island.getClass().getMethod("getMaxMembers", int.class).invoke(island, MEMBER_RANK);
            return (limit instanceof Integer) ? (Integer) limit : -1;
        } catch (Throwable t) { return -1; }
    }
}
