
package kr.minkang.samskybridge;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class BentoBridge {

    private BentoBridge() {}

    public static boolean isAvailable() {
        return Bukkit.getPluginManager().getPlugin("BentoBox") != null;
    }

    /**
     * Try resolve player's island from BentoBox via reflection.
     * Returns an IslandData built from BentoBox (owner/members/range).
     * Returns null if no island or BentoBox not present.
     */
    @SuppressWarnings("unchecked")
    public static IslandData resolveFromBento(Main plugin, Player p) {
        try {
            if (!isAvailable()) return null;
            // world.bentobox.bentobox.BentoBox.getInstance().getIslandsManager().getIsland(world, uuid)
            Class<?> bbClazz = Class.forName("world.bentobox.bentobox.BentoBox");
            Method getInstance = bbClazz.getMethod("getInstance");
            Object bb = getInstance.invoke(null);

            Method getIslandsManager;
            try {
                getIslandsManager = bbClazz.getMethod("getIslandsManager");
            } catch (NoSuchMethodException e) {
                // older API
                getIslandsManager = bbClazz.getMethod("getIslands");
            }
            Object islandsManager = getIslandsManager.invoke(bb);

            World w = p.getWorld();
            UUID uuid = p.getUniqueId();
            Method getIsland = islandsManager.getClass().getMethod("getIsland", World.class, UUID.class);
            Object island = getIsland.invoke(islandsManager, w, uuid);
            if (island == null) return null;

            // island.getOwner(): UUID
            Method getOwner = island.getClass().getMethod("getOwner");
            UUID owner = (UUID) getOwner.invoke(island);
            if (owner == null) owner = uuid;

            // island.getMembers(): Map<UUID, Integer>
            Method getMembers = island.getClass().getMethod("getMembers");
            Map<UUID, ?> members = (Map<UUID, ?>) getMembers.invoke(island);

            // island.getProtectionRange(): int (radius)
            int radius = 50;
            try {
                Method getProtectionRange = island.getClass().getMethod("getProtectionRange");
                radius = (int) getProtectionRange.invoke(island);
            } catch (NoSuchMethodException ignored) {}

            IslandData d = new IslandData(owner);
            d.sizeRadius = radius;
            // default teamMax = max(current team size, configured base)
            int baseMembers = plugin.getConfig().getInt("upgrade.team.base-members", 2);
            int currentTeam = 1;
            if (members != null) {
                for (UUID m : members.keySet()) {
                    if (m != null && !m.equals(owner)) {
                        d.members.add(m);
                    }
                }
                currentTeam = d.members.size() + 1;
            }
            d.teamMax = Math.max(baseMembers, currentTeam);
            // level/xp are our plugin's scope; keep defaults for now
            // Persist to storage for future fast access
            plugin.storage.write(d);
            plugin.storage.save();
            return d;
        } catch (Throwable t) {
            // Any reflection failure -> treat as not available
            return null;
        }
    }

    /**
     * Apply new protection range to BentoBox island of this player.
     */
    public static boolean applyProtectionRange(Main plugin, org.bukkit.entity.Player p, int newRange) {
        try {
            if (!isAvailable()) return false;
            Class<?> bbClazz = Class.forName("world.bentobox.bentobox.BentoBox");
            Object bb = bbClazz.getMethod("getInstance").invoke(null);
            java.lang.reflect.Method getIslandsManager;
            try { getIslandsManager = bbClazz.getMethod("getIslandsManager"); }
            catch (NoSuchMethodException e) { getIslandsManager = bbClazz.getMethod("getIslands"); }
            Object islandsManager = getIslandsManager.invoke(bb);
            java.lang.reflect.Method getIsland = islandsManager.getClass().getMethod("getIsland", org.bukkit.World.class, java.util.UUID.class);
            Object island = getIsland.invoke(islandsManager, p.getWorld(), p.getUniqueId());
            if (island == null) return false;
            island.getClass().getMethod("setProtectionRange", int.class).invoke(island, newRange);
            islandsManager.getClass().getMethod("save", Class.forName("world.bentobox.bentobox.database.objects.Island")).invoke(islandsManager, island);
            return true;
        } catch (Throwable t) { return false; }
    }
    /**
     * Apply new team limit (per MEMBER_RANK) to BentoBox island of this player.
     */
    public static boolean applyTeamMax(Main plugin, org.bukkit.entity.Player p, int teamMax) {
        try {
            if (!isAvailable()) return false;
            Class<?> bbClazz = Class.forName("world.bentobox.bentobox.BentoBox");
            Object bb = bbClazz.getMethod("getInstance").invoke(null);
            java.lang.reflect.Method getIslandsManager;
            try { getIslandsManager = bbClazz.getMethod("getIslandsManager"); }
            catch (NoSuchMethodException e) { getIslandsManager = bbClazz.getMethod("getIslands"); }
            Object islandsManager = getIslandsManager.invoke(bb);
            java.lang.reflect.Method getIsland = islandsManager.getClass().getMethod("getIsland", org.bukkit.World.class, java.util.UUID.class);
            Object island = getIsland.invoke(islandsManager, p.getWorld(), p.getUniqueId());
            if (island == null) return false;
            Class<?> ranksClazz = Class.forName("world.bentobox.bentobox.managers.RanksManager");
            int MEMBER_RANK = ranksClazz.getField("MEMBER_RANK").getInt(null);
            island.getClass().getMethod("setMaxMembers", int.class, java.lang.Integer.class).invoke(island, MEMBER_RANK, java.lang.Integer.valueOf(teamMax));
            islandsManager.getClass().getMethod("save", Class.forName("world.bentobox.bentobox.database.objects.Island")).invoke(islandsManager, island);
            return true;
        } catch (Throwable t) { return false; }
    }
}
