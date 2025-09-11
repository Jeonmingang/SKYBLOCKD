
package com.signition.samskybridge.gui;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.integration.BentoBridge;
import com.signition.samskybridge.util.Eco;
import com.signition.samskybridge.util.Text;
import com.signition.samskybridge.util.Hud;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class UpgradeGui {
  private final Main plugin; private final DataStore store;
  public UpgradeGui(Main plugin, DataStore store){ this.plugin=plugin; this.store=store; }
  private static final String TITLE = "섬 업그레이드";

  public void open(Player p){
    Inventory inv = Bukkit.createInventory(p, 54, Text.color("&a"+TITLE));
    DataStore ds = store;
    java.util.Optional<com.signition.samskybridge.data.IslandData> opt = ds.findByMember(p.getUniqueId());
    IslandData is = opt.orElseGet(() -> ds.getOrCreate(p.getUniqueId()));

    // slots - swap: 머리(팀) on left, 블럭(크기) on right
    inv.setItem(20, headTeam(p, is));
    inv.setItem(24, blockSize(is));

    p.openInventory(inv);
  }

  private ItemStack headTeam(Player p, IslandData is){
    ItemStack it = new ItemStack(Material.PLAYER_HEAD);
    SkullMeta sm = (SkullMeta)it.getItemMeta();
    sm.setOwningPlayer(p);
    sm.setDisplayName(Text.color("&b섬 인원 업그레이드"));
    int step = is.getTeamLevel();
    int base = plugin.getConfig().getInt("upgrade.team.base-members", 4);
    int per = plugin.getConfig().getInt("upgrade.team.per-level", 1);
    int limit = base + per * step;
    double cost = plugin.getConfig().getDouble("upgrade.team.cost-base", 80000.0) * Math.pow(plugin.getConfig().getDouble("upgrade.team.cost-multiplier",1.35), step);
    int reqLv = plugin.getConfig().getInt("upgrade.team.required-level-base", 5) + plugin.getConfig().getInt("upgrade.team.required-level-step",2)* step;
    java.util.List<String> lore = new java.util.ArrayList<>();
    lore.add(Text.color("&7현재 한도: &f"+limit+"명"));
    lore.add(Text.color("&7다음 단계: &a"+(limit+per)+"명"));
    lore.add(Text.color("&7요구 레벨: &bLv."+reqLv));
    lore.add(Text.color("&7필요 금액: &a"+String.format("%,.0f", cost)));
    lore.add(Text.color("&7업그레이드 레벨: &d"+step));
    lore.add(Text.color("&7업그레이드 레벨: &d"+step));
    lore.add(Text.color("&8클릭: 업그레이드"));
    sm.setLore(lore); it.setItemMeta(sm);
    return it;
  }

  private ItemStack blockSize(IslandData is){
    ItemStack it = new ItemStack(Material.IRON_BLOCK);
    ItemMeta im = it.getItemMeta();
    im.setDisplayName(Text.color("&e섬 크기 업그레이드"));
    int step = is.getSizeLevel();
    int base = plugin.getConfig().getInt("upgrade.size.base-range", 50);
    int per = plugin.getConfig().getInt("upgrade.size.per-level", 10);
    int range = base + per * step;
    double cost = plugin.getConfig().getDouble("upgrade.size.cost-base", 50000.0) * Math.pow(plugin.getConfig().getDouble("upgrade.size.cost-multiplier",1.25), step);
    int reqLv = plugin.getConfig().getInt("upgrade.size.required-level-base", 3) + plugin.getConfig().getInt("upgrade.size.required-level-step",2)* step;
    java.util.List<String> lore = new java.util.ArrayList<>();
    lore.add(Text.color("&7현재 보호반경: &f"+range+" 블럭"));
    lore.add(Text.color("&7다음 단계: &a"+(range+per)+" 블럭"));
    lore.add(Text.color("&7요구 레벨: &bLv."+reqLv));
    lore.add(Text.color("&7필요 금액: &a"+String.format("%,.0f", cost)));
    lore.add(Text.color("&7업그레이드 레벨: &d"+step));
    lore.add(Text.color("&7업그레이드 레벨: &d"+step));
    lore.add(Text.color("&8클릭: 업그레이드"));
    im.setLore(lore); it.setItemMeta(im);
    return it;
  }

  public void onClick(Player p, InventoryClickEvent e){
    Inventory inv = e.getInventory();
    if (e.getView()==null || e.getView().getTitle()==null) return;
    if (!e.getView().getTitle().contains(TITLE)) return;
    e.setCancelled(true);
    ItemStack it = e.getCurrentItem(); if (it==null) return;
    DataStore ds = store;
    IslandData is = ds.findByMember(p.getUniqueId()).orElseGet(() -> ds.getOrCreate(p.getUniqueId()));

    String name = it.hasItemMeta()? org.bukkit.ChatColor.stripColor(it.getItemMeta().getDisplayName()) : "";
    if (name.contains("인원")){
      int step = is.getTeamLevel();
      int base = plugin.getConfig().getInt("upgrade.team.base-members", 4);
      int per = plugin.getConfig().getInt("upgrade.team.per-level", 1);
      double cost = plugin.getConfig().getDouble("upgrade.team.cost-base", 80000.0) * Math.pow(plugin.getConfig().getDouble("upgrade.team.cost-multiplier",1.35), step);
      int reqLv = plugin.getConfig().getInt("upgrade.team.required-level-base", 5) + plugin.getConfig().getInt("upgrade.team.required-level-step",2)* step;
      if (is.getLevel() < reqLv){ p.sendMessage(Text.color("&c요구 레벨 Lv."+reqLv+" 미만입니다.")); return; }
      if (!Eco.withdraw(p, cost)){ p.sendMessage(Text.color("&c잔액 부족! 필요 금액: &f"+String.format("%,.0f",cost))); return; }
      is.setTeamLevel(step+1); ds.saveAsync();
      int newLimit = base + per*(step+1);
      // BentoBox 연동
      if (com.signition.samskybridge.integration.BentoBridge.available()){
        BentoBridge.setMemberLimit(p, newLimit);
      }
      p.sendMessage(Text.color("&b[섬] 인원 업그레이드 완료: &f"+newLimit+"명"));
      Hud.upgraded(plugin, is, "인원", base + per*step, newLimit, step+1);
      // refresh items
      open(p);
    } else if (name.contains("크기")){
      int step = is.getSizeLevel();
      int base = plugin.getConfig().getInt("upgrade.size.base-range", 50);
      int per = plugin.getConfig().getInt("upgrade.size.per-level", 10);
      double cost = plugin.getConfig().getDouble("upgrade.size.cost-base", 50000.0) * Math.pow(plugin.getConfig().getDouble("upgrade.size.cost-multiplier",1.25), step);
      int reqLv = plugin.getConfig().getInt("upgrade.size.required-level-base", 3) + plugin.getConfig().getInt("upgrade.size.required-level-step",2)* step;
      if (is.getLevel() < reqLv){ p.sendMessage(Text.color("&c요구 레벨 Lv."+reqLv+" 미만입니다.")); return; }
      if (!Eco.withdraw(p, cost)){ p.sendMessage(Text.color("&c잔액 부족! 필요 금액: &f"+String.format("%,.0f",cost))); return; }
      is.setSizeLevel(step+1); ds.saveAsync();
      int newRange = base + per*(step+1);
      if (com.signition.samskybridge.integration.BentoBridge.available()){
        BentoBridge.setProtectionRange(p, newRange);
      }
      p.sendMessage(Text.color("&e[섬] 크기 업그레이드 완료: &f"+newRange+" 블럭"));
      Hud.upgraded(plugin, is, "크기", base + per*step, newRange, step+1);
      open(p);
    }
  }
}
