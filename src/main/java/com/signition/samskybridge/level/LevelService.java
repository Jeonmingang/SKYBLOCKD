package com.signition.samskybridge.level;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LevelService {
    /* LastPlacement removed */}
    private final Map<UUID, LastPlacement> lastPlacements = new HashMap<>();
    
    private final Main plugin;
    private final DataStore store;
    private final Map<String, Integer> blockXp = new HashMap<>();

    public LevelService(Main plugin, DataStore store){
        this.plugin = plugin;
        this.store = store;
        reloadBlocks();
    }

    }
        } catch (Throwable ignored) {}
        // 2) merge fallback blocks.yml
        for (String k : cfg.getKeys(false)){
            blockXp.putIfAbsent(k.toUpperCase(), cfg.getInt(k, 0));
        }
        plugin.getLogger().info("Loaded block XP entries: " + blockXp.size());
    }

    public IslandData getIslandOf(Player p){
        UUID id = p.getUniqueId(); // BentoBox 연동 전까지는 플레이어 UUID=섬ID
        return store.getOrCreate(id, p.getName());
    }

    public void onPlace(org.bukkit.Material mat, org.bukkit.Location loc, org.bukkit.entity.Player p) { /* removed */ }}
                if (!ok) return;
            }
        } catch (Throwable ignored) {}

        // Anti loop: ignore if same player re-places same block at exactly same location within window
        long now = System.currentTimeMillis();
        LastPlacement last = lastPlacements.get(p.getUniqueId());
        if (last != null) {
            boolean sameLoc = last.loc != null && last.loc.getWorld()!=null && loc.getWorld()!=null && last.loc.getWorld().equals(loc.getWorld())
                              && last.loc.getBlockX()==loc.getBlockX()
                              && last.loc.getBlockY()==loc.getBlockY()
                              && last.loc.getBlockZ()==loc.getBlockZ();
            if (sameLoc && last.mat == mat && (now - last.timeMs) < plugin.getConfig().getLong("leveling.anti-loop-ms", plugin.getConfig().getLong("xp.anti-loop-ms", 5000L))) {
                // skip xp grant
                lastPlacements.put(p.getUniqueId(), new LastPlacement(mat, loc.clone(), now));
                return;
            }
        }
        lastPlacements.put(p.getUniqueId(), new LastPlacement(mat, loc.clone(), now));

        Integer xp = blockXp.get(mat.name().toUpperCase());
        if (xp == null) return;
        IslandData is = getIslandOf(p);
        long before = is.getXp();
        is.addXp(xp);
        // Chat notify
        if (plugin.getConfig().getBoolean("xp.chat.notify", true)){
            long need = requiredXp(is.getLevel());
            long remain = Math.max(0, need - is.getXp());
            double percent = need > 0 ? (is.getXp() * 100.0 / need) : 100.0;
            String msg = plugin.getConfig().getString("messages.xp.gain", "&a+<xp> XP &7(다음 레벨까지 <remain> | <percent>%)");
            msg = com.signition.samskybridge.util.Text.color(msg
                    .replace("<xp>", String.valueOf(xp))
                    .replace("<remain>", String.valueOf(remain))
                    .replace("<percent>", String.format("%.1f", percent))
                    .replace("<level>", String.valueOf(is.getLevel())));
            p.sendMessage(msg);
        }
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

    public long requiredXpForLevel(int level){
        // Table override: config 'leveling.table.<level>' if set, else fallback
        try{
            FileConfiguration cfg = Main.get().getConfig();
            if (cfg.isConfigurationSection("leveling.table")){
                String key = "leveling.table." + level;
                if (cfg.isSet(key)){
                    long v = cfg.getLong(key, -1L);
                    if (v >= 0) return v;
                }
            }
        }catch (Throwable ignored){}
        return requiredXp(level);
    }

    public void tryLevelUp(IslandData is){
        // Level up repeatedly while XP meets requirement
        while (is.getXp() >= requiredXpForLevel(is.getLevel())){
            is.setLevel(is.getLevel() + 1);
            try {
                org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayer(is.getId());
                if (p != null){
                    String msg = com.signition.samskybridge.util.Text.color(
                        Main.get().getConfig().getString("messages.level.leveled-up","&a레벨업! 현재 레벨: &f<level>").replace("<level>", String.valueOf(is.getLevel()))
                    );
                    p.sendMessage(msg);
                }
            } catch (Throwable ignored){}
        }
    }

    public void applyXpPurchase(IslandData is, long add){
        long before = is.getXp();
        is.addXp(add);
        tryLevelUp(is);
    }

}
