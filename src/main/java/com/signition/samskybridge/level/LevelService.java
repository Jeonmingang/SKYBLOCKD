
package com.signition.samskybridge.level;
import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Text;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LevelService {
  private boolean xpDebug(){ return plugin.getConfig().getBoolean("xp.debug", false); }
  private void debug(Player p, String m){ if (xpDebug() && p!=null) p.sendMessage(com.signition.samskybridge.util.Text.color("&8[XP 디버그] &7"+m)); }
  private final Main plugin; private final DataStore store; private final Map<String, Long> blockXp = new HashMap<String, Long>();
  private final NamespacedKey minedKey;
  private final Set<String> allowedWorlds = new HashSet<String>();

  public LevelService(Main plugin, DataStore store){
    this.plugin=plugin; this.store=store;
    this.minedKey = new NamespacedKey(plugin, "mined_item");
    loadBlocks();
    loadWorlds();
  }
  public void reload(){ blockXp.clear(); loadBlocks(); loadWorlds(); }

  private void loadWorlds(){
    allowedWorlds.clear();
    java.util.List<String> ws = plugin.getConfig().getStringList("xp.allowed-worlds");
    if (ws==null || ws.isEmpty()) allowedWorlds.add("bskyblock_world");
    else for (String w : ws) allowedWorlds.add(w);
  }

  private void loadBlocks(){
    try{
      File f = new File(plugin.getDataFolder(), "blocks.yml");
      if (!f.getParentFile().exists()) f.getParentFile().mkdirs();
      if (!f.exists()){
        YamlConfiguration y=new YamlConfiguration();
        y.set("minecraft:stone", 1);
        y.set("pixelmon:ruby_block", 10);
        y.set("pixelmon:platinum_block", 12);
        y.set("pixelmon:aluminum_block", 8);
        y.set("pixelmon:amethyst_block", 14);
        y.set("pixelmon:sapphire_block", 16);
        y.set("pixelmon:silicon_block", 9);
        y.save(f);
      }
      YamlConfiguration y=YamlConfiguration.loadConfiguration(f);
      for (String k : y.getKeys(false)) blockXp.put(k.toLowerCase(), y.getLong(k,0));
    }catch(Exception ex){ plugin.getLogger().warning("blocks.yml load error: "+ex.getMessage()); }
  }

  public void onPlace(BlockPlaceEvent e){
    Player p = e.getPlayer();
    if (p==null) return;
    if (e.getBlock()==null || e.getBlock().getWorld()==null) return;
    String worldName = e.getBlock().getWorld().getName();
    if (!allowedWorlds.contains(worldName)) return; // only in allowed worlds

    // If the item used to place is from a mined drop, no XP
    ItemStack hand = e.getItemInHand();
    if (hand!=null){
      try{
        ItemMeta meta = hand.getItemMeta();
        if (meta!=null && meta.getPersistentDataContainer().has(minedKey, org.bukkit.persistence.PersistentDataType.BYTE)){
          return; // mined item -> no XP
        }
      }catch(Throwable ignored){}
    }

    IslandData is = store.findByMember(p.getUniqueId()).orElseGet(new java.util.function.Supplier<IslandData>(){ public IslandData get(){ return store.getOrCreate(p.getUniqueId()); }});

    // Identify block key (namespace if available)
    String id=e.getBlockPlaced().getType().name().toLowerCase();
    try{ org.bukkit.NamespacedKey nk = e.getBlockPlaced().getType().getKey(); if (nk!=null) id=(nk.getNamespace()+":"+nk.getKey()).toLowerCase(); }catch(Throwable ignored){}
    long add = blockXp.getOrDefault(id, blockXp.getOrDefault(e.getBlockPlaced().getType().name().toLowerCase(), 0L));
    if (add<=0) { add = (long) plugin.getConfig().getDouble("xp.default-per-block", 0.0); if (add<=0) { debug(p, "블럭 XP 매핑/기본값=0: XP 미지급"); return; } }

    is.addXp(add); debug(p, "XP +"+add+" (id="+id+")");
    long need=requiredXp(is.getLevel());
    while (is.getXp()>=need){ is.setXp(is.getXp()-need); is.setLevel(is.getLevel()+1); p.sendMessage(Text.color(plugin.getConfig().getString("messages.level-up","&a레벨업! 현재 레벨: &f<level>").replace("<level>", String.valueOf(is.getLevel())))); need=requiredXp(is.getLevel()); }
    String fmt=plugin.getConfig().getString("messages.level-bar","&7경험치: &f<cur>&7/&f<need>");
    p.sendMessage(Text.color(fmt.replace("<cur>", String.valueOf(is.getXp())).replace("<need>", String.valueOf(need))));
  }

  public long requiredXp(int level){ double base=plugin.getConfig().getDouble("level.base",100.0); double g=plugin.getConfig().getDouble("level.growth",1.15); return (long)Math.max(10, Math.floor(base*Math.pow(g, Math.max(0, level)))); }

  public NamespacedKey getMinedKey(){ return minedKey; }
}
