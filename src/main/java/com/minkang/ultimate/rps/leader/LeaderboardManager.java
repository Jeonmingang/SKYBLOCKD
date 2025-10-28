package com.minkang.ultimate.rps.leader;

import com.minkang.ultimate.rps.UltimateRpsPlugin;
import com.minkang.ultimate.rps.data.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LeaderboardManager {

    private final UltimateRpsPlugin plugin;
    private File file;
    private YamlConfiguration yaml;
    private Location loc;
    private List<UUID> holo = new ArrayList<>();

    public LeaderboardManager(UltimateRpsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "leaderboard.yml");
        this.yaml = new YamlConfiguration();
    }

    public void load() {
        if (!file.exists()) return;
        try { yaml.load(file); } catch (Exception e) { e.printStackTrace(); }
        String world = yaml.getString("world", null);
        if (world == null) return;
        World w = Bukkit.getWorld(world);
        if (w == null) return;
        this.loc = new Location(w, yaml.getDouble("x"), yaml.getDouble("y"), yaml.getDouble("z"));
        List<String> ids = yaml.getStringList("holo");
        this.holo.clear();
        for (String s : ids) { try { this.holo.add(UUID.fromString(s)); } catch (Exception ignored) {} }
        spawnOrRefresh();
    }

    private void save() {
        if (loc == null) return;
        yaml = new YamlConfiguration();
        yaml.set("world", loc.getWorld().getName());
        yaml.set("x", loc.getX()); yaml.set("y", loc.getY()); yaml.set("z", loc.getZ());
        List<String> ids = new ArrayList<>();
        for (UUID id : holo) ids.add(id.toString());
        yaml.set("holo", ids);
        try { yaml.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public boolean isInstalled() { return loc != null; }
    public Location getLocation() { return loc; }

    public void install(Location base) {
        this.loc = base.getBlock().getLocation();
        spawnOrRefresh();
        save();
    }

    public void remove() {
        despawn();
        this.loc = null;
        if (file.exists()) file.delete();
    }

    public void spawnOrRefresh() {
        if (loc == null) return;
        despawn();
        List<String> lines = new ArrayList<>();
        String title = plugin.getConfig().getString("leaderboard.title","&b&l가위바위보 랭킹");
        lines.add(ChatColor.translateAlternateColorCodes('&', title));
        StatsManager sm = plugin.stats();
        int max = plugin.getConfig().getInt("leaderboard.max-lines", 10);
        List<String> tops = sm.buildTopLines(max);
        if (tops.isEmpty()) lines.add(ChatColor.GRAY + "데이터가 없습니다.");
        else lines.addAll(tops);
        Location base = loc.clone().add(0.5, 1.5, 0.5);
        double dy = 0.28;
        for (int i = lines.size()-1; i >= 0; i--) {
            ArmorStand as = (ArmorStand) base.getWorld().spawnEntity(base.clone().add(0, (lines.size()-1-i)*dy, 0), EntityType.ARMOR_STAND);
            as.setMarker(true); as.setGravity(false); as.setVisible(false);
            as.setCustomNameVisible(true); as.setCustomName(lines.get(i));
            holo.add(as.getUniqueId());
        }
        save();
    }

    public void despawn() {
        for (UUID id : holo) {
            if (plugin.getServer().getEntity(id) != null) plugin.getServer().getEntity(id).remove();
        }
        holo.clear();
        save();
    }
}
