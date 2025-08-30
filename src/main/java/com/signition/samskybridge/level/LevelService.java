package com.signition.samskybridge.level;
import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File; import java.util.*;
public class LevelService {
  private final Main plugin; private final DataStore store; private final Map<String, Long> blockXp = new HashMap<>();
  public LevelService(Main plugin, DataStore store){ this.plugin = plugin; this.store = store; loadBlocks(); }
  public void reload(){ blockXp.clear(); loadBlocks(); }
  private void loadBlocks(){
    try{
      File f = new File(plugin.getDataFolder(), "blocks.yml");
      if (!f.getParentFile().exists()) f.getParentFile().mkdirs();
      if (!f.exists()){
        YamlConfiguration y = new YamlConfiguration();
        y.set("minecraft:stone", 1);
        // Pixelmon 예시 키 (하이브리드 서버에서 NamespacedKey가 노출되는 경우)
        y.set("pixelmon:ruby_block", 10);
        y.set("pixelmon:platinum_block", 12);
        y.set("pixelmon:aluminum_block", 8);
        y.set("pixelmon:amethyst_block", 14);
        y.set("pixelmon:sapphire_block", 16);
        y.set("pixelmon:silicon_block", 9);
        y.save(f);
      }
      YamlConfiguration y = YamlConfiguration.loadConfiguration(f);
      for (String k : y.getKeys(false)){
        long xp = y.getLong(k, 0);
        blockXp.put(k.toLowerCase(), xp);
      }
    }catch(Exception e){ plugin.getLogger().warning("blocks.yml load error: "+e.getMessage()); }
  }
  public void onPlace(Material mat, Player p){
    if (p==null) return;
    IslandData is = store.findByMember(p.getUniqueId()).orElseGet(()->store.getOrCreate(p.getUniqueId()));
    Location l = p.getLocation().getBlock().getLocation();
    String key = (l.getWorld()!=null?l.getWorld().getName():"world")+":"+l.getBlockX()+":"+l.getBlockY()+":"+l.getBlockZ();
    if (is.hasXpOnce(key)) return;
    is.markXpOnce(key);
    String id = mat.name().toLowerCase();
    try{ NamespacedKey nk = mat.getKey(); if (nk!=null) id = (nk.getNamespace()+":"+nk.getKey()).toLowerCase(); }catch(Throwable ignored){}
    long add = blockXp.getOrDefault(id, blockXp.getOrDefault(mat.name().toLowerCase(), 0L));
    if (add<=0) return;
    is.addXp(add);
    long need = requiredXp(is.getLevel());
    while (is.getXp() >= need){
      is.setXp(is.getXp()-need); is.setLevel(is.getLevel()+1);
      String msg = Text.color(plugin.getConfig().getString("messages.level-up","&a레벨업! 현재 레벨: &f<level>").replace("<level>", String.valueOf(is.getLevel())));
      p.sendMessage(msg);
      need = requiredXp(is.getLevel());
    }
    String fmt = plugin.getConfig().getString("messages.level-bar","&7경험치: &f<cur>&7/&f<need>");
    p.sendMessage(Text.color(fmt.replace("<cur>", String.valueOf(is.getXp())).replace("<need>", String.valueOf(need))));
  }
  public long requiredXp(int level){
    double base = plugin.getConfig().getDouble("level.base", 100.0);
    double growth = plugin.getConfig().getDouble("level.growth", 1.15);
    return (long)Math.max(10, Math.floor(base*Math.pow(growth, Math.max(0, level))));
  }
}
