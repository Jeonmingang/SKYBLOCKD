
package com.signition.samskybridge.integration;

import org.bukkit.World;
import org.bukkit.entity.Player;
import java.lang.reflect.Method;
import java.util.UUID;

public final class BentoHooks {
    private BentoHooks() { }

    public static boolean hasIslandAnywhere(Player p) {
        UUID u = p.getUniqueId();
        if (classExists("world.bentobox.bentobox.BentoBox")) {
            if (hasIsland_BentoBox(u)) return true;
        }
        if (classExists("com.wasteofplastic.bskyblock.api.BSkyBlockAPI")) {
            if (hasIsland_BSkyBlock(u)) return true;
        }
        return false;
    }

    public static String resolveRole(Player p) {
        UUID u = p.getUniqueId();
        if (classExists("world.bentobox.bentobox.BentoBox")) {
            String r = role_BentoBox(u);
            if (r != null) return r;
        }
        if (classExists("com.wasteofplastic.bskyblock.api.BSkyBlockAPI")) {
            String r = role_BSkyBlock(u);
            if (r != null) return r;
        }
        return "미등록";
    }

    private static boolean hasIsland_BentoBox(UUID u) {
        try {
            Class<?> BentoBox = Class.forName("world.bentobox.bentobox.BentoBox");
            Object api = BentoBox.getMethod("getInstance").invoke(null);
            Object iwm = api.getClass().getMethod("getIWM").invoke(api);
            Iterable<?> worlds = (Iterable<?>) iwm.getClass().getMethod("getIslandWorlds").invoke(iwm);
            Object islands = api.getClass().getMethod("getIslands").invoke(api);
            Method getIsland = null;
            for (Method m : islands.getClass().getMethods()) {
                if (m.getName().equals("getIsland") && m.getParameterCount()==2) { getIsland = m; break; }
            }
            if (getIsland == null) return false;
            for (Object wObj : worlds) {
                World w = (World) wObj;
                Object island = getIsland.invoke(islands, w, u);
                if (island != null) return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }

    private static String role_BentoBox(UUID u) {
        try {
            Class<?> BentoBox = Class.forName("world.bentobox.bentobox.BentoBox");
            Object api = BentoBox.getMethod("getInstance").invoke(null);
            Object iwm = api.getClass().getMethod("getIWM").invoke(api);
            Iterable<?> worlds = (Iterable<?>) iwm.getClass().getMethod("getIslandWorlds").invoke(iwm);
            Object islands = api.getClass().getMethod("getIslands").invoke(api);
            Method getIsland = null;
            for (Method m : islands.getClass().getMethods()) {
                if (m.getName().equals("getIsland") && m.getParameterCount()==2) { getIsland = m; break; }
            }
            if (getIsland == null) return null;
            for (Object wObj : worlds) {
                World w = (World) wObj;
                Object island = getIsland.invoke(islands, w, u);
                if (island != null) {
                    for (Method m : island.getClass().getMethods()) {
                        if (m.getName().equals("isOwner") && m.getParameterCount()==1 && m.getParameterTypes()[0]==UUID.class) {
                            Boolean owner = (Boolean)m.invoke(island, u);
                            return owner ? "섬장" : "섬원";
                        }
                        if (m.getName().equals("getOwner") && m.getParameterCount()==0) {
                            Object owner = m.invoke(island);
                            if (owner instanceof UUID) return ((UUID)owner).equals(u) ? "섬장" : "섬원";
                        }
                    }
                    return "섬원";
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static boolean hasIsland_BSkyBlock(UUID u) {
        try {
            Class<?> API = Class.forName("com.wasteofplastic.bskyblock.api.BSkyBlockAPI");
            Object api = API.getMethod("getInstance").invoke(null);
            for (Method m : API.getMethods()) {
                if ((m.getName().equals("inTeam") || m.getName().equals("hasIsland")) &&
                    m.getParameterCount()==1 && m.getParameterTypes()[0]==UUID.class) {
                    Boolean b = (Boolean)m.invoke(api, u);
                    if (b) return true;
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }

    private static String role_BSkyBlock(UUID u) {
        try {
            Class<?> API = Class.forName("com.wasteofplastic.bskyblock.api.BSkyBlockAPI");
            Object api = API.getMethod("getInstance").invoke(null);
            for (Method m : API.getMethods()) {
                if ((m.getName().equals("isIslandLeader") || m.getName().equals("isTeamLeader")) &&
                    m.getParameterCount()==1 && m.getParameterTypes()[0]==UUID.class) {
                    Boolean leader = (Boolean)m.invoke(api, u);
                    return leader ? "섬장" : "섬원";
                }
            }
            return hasIsland_BSkyBlock(u) ? "섬원" : "미등록";
        } catch (Throwable ignored) {}
        return null;
    }

    private static boolean classExists(String fqn) {
        try { Class.forName(fqn); return true; } catch (Throwable t) { return false; }
    }
}
