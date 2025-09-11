
package com.signition.samskybridge.level;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.integration.IslandSync;
import com.signition.samskybridge.util.Hud;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class LevelService {
    private final Main plugin;
    private final DataStore store;

    public LevelService(Main plugin, DataStore store){
        this.plugin = plugin;
        this.store = store;
    }

    public void onPlace(BlockPlaceEvent e){
        Player p = e.getPlayer();

        // Sync from Bento if needed
        IslandSync.ensureSyncedFromBento(plugin, store, p);

        // Allowed worlds
        List<String> worlds = plugin.getConfig().getStringList("xp.allowed-worlds");
        if (!worlds.isEmpty() && !worlds.contains(e.getBlock().getWorld().getName())) return;

        int give = xpFor(e.getBlockPlaced());
        if (give <= 0) return;

        IslandData is = store.findByMember(p.getUniqueId()).orElseGet(() -> store.getOrCreate(p.getUniqueId()));
        long cur = is.getXp() + give;
        long need = requiredXp(is.getLevel());
        if (cur >= need){
            is.setLevel(is.getLevel() + 1);
            is.setXp(cur - need);
            Hud.levelUp(plugin, is, is.getLevel(), requiredXp(is.getLevel()));
        } else {
            is.setXp(cur);
        }
        store.saveAsync();
    }

    public long requiredXp(long level){
        if (level < 0) level = 0;
        return 100 + level * level * 20L;
    }

    private static final Map<Material,Integer> XP_TABLE = new HashMap<Material,Integer>();
    static {
        XP_TABLE.put(Material.COBBLESTONE, 1);
        XP_TABLE.put(Material.STONE, 1);
        XP_TABLE.put(Material.OAK_LOG, 2);
        XP_TABLE.put(Material.IRON_BLOCK, 5);
        XP_TABLE.put(Material.DIAMOND_BLOCK, 15);
    }

    public int xpFor(Block b){
        if (b == null) return 0;
        Integer val = XP_TABLE.get(b.getType());
        return val == null ? 0 : val.intValue();
    }
}
