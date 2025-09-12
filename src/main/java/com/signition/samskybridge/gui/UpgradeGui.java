
package com.signition.samskybridge.gui;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.integration.BentoBridge;
import com.signition.samskybridge.upgrade.CustomUpgradeStore;
import com.signition.samskybridge.util.Eco;
import com.signition.samskybridge.util.Text;
import com.signition.samskybridge.util.Hud;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Upgrade GUI with config-driven titles/lores and extensible custom upgrades (command-based).
 */
public class UpgradeGui {
  private final Main plugin;
  private final DataStore store;
  private final CustomUpgradeStore customStore;

  public UpgradeGui(Main plugin, DataStore store){
    this.plugin = plugin;
    this.store = store;
    this.customStore = new CustomUpgradeStore(plugin);
  }

  private static final String TITLE = "섬 업그레이드";
  private int teamSlot(){ return plugin.getConfig().getInt("upgrade.gui.slots.team", 12); }
  private int sizeSlot(){ return plugin.getConfig().getInt("upgrade.gui.slots.size", 14); }

  public void open(Player p){
    Inventory inv = Bukkit.createInventory(p, 54, Text.color("&a"+TITLE));
    Optional<IslandData> opt = store.findByMember(p.getUniqueId());
    if (!opt.isPresent()){
      p.sendMessage(Text.color("&c섬 정보를 찾을 수 없습니다."));
      p.closeInventory();
      return;
    }
    IslandData is = opt.get();

    // filler
    ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    ItemMeta fm = filler.getItemMeta(); fm.setDisplayName(" "); filler.setItemMeta(fm);
    for (int i=0;i<54;i++){ inv.setItem(i, filler); }

    // owner head at slot 4
    ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
    SkullMeta sm = (SkullMeta) head.getItemMeta();
    OfflinePlayer op = Bukkit.getOfflinePlayer(is.getOwner());
    sm.setOwningPlayer(op);
    sm.setDisplayName(Text.color("&f섬 정보"));
    List<String> inf = new ArrayList<String>();
    inf.add(Text.color("&7이름: &f"+(is.getName()==null? "이름없음":is.getName())));
    inf.add(Text.color("&7레벨: &bLv."+is.getLevel()+" &7(경험치: &f"+is.getXp()+"&7)"));
    sm.setLore(inf);
    head.setItemMeta(sm);
    inv.setItem(4, head);

    // Team limit upgrade
    int tSlot = teamSlot();
    inv.setItem(tSlot, makeTeamItem(is));

    // Size upgrade
    int sSlot = sizeSlot();
    inv.setItem(sSlot, makeSizeItem(is));

    // Custom upgrades
    renderCustomUpgrades(inv, is);

    p.openInventory(inv);
  }

  private ItemStack makeTeamItem(IslandData is){
    int step = is.getTeamLevel();
    int base = plugin.getConfig().getInt("upgrade.team.base-members", 4);
    int per = plugin.getConfig().getInt("upgrade.team.per-level", 1);
    int current = base + per*step;
    int next = base + per*(step+1);
    double cost = plugin.getConfig().getDouble("upgrade.team.cost-base", 15000.0)
            * Math.pow(plugin.getConfig().getDouble("upgrade.team.cost-multiplier", 1.35), step);
    int reqLv = plugin.getConfig().getInt("upgrade.team.required-level-base", 2)
            + plugin.getConfig().getInt("upgrade.team.required-level-step", 2) * step;

    ItemStack it = new ItemStack(Material.PLAYER_HEAD);
    ItemMeta im = it.getItemMeta();
    im.setDisplayName(Text.color(plugin.getConfig().getString("upgrade.team.title", "&b섬 인원 업그레이드")));
    List<String> tmpl = plugin.getConfig().getStringList("upgrade.team.lore");
    if (tmpl == null || tmpl.isEmpty()){
      tmpl = Arrays.asList("&7현재 한도: &f{current}명","&7다음 단계: &a{next}명","&7요구 레벨: &bLv.{reqLevel}","&7필요 금액: &a{cost}","&7업그레이드 레벨: &d{step}","&8클릭: 업그레이드");
    }
    List<String> lore = new ArrayList<String>();
    for (String s0 : tmpl){
      String s = s0.replace("{current}", String.valueOf(current))
                   .replace("{next}", String.valueOf(next))
                   .replace("{reqLevel}", String.valueOf(reqLv))
                   .replace("{cost}", String.format("%,.0f",cost))
                   .replace("{step}", String.valueOf(step));
      lore.add(Text.color(s));
    }
    im.setLore(lore);
    it.setItemMeta(im);
    return it;
  }

  private ItemStack makeSizeItem(IslandData is){
    int step = is.getSizeLevel();
    int base = plugin.getConfig().getInt("upgrade.size.base-range", 50);
    int per = plugin.getConfig().getInt("upgrade.size.per-level", 10);
    int current = base + per*step;
    int next = base + per*(step+1);
    double cost = plugin.getConfig().getDouble("upgrade.size.cost-base", 20000.0)
            * Math.pow(plugin.getConfig().getDouble("upgrade.size.cost-multiplier", 1.25), step);
    int reqLv = plugin.getConfig().getInt("upgrade.size.required-level-base", 2)
            + plugin.getConfig().getInt("upgrade.size.required-level-step", 2) * step;

    ItemStack it = new ItemStack(Material.MAP);
    ItemMeta im = it.getItemMeta();
    im.setDisplayName(Text.color(plugin.getConfig().getString("upgrade.size.title", "&e섬 크기 업그레이드")));
    List<String> tmpl = plugin.getConfig().getStringList("upgrade.size.lore");
    if (tmpl == null || tmpl.isEmpty()){
      tmpl = Arrays.asList("&7현재 보호반경: &f{range} 블럭","&7다음 단계: &a{next} 블럭","&7요구 레벨: &bLv.{reqLevel}","&7필요 금액: &a{cost}","&7업그레이드 레벨: &d{step}","&8클릭: 업그레이드");
    }
    List<String> lore = new ArrayList<String>();
    for (String s0 : tmpl){
      String s = s0.replace("{range}", String.valueOf(current))
                   .replace("{next}", String.valueOf(next))
                   .replace("{reqLevel}", String.valueOf(reqLv))
                   .replace("{cost}", String.format("%,.0f",cost))
                   .replace("{step}", String.valueOf(step));
      lore.add(Text.color(s));
    }
    im.setLore(lore);
    it.setItemMeta(im);
    return it;
  }

  private void renderCustomUpgrades(Inventory inv, IslandData is){
    if (!plugin.getConfig().isConfigurationSection("upgrade.customs")) return;
    ConfigurationSection sec = plugin.getConfig().getConfigurationSection("upgrade.customs");
    for (String id : sec.getKeys(false)){
      ConfigurationSection u = sec.getConfigurationSection(id);
      int slot = u.getInt("slot", 22);
      String icon = u.getString("icon", "BOOK");
      Material mat;
      try{ mat = Material.valueOf(icon.toUpperCase()); } catch (Throwable t){ mat = Material.BOOK; }
      ItemStack it = new ItemStack(mat);
      ItemMeta meta = it.getItemMeta();
      meta.setDisplayName(Text.color(u.getString("title", "&b커스텀 업그레이드")));
      int step = customStore.getStep(is.getId(), id);
      int base = u.getInt("base", 0);
      int per  = u.getInt("per", 1);
      int current = base + per*step;
      int next = base + per*(step+1);
      int reqLv = u.getInt("required-level-base", 1) + u.getInt("required-level-step", 1) * step;
      double cost = u.getDouble("cost-base", 1000.0) * Math.pow(u.getDouble("cost-multiplier", 1.15), step);
      List<String> loreT = u.getStringList("lore");
      if (loreT == null || loreT.isEmpty()){
        loreT = Arrays.asList("&7현재: &f{current}", "&7다음: &a{next}", "&7요구 레벨: &bLv.{reqLevel}", "&7필요 금액: &a{cost}", "&7업그레이드 레벨: &d{step}", "&8클릭: 업그레이드");
      }
      List<String> lore = new ArrayList<String>();
      for (String s0 : loreT){
        String s = s0.replace("{current}", String.valueOf(current))
                .replace("{next}", String.valueOf(next))
                .replace("{reqLevel}", String.valueOf(reqLv))
                .replace("{cost}", String.format("%,.0f",cost))
                .replace("{step}", String.valueOf(step));
        lore.add(Text.color(s));
      }
      meta.setLore(lore);
      it.setItemMeta(meta);
      inv.setItem(slot, it);
    }
  }

  public void onClick(Player p, InventoryClickEvent e){
    if (e.getView()==null || e.getView().getTitle()==null) return;
    String title = ChatColor.stripColor(e.getView().getTitle());
    if (!title.contains("섬 업그레이드")) return;
    e.setCancelled(true);

    Optional<IslandData> opt = store.findByMember(p.getUniqueId());
    if (!opt.isPresent()){ p.closeInventory(); return; }
    IslandData is = opt.get();

    // Team click
    if (e.getRawSlot() == teamSlot()){
      int step = is.getTeamLevel();
      int base = plugin.getConfig().getInt("upgrade.team.base-members", 4);
      int per  = plugin.getConfig().getInt("upgrade.team.per-level", 1);
      int limit = base + per*step;
      int reqLv = plugin.getConfig().getInt("upgrade.team.required-level-base",2)
              + plugin.getConfig().getInt("upgrade.team.required-level-step",2)* step;
      double cost = plugin.getConfig().getDouble("upgrade.team.cost-base", 15000.0)
              * Math.pow(plugin.getConfig().getDouble("upgrade.team.cost-multiplier",1.35), step);
      if (is.getLevel() < reqLv){ p.sendMessage(Text.color("&c요구 레벨 Lv."+reqLv+" 미만입니다.")); return; }
      if (!Eco.withdraw(p, cost)){ p.sendMessage(Text.color("&c잔액 부족! 필요 금액: &f"+String.format("%,.0f",cost))); return; }
      is.setTeamLevel(step+1); store.saveAsync();
      int newLimit = base + per*(step+1);
      p.sendMessage(Text.color("&e[섬] 인원 업그레이드 완료: &f"+newLimit+"명"));
      Hud.upgraded(plugin, is, "인원", limit, newLimit, step+1);
      open(p);
      return;
    }

    // Size click
    if (e.getRawSlot() == sizeSlot()){
      int step = is.getSizeLevel();
      int base = plugin.getConfig().getInt("upgrade.size.base-range", 50);
      int per  = plugin.getConfig().getInt("upgrade.size.per-level", 10);
      int range = base + per*step;
      int reqLv = plugin.getConfig().getInt("upgrade.size.required-level-base",2)
              + plugin.getConfig().getInt("upgrade.size.required-level-step",2)* step;
      double cost = plugin.getConfig().getDouble("upgrade.size.cost-base", 20000.0)
              * Math.pow(plugin.getConfig().getDouble("upgrade.size.cost-multiplier",1.25), step);
      if (is.getLevel() < reqLv){ p.sendMessage(Text.color("&c요구 레벨 Lv."+reqLv+" 미만입니다.")); return; }
      if (!Eco.withdraw(p, cost)){ p.sendMessage(Text.color("&c잔액 부족! 필요 금액: &f"+String.format("%,.0f",cost))); return; }
      is.setSizeLevel(step+1); store.saveAsync();
      int newRange = base + per*(step+1);
      if (BentoBridge.available()){
        BentoBridge.setProtectionRange(p, newRange);
      }
      p.sendMessage(Text.color("&e[섬] 크기 업그레이드 완료: &f"+newRange+" 블럭"));
      Hud.upgraded(plugin, is, "크기", range, newRange, step+1);
      open(p);
      return;
    }

    // Custom upgrades click detection
    if (plugin.getConfig().isConfigurationSection("upgrade.customs")){
      ConfigurationSection sec = plugin.getConfig().getConfigurationSection("upgrade.customs");
      for (String id : sec.getKeys(false)){
        ConfigurationSection u = sec.getConfigurationSection(id);
        int slot = u.getInt("slot", 22);
        if (e.getRawSlot() == slot){
          int step = customStore.getStep(is.getId(), id);
          int reqLv = u.getInt("required-level-base",1) + u.getInt("required-level-step",1)*step;
          double cost = u.getDouble("cost-base",1000.0) * Math.pow(u.getDouble("cost-multiplier",1.15), step);
          if (is.getLevel() < reqLv){ p.sendMessage(Text.color("&c요구 레벨 Lv."+reqLv+" 미만입니다.")); return; }
          if (!Eco.withdraw(p, cost)){ p.sendMessage(Text.color("&c잔액 부족! 필요 금액: &f"+String.format("%,.0f",cost))); return; }

          // Run console commands
          List<String> cmds = u.getStringList("on-success.commands");
          for (String cmd : cmds){
            String c = cmd.replace("{player}", p.getName())
                    .replace("{uuid}", p.getUniqueId().toString())
                    .replace("{island}", is.getName()==null?is.getId().toString():is.getName())
                    .replace("{step}", String.valueOf(step+1));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c);
          }

          customStore.incStep(is.getId(), id);
          p.sendMessage(Text.color("&e[섬] "+u.getString("title","커스텀 업그레이드") + " 완료! &7(레벨 "+(step+1)+")"));
          open(p);
          return;
        }
      }
    }
  }
}
