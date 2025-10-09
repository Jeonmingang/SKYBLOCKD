
package com.signition.samskybridge.upgrade;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.util.Text;
import com.signition.samskybridge.util.VaultHook;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class UpgradeService {
  private final Main plugin;
  private final DataStore store;
  public UpgradeService(Main plugin, DataStore store){
    this.plugin = plugin;
    this.store = store;
  }

  public Inventory build(Player p){
    int size = plugin.getConfig().getInt("upgrade.gui.inventory-size", 27);
    String title = Text.color(plugin.getConfig().getString("gui.title-upgrade", "&f섬 업그레이드"));
    Inventory inv = Bukkit.createInventory(p, size, title);

    // size item
    int slotSize = plugin.getConfig().getInt("upgrade.gui.slots.size", 12);
    inv.setItem(slotSize, sizeItem(p));

    // team item
    int slotTeam = plugin.getConfig().getInt("upgrade.gui.slots.team", 14);
    inv.setItem(slotTeam, teamItem(p));

    // xp item
    int slotXp = plugin.getConfig().getInt("upgrade.gui.slots.xp", 22);
    inv.setItem(slotXp, xpItem(p));

    return inv;
  }

  private ItemStack makeItem(Material m, String name, List<String> lore){
    ItemStack it = new ItemStack(m);
    ItemMeta meta = it.getItemMeta();
    if (meta != null){
      meta.setDisplayName(Text.color(name));
      List<String> lines = new ArrayList<>();
      for (String l: lore) lines.add(Text.color(l));
      meta.setLore(lines);
      it.setItemMeta(meta);
    }
    return it;
  }

  private int currentSize(Player p){
    int def = plugin.getConfig().getInt("defaults.size", 120);
    return store.getSize(p.getUniqueId(), def);
  }
  private int currentTeam(Player p){
    int def = plugin.getConfig().getInt("defaults.team-max", 4);
    return store.getTeam(p.getUniqueId(), def);
  }

  private int nextSizeTierValue(Player p){
    int cur = currentSize(p);
    ConfigurationSection tiers = plugin.getConfig().getConfigurationSection("upgrade.size.tiers");
    if (tiers == null) return cur;
    int bestNext = Integer.MAX_VALUE;
    for (String k: tiers.getKeys(false)){
      int range = tiers.getInt(k+".range", cur);
      if (range > cur) bestNext = Math.min(bestNext, range);
    }
    return bestNext==Integer.MAX_VALUE?cur:bestNext;
  }

  private int nextTeamTierValue(Player p){
    int cur = currentTeam(p);
    ConfigurationSection tiers = plugin.getConfig().getConfigurationSection("upgrade.team.tiers");
    if (tiers == null) return cur;
    int bestNext = Integer.MAX_VALUE;
    for (String k: tiers.getKeys(false)){
      int next = tiers.getInt(k+".next", cur);
      if (next > cur) bestNext = Math.min(bestNext, next);
    }
    return bestNext==Integer.MAX_VALUE?cur:bestNext;
  }

  private ItemStack sizeItem(Player p){
    int cur = currentSize(p);
    int next = nextSizeTierValue(p);
    int needLvl = findNeedLevelForSize(next);
    int cost = findCostForSize(next);

    List<String> lore = plugin.getConfig().getStringList("size.lore");
    if (lore.isEmpty()){
      lore = List.of("&7현재: &f<current> &7→ 다음: &f<next>",
                     "&7필요 레벨: &a<level>",
                     "&7가격: &6<cost>");
    }
    lore = loreReplace(lore, cur, next, needLvl, cost);
    return makeItem(Material.GRASS_BLOCK, plugin.getConfig().getString("size.name","&a섬 크기 업그레이드"), lore);
  }

  private ItemStack teamItem(Player p){
    int cur = currentTeam(p);
    int next = nextTeamTierValue(p);
    int needLvl = findNeedLevelForTeam(next);
    int cost = findCostForTeam(next);

    List<String> lore = plugin.getConfig().getStringList("members.lore");
    if (lore.isEmpty()){
      lore = List.of("&7현재: &f<current> &7→ 다음: &f<next>",
                     "&7필요 레벨: &a<level>",
                     "&7가격: &6<cost>");
    }
    lore = loreReplace(lore, cur, next, needLvl, cost);
    return makeItem(Material.PLAYER_HEAD, plugin.getConfig().getString("members.name","&a섬 인원 업그레이드"), lore);
  }

  private ItemStack xpItem(Player p){
    int amt = plugin.getConfig().getInt("upgrade.xp.amount", 1000);
    int cost = plugin.getConfig().getInt("upgrade.xp.cost", 10000);
    List<String> lore = new ArrayList<>();
    lore.add("&7경험치 구매");
    lore.add("&7지급량: &a"+amt);
    lore.add("&7가격: &6"+cost);
    return makeItem(Material.EXPERIENCE_BOTTLE, "&aXP 구매", lore);
  }

  private List<String> loreReplace(List<String> lore, int cur, int next, int needLvl, int cost){
    List<String> out = new ArrayList<>();
    for (String s: lore){
      out.add(Text.color(
        s.replace("<current>", String.valueOf(cur))
         .replace("<next>", String.valueOf(next))
         .replace("<level>", String.valueOf(needLvl))
         .replace("<cost>", String.valueOf(cost))
         .replace("{현재크기}", String.valueOf(cur))
         .replace("{다음크기}", String.valueOf(next))
         .replace("{크기필요레벨}", String.valueOf(needLvl))
         .replace("{크기비용}", String.valueOf(cost))
      ));
    }
    return out;
  }

  private int findNeedLevelForSize(int target){
    int best=0;
    ConfigurationSection tiers = plugin.getConfig().getConfigurationSection("upgrade.size.tiers");
    if (tiers==null) return 0;
    for (String k: tiers.getKeys(false)){
      int r = tiers.getInt(k+".range");
      if (r==target) best = tiers.getInt(k+".need", 0);
    }
    return best;
  }
  private int findCostForSize(int target){
    ConfigurationSection tiers = plugin.getConfig().getConfigurationSection("upgrade.size.tiers");
    if (tiers==null) return 0;
    for (String k: tiers.getKeys(false)){
      int r = tiers.getInt(k+".range");
      if (r==target) return tiers.getInt(k+".cost", 0);
    }
    return 0;
  }
  private int findNeedLevelForTeam(int target){
    ConfigurationSection tiers = plugin.getConfig().getConfigurationSection("upgrade.team.tiers");
    if (tiers==null) return 0;
    for (String k: tiers.getKeys(false)){
      int n = tiers.getInt(k+".next");
      if (n==target) return tiers.getInt(k+".need", 0);
    }
    return 0;
  }
  private int findCostForTeam(int target){
    ConfigurationSection tiers = plugin.getConfig().getConfigurationSection("upgrade.team.tiers");
    if (tiers==null) return 0;
    for (String k: tiers.getKeys(false)){
      int n = tiers.getInt(k+".next");
      if (n==target) return tiers.getInt(k+".cost", 0);
    }
    return 0;
  }

  public void open(Player p){ p.openInventory(build(p)); }

  public void tryBuySize(Player p){
    int cur = currentSize(p);
    int next = nextSizeTierValue(p);
    if (next <= cur){ p.sendMessage(Text.color("&c더 이상 업그레이드가 없습니다.")); return; }
    int need = findNeedLevelForSize(next);
    int cost = findCostForSize(next);
    if (VaultHook.economy() != null && !VaultHook.economy().withdrawPlayer(p, cost).transactionSuccess()){
      p.sendMessage(Text.color("&c잔액이 부족합니다. 가격: &6"+cost)); return;
    }
    store.setSize(p.getUniqueId(), next);
    p.sendMessage(Text.color(plugin.getConfig().getString("size.size-success",
      "&a섬 반경이 &f<current>&a → &f<next>&a 으로 확장되었습니다!")
      .replace("<current>", String.valueOf(cur)).replace("<next>", String.valueOf(next))));
    open(p);
  }

  public void tryBuyTeam(Player p){
    int cur = currentTeam(p);
    int next = nextTeamTierValue(p);
    if (next <= cur){ p.sendMessage(Text.color("&c더 이상 업그레이드가 없습니다.")); return; }
    int need = findNeedLevelForTeam(next);
    int cost = findCostForTeam(next);
    if (VaultHook.economy() != null && !VaultHook.economy().withdrawPlayer(p, cost).transactionSuccess()){
      p.sendMessage(Text.color("&c잔액이 부족합니다. 가격: &6"+cost)); return;
    }
    store.setTeam(p.getUniqueId(), next);
    p.sendMessage(Text.color("&a섬 인원이 &f"+cur+"&a → &f"+next+"&a 로 확장되었습니다!"));
    open(p);
  }

  public void buyXp(Player p){
    int amt = plugin.getConfig().getInt("upgrade.xp.amount", 1000);
    int cost = plugin.getConfig().getInt("upgrade.xp.cost", 10000);
    if (VaultHook.economy() != null && !VaultHook.economy().withdrawPlayer(p, cost).transactionSuccess()){
      p.sendMessage(Text.color("&c잔액이 부족합니다. 가격: &6"+cost)); return;
    }
    p.giveExp(amt);
    p.sendMessage(Text.color("&a경험치 &f"+amt+"&a 지급됨!"));
    open(p);
  }
}
