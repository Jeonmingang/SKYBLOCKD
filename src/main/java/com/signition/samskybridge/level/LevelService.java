package com.signition.samskybridge.level;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LevelService {
    private final Main plugin;
    private final DataStore store;
    private final Map<String, Integer> blockXp = new HashMap<>();

    public LevelService(Main plugin, DataStore store){
        this.plugin = plugin;
        this.store = store;
        reloadBlocks();
    }

    public void reloadBlocks(){
        File f = new File(plugin.getDataFolder(), "blocks.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        blockXp.clear();
        for (String k : cfg.getKeys(false)){
            blockXp.put(k.toUpperCase(), cfg.getInt(k, 0));
        }
        plugin.getLogger().info("Loaded block XP entries: " + blockXp.size());
    }

    public IslandData getIslandOf(Player p){
        UUID id = p.getUniqueId(); // BentoBox 연동 전까지는 플레이어 UUID=섬ID
        return store.getOrCreate(id, p.getName());
    }

    public void onPlace(Material mat, Player p){
        if (!plugin.getConfig().getBoolean("leveling.track-placements", true)) return;
        Integer xp = blockXp.get(mat.name().toUpperCase());
        if (xp == null) return;
        IslandData is = getIslandOf(p);
        long before = is.getXp();
        is.addXp(xp);
        checkLevelUp(is, before);
    }

    private void checkLevelUp(IslandData is, long before){
        long need = requiredXp(is.getLevel());
        if (is.getXp() >= need){
            is.setLevel(is.getLevel()+1);
            // 메시지 전송
            plugin.getServer().getOnlinePlayers().forEach(pl -> {
                if (pl.getUniqueId().equals(is.getId())){
                    String msg = com.signition.samskybridge.util.Text.color(
                        plugin.getConfig().getString("messages.level.leveled-up","레벨업!").replace("<level>", String.valueOf(is.getLevel()))
                    );
                }
            });
        }
    }

    public long requiredXp(int level){
        double base = plugin.getConfig().getDouble("leveling.base-required-xp", 1000.0);
        double inc = plugin.getConfig().getDouble("leveling.increase-percent", 1.5) / 100.0;
        double need = base;
        for (int i=1;i<level;i++){
            need = Math.ceil(need * (1.0 + inc));
        }
        return (long) need;
    }
}