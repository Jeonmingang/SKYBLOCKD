
package com.minkang.ultimate.rps.data;

import com.minkang.ultimate.rps.UltimateRpsPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

public class AttemptsManager {
    private final UltimateRpsPlugin plugin;
    private final File file;
    private YamlConfiguration yaml;

    public AttemptsManager(UltimateRpsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "attempts.yml");
        this.yaml = new YamlConfiguration();
    }

    public void load() {
        if (!file.exists()) save();
        try { yaml.load(file); } catch (Exception e) { e.printStackTrace(); }
        ensureToday();
    }

    private void ensureToday() {
        String today = LocalDate.now().toString();
        String cur = yaml.getString("date", "");
        if (!today.equals(cur)) {
            yaml = new YamlConfiguration();
            yaml.set("date", today);
            save();
        }
    }

    public void save() {
        try { yaml.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public boolean canAttempt(String station, UUID uuid, int limit) {
        if (limit <= 0) return true;
        ensureToday();
        int n = yaml.getInt("counts."+station+"."+uuid.toString(), 0);
        return n < limit;
    }

    public int recordAttempt(String station, UUID uuid) {
        ensureToday();
        String path = "counts."+station+"."+uuid.toString();
        int n = yaml.getInt(path, 0) + 1;
        yaml.set(path, n);
        save();
        return n;
    }

    public int getTodayCount(String station, UUID uuid) {
        ensureToday();
        return yaml.getInt("counts."+station+"."+uuid.toString(), 0);
    }
}
