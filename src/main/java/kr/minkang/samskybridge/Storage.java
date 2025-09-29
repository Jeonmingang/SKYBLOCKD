
package kr.minkang.samskybridge;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Storage {

    private final Main plugin;
    private final File file;
    private final FileConfiguration data;

    public Storage(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "islands.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public IslandData getIslandByOwner(UUID owner) {
        String path = "islands."+owner.toString();
        if (!data.isConfigurationSection(path)) return null;
        return read(owner, data.getConfigurationSection(path));
    }

    public IslandData getIslandByPlayer(UUID player) {
        // owner or member
        if (getIslandByOwner(player) != null) return getIslandByOwner(player);
        for (String key : data.getConfigurationSection("islands") == null ? new ArrayList<String>() : data.getConfigurationSection("islands").getKeys(false)) {
            UUID owner = UUID.fromString(key);
            ConfigurationSection sec = data.getConfigurationSection("islands."+key);
            List<String> members = sec.getStringList("members");
            for (String m : members) {
                if (m.equals(player.toString())) {
                    return read(owner, sec);
                }
            }
        }
        return null;
    }

    public List<IslandData> getAllIslands() {
        List<IslandData> list = new ArrayList<>();
        ConfigurationSection root = data.getConfigurationSection("islands");
        if (root == null) return list;
        for (String key : root.getKeys(false)) {
            UUID owner = UUID.fromString(key);
            list.add(read(owner, root.getConfigurationSection(key)));
        }
        return list;
    }

    public void write(IslandData d) {
        String path = "islands."+d.owner.toString();
        data.set(path+".level", d.level);
        data.set(path+".xp", d.xp);
        data.set(path+".sizeRadius", d.sizeRadius);
        data.set(path+".teamMax", d.teamMax);
        List<String> m = new ArrayList<>();
        for (UUID u : d.members) m.add(u.toString());
        data.set(path+".members", m);
        save();
    }

    private IslandData read(UUID owner, ConfigurationSection sec) {
        IslandData d = new IslandData(owner);
        d.level = sec.getInt("level", 1);
        d.xp = sec.getInt("xp", 0);
        d.sizeRadius = sec.getInt("sizeRadius", plugin.getConfig().getInt("upgrade.size.base-radius", 50));
        d.teamMax = sec.getInt("teamMax", plugin.getConfig().getInt("upgrade.team.base-members", 2));
        List<String> members = sec.getStringList("members");
        for (String s : members) {
            try { d.members.add(UUID.fromString(s)); } catch (Exception ignored) {}
        }
        return d;
    }
}
