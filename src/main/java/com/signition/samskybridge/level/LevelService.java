package com.signition.samskybridge.level;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LevelService {
    private final Main plugin;
    private final DataStore store;
    private final NamespacedKey minedKey;
    private final Map<String, Long> blockXp = new HashMap<String, Long>();

    public LevelService(Main plugin, DataStore store){
        this.plugin=plugin; this.store=store;
        this.minedKey = new NamespacedKey(plugin, "mined_item");
        loadBlocks();
    }

    public NamespacedKey getMinedKey(){ return minedKey; }

    private boolean xpDebug(){ return plugin.getConfig().getBoolean("xp.debug", false); }
    private void debug(Player p, String m){ if (xpDebug() && p!=null) p.sendMessage(Text.color("&8[XP 디버그] &7"+m)); }

    private void loadBlocks(){
        try{
            File f = new File(plugin.getDataFolder(), "blocks.yml");
            if (!f.getParentFile().exists()) f.getParentFile().mkdirs();
            if (!f.exists()){
                YamlConfiguration y=new YamlConfiguration();
                y.set("minecraft:diamond_block", 20);
                y.set("pixelmon:ruby_block", 12);
                y.save(f);
            }
            YamlConfiguration y = YamlConfiguration.loadConfiguration(f);
            for (String k : y.getKeys(false)){
                blockXp.put(k.toLowerCase(), y.getLong(k));
            }
        }catch(Exception e){
            plugin.getLogger().warning("blocks.yml 로드 실패: "+e.getMessage());
        }
    }

    
public void onPlace(BlockPlaceEvent e){
    // Guard: cancelled or cannot build
    if (e.isCancelled() || (e.isCancelled()==false && e.canBuild()==false)) return;

    final Player p = e.getPlayer();
    final String world = e.getBlockPlaced().getWorld().getName();
    java.util.List<String> allowed = plugin.getConfig().getStringList("xp.allowed-worlds");
    if (allowed==null || !allowed.contains(world)){ debug(p,"허용 월드 아님: "+world); return; }

    final java.util.Optional<IslandData> opt = store.findByMember(p.getUniqueId());
    if (!opt.isPresent()){ debug(p, "섬 데이터 없음(멤버 아님)"); return; }
    final IslandData is = opt.get();

    // capture item-tag now (hand might change next tick)
    final boolean fromDropTag;
    {
        ItemStack inHand = e.getItemInHand();
        boolean tagged = false;
        if (inHand!=null && inHand.hasItemMeta()){
            ItemMeta im = inHand.getItemMeta();
            if (im!=null && im.getPersistentDataContainer().has(minedKey, PersistentDataType.BYTE)){
                tagged = true;
            }
        }
        fromDropTag = tagged;
    }

    // capture placed type/location to verify actual placement next tick
    final Material placedType = e.getBlockPlaced().getType();
    final Material replacedType = e.getBlockReplacedState().getType();
    final Location loc = e.getBlockPlaced().getLocation();

    Bukkit.getScheduler().runTask(plugin, new Runnable(){
        @Override public void run(){
            // Only award if the block actually changed to the placed type
            Material now = loc.getBlock().getType();
            if (now != placedType || now == replacedType){
                debug(p, "실제 배치되지 않음(우클릭 상호작용 등): XP 미지급");
                return;
            }
            if (fromDropTag){
                debug(p, "드랍 재설치 감지: XP 미지급");
                return;
            }

            // resolve block id (lowercase material and try namespaced key)
            String id = now.name().toLowerCase();
            long add = 0L;
            if (blockXp.containsKey(id)) add = blockXp.get(id);
            else {
                try{
                    String ns = loc.getBlock().getBlockData().getMaterial().getKey().toString().toLowerCase();
                    if (blockXp.containsKey(ns)) add = blockXp.get(ns);
                }catch(Throwable ignored){}
            }
            if (add<=0){
                add = (long) plugin.getConfig().getDouble("xp.default-per-block", 0.0);
                if (add<=0){ debug(p, "블럭 XP 매핑/기본값=0: XP 미지급"); return; }
            }

            is.addXp(add);
            debug(p, "XP +"+add+" (id="+id+")");
        }
    });
}



public String formatProgressBar(long cur, long need){
    int bars = 10;
    double pct = need <= 0 ? 1.0 : Math.min(1.0, cur * 1.0 / need);
    int filled = (int)Math.floor(pct * bars);
    StringBuilder sb = new StringBuilder();
    sb.append("&8[");
    for (int i=0;i<bars;i++){
        sb.append(i<filled? "&a|" : "&7|");
    }
    sb.append("&8] ");
    sb.append(String.format("&f%d&7/&f%d &7(%.0f%%)", cur, need, pct*100.0));
    return com.signition.samskybridge.util.Text.color(sb.toString());
}


public void addIslandXp(IslandData is, long add){
    if (add <= 0) return;
    long cur = is.getXp();
    cur += add;
    // level-up loop with carry over XP
    int lv = is.getLevel();
    long need = requiredXp(lv);
    while (cur >= need && lv < Integer.MAX_VALUE){
        cur -= need;
        lv++;
        long nextNeed = requiredXp(lv);
        is.setLevel(lv);
        is.setXp(cur);
        // HUD + chat
        com.signition.samskybridge.util.Hud.levelUp(plugin, is, lv, nextNeed);
        need = nextNeed;
    }
    is.setXp(cur);
}


public synchronized void reloadBlocksIfChanged(){
    long lm = blocksFile.lastModified();
    if (lm != blocksLastModified){
        loadBlocks();
        blocksLastModified = lm;
    }
}

private void loadBlocks(){
    blockXp.clear();
    org.bukkit.configuration.file.YamlConfiguration y = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(blocksFile);
    for (String k : y.getKeys(false)){
        try{
            long v = y.getLong(k, 0L);
            blockXp.put(k.toLowerCase(), v);
        }catch(Exception ignore){}
    }
}
