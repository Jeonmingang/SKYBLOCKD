
package com.minkang.ultimate.rps.holo;

import com.minkang.ultimate.rps.UltimateRpsPlugin;
import com.minkang.ultimate.rps.station.Station;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HologramManager {

    private final UltimateRpsPlugin plugin;
    private final NamespacedKey KEY;

    public HologramManager(UltimateRpsPlugin plugin) {
        this.plugin = plugin;
        this.KEY = new NamespacedKey(plugin, "ultirps-holo");
    }

    public void spawnOrRefresh(Station st) {
        despawn(st);
        List<String> lines = st.getCustomHologramLines();
        if (lines == null || lines.isEmpty()) {
            lines = plugin.getConfig().getStringList("hologram.lines");
        }
        String coinName = "미설정";
        if (st.getCoinItem() != null) {
            try {
                if (st.getCoinItem().hasItemMeta() && st.getCoinItem().getItemMeta().hasDisplayName()) {
                    coinName = st.getCoinItem().getItemMeta().getDisplayName();
                } else {
                    coinName = st.getCoinItem().getType().name();
                }
            } catch (Throwable ignored) {}
        }
        int minMul = 0, maxMul = 0, idx = 1;
        for (int w : st.getWedges()) {
            if (w > 0) {
                if (minMul == 0) minMul = idx;
                maxMul = idx;
            }
            idx++;
        }

        List<UUID> ids = new ArrayList<>();
        Location base = st.getBlockLocation().clone().add(0.5, 1.2, 0.5);
        double dy = 0.28;
        for (int i = lines.size() - 1; i >= 0; i--) {
            String line = color(lines.get(i)
                    .replace("{name}", st.getName())
                    .replace("{coin}", coinName)
                    .replace("{최소}", (minMul == 0 ? "1" : String.valueOf(minMul)))
                    .replace("{최대}", (maxMul == 0 ? "1" : String.valueOf(maxMul))));
            ArmorStand as = (ArmorStand) base.getWorld().spawnEntity(
                    base.clone().add(0, (lines.size() - 1 - i) * dy, 0), EntityType.ARMOR_STAND);
            as.setMarker(true);
            as.setGravity(false);
            as.setVisible(false);
            as.setCustomNameVisible(true);
            as.setCustomName(line);
            try {
                PersistentDataContainer pdc = as.getPersistentDataContainer();
                pdc.set(KEY, PersistentDataType.STRING, st.getName());
            } catch (Throwable ignored) {}
            ids.add(as.getUniqueId());
        }
        st.setHologramIds(ids);
        plugin.stations().save();
    }

    public void despawn(Station st) {
        // remove by stored UUIDs
        for (UUID id : st.getHologramIds()) {
            Entity e = plugin.getServer().getEntity(id);
            if (e != null) e.remove();
        }
        st.setHologramIds(new ArrayList<>());

        // extra safety: scan nearby area
        try {
            Location loc = st.getBlockLocation();
            for (Entity e : loc.getWorld().getNearbyEntities(loc, 3.0, 6.0, 3.0)) {
                if (e instanceof ArmorStand) {
                    ArmorStand as = (ArmorStand) e;
                    boolean tagged = false;
                    try {
                        String v = as.getPersistentDataContainer().get(KEY, PersistentDataType.STRING);
                        tagged = (v != null && v.equals(st.getName()));
                    } catch (Throwable ignored) {}
                    String cn = as.getCustomName();
                    if (tagged || (cn != null && ChatColor.stripColor(cn).contains(st.getName()))) {
                        as.remove();
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    public void despawnAll() {
        for (String n : plugin.stations().getNames()) {
            Station st = plugin.stations().getByName(n);
            if (st != null) despawn(st);
        }
    }

    public void hardClearAll() {
        try {
            for (org.bukkit.World w : plugin.getServer().getWorlds()) {
                for (Entity e : w.getEntities()) {
                    if (e instanceof ArmorStand) {
                        ArmorStand as = (ArmorStand) e;
                        evaluateAndMaybeRemove(as);
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    public boolean evaluateAndMaybeRemove(ArmorStand as) {
        boolean remove = false;
        try {
            String v = as.getPersistentDataContainer().get(KEY, PersistentDataType.STRING);
            if (v != null && !v.isEmpty()) {
                Station st = plugin.stations().getByName(v);
                if (st == null || !st.isHologram()) {
                    remove = true;
                } else {
                    if (as.getLocation().distanceSquared(st.getBlockLocation()) > 25) {
                        remove = true;
                    }
                }
            } else {
                String cn = as.getCustomName();
                remove = isOurHoloName(cn);
            }
        } catch (Throwable ignored) {}
        if (remove) as.remove();
        return remove;
    }

    private boolean isOurHoloName(String cn) {
        if (cn == null) return false;
        cn = ChatColor.stripColor(cn).toLowerCase();
        String[] keys = new String[]{ "가위바위보", "기계:", "코인:", "배수정보", "우클릭하여 시작" };
        for (String k : keys) if (cn.contains(k.toLowerCase())) return true;
        return false;
    }

    private String color(String s) { return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s); }
}
