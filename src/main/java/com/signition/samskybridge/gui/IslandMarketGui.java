
package com.signition.samskybridge.gui;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.DataStore.IslandSale;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Text;
import com.signition.samskybridge.util.Eco;
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
import com.signition.samskybridge.data.DataStore.IslandSale;

public class IslandMarketGui {
  private final Main plugin; private final DataStore store;
  public IslandMarketGui(Main plugin, DataStore store){ this.plugin=plugin; this.store=store; }
  private static final String TITLE = "섬 매물";
  public void open(Player p, int page){
    int pageSizeize = plugin.getConfig().getInt("market.page-size", 28);
    List<IslandSale> list = new ArrayList<>(store.getMarket());
    list.sort(Comparator.comparingLong(s -> -s.listedAt));
    int from = Math.max(0, page * pageSizeize);
    int to = Math.min(list.size(), from + pageSizeize);
    Inventory inv = Bukkit.createInventory(p, 54, Text.color("&a"+TITLE+" &7- "+(page+1)+"페이지"));
    // items
    for (int i=from;i<to;i++){
      IslandSale s = list.get(i);
      ItemStack head = new ItemStack(Material.PLAYER_HEAD);
      SkullMeta sm = (SkullMeta)head.getItemMeta();
      OfflinePlayer op = Bukkit.getOfflinePlayer(s.owner);
      sm.setOwningPlayer(op);
      sm.setDisplayName(Text.color("&f섬 판매: &e"+(op.getName()==null? s.owner.toString(): op.getName())));
      java.util.List<String> lore = new java.util.ArrayList<>();
      lore.add(Text.color("&7가격: &a"+String.format("%,.0f", s.price)));
      lore.add(Text.color("&8클릭: 구매 확인"));
      sm.setLore(lore);
      head.setItemMeta(sm);
      inv.addItem(head);
    }
    // nav buttons
    if (to < list.size()){
      ItemStack next = new ItemStack(Material.ARROW);
      ItemMeta im = next.getItemMeta(); im.setDisplayName(Text.color("&a다음 페이지")); next.setItemMeta(im);
      inv.setItem(5, next); // 6번째 칸
    }
    if (page > 0){
      ItemStack prev = new ItemStack(Material.ARROW);
      ItemMeta im = prev.getItemMeta(); im.setDisplayName(Text.color("&c이전 페이지")); prev.setItemMeta(im);
      inv.setItem(1, prev); // 2번째 칸
    }
    p.openInventory(inv);
    pages.put(p.getUniqueId(), page);
  }

  private final Map<UUID,Integer> pages = new HashMap<>();

  public void onClick(Player p, InventoryClickEvent e){
    Inventory inv = e.getInventory();
    if (e.getView()==null || e.getView().getTitle()==null) return;
    if (!e.getView().getTitle().contains(TITLE)) return;
    e.setCancelled(true);
    ItemStack it = e.getCurrentItem(); if (it==null) return;
    int page = pages.getOrDefault(p.getUniqueId(), 0);
    if (it.getType()==Material.ARROW){
      String name = it.hasItemMeta()? it.getItemMeta().getDisplayName() : "";
      if (name.contains("다음")) open(p, page+1); else open(p, Math.max(0, page-1));
      return;
    }
    if (it.getType()==Material.PLAYER_HEAD){
      // confirm & buy
      if (!plugin.getConfig().getBoolean("market.confirm", true)){
        buy(p, it); return;
      } else {
        Inventory conf = Bukkit.createInventory(p, 27, Text.color("&c정말 구매할까요?"));
        ItemStack yes = new ItemStack(Material.LIME_WOOL); ItemMeta yim = yes.getItemMeta(); yim.setDisplayName(Text.color("&a구매")); yes.setItemMeta(yim);
        ItemStack no = new ItemStack(Material.RED_WOOL); ItemMeta nim = no.getItemMeta(); nim.setDisplayName(Text.color("&c취소")); no.setItemMeta(nim);
        conf.setItem(11, yes); conf.setItem(15, no); conf.setItem(13, it);
        p.openInventory(conf);
        confirmTarget.put(p.getUniqueId(), it);
      }
    }
  }

  private final Map<UUID, ItemStack> confirmTarget = new HashMap<>();

  public void onConfirm(Player p, InventoryClickEvent e){
    Inventory inv = e.getInventory();
    if (e.getView()==null || e.getView().getTitle()==null) return;
    if (!e.getView().getTitle().contains("정말 구매")) return;
    e.setCancelled(true);
    ItemStack it = e.getCurrentItem(); if (it==null) return;
    if (it.getType()==Material.LIME_WOOL){
      ItemStack target = confirmTarget.remove(p.getUniqueId());
      if (target!=null) buy(p, target);
      p.closeInventory();
    } else if (it.getType()==Material.RED_WOOL){
      confirmTarget.remove(p.getUniqueId()); p.closeInventory();
    }
  }

  private void buy(Player p, ItemStack head){
    ItemMeta im = head.getItemMeta();
    java.util.List<String> lore = im.hasLore()? im.getLore(): java.util.Collections.emptyList();
    String priceLine = lore.isEmpty()? "" : org.bukkit.ChatColor.stripColor(lore.get(0));
    double price = 0.0;
    try{ price = Double.parseDouble(priceLine.replace("가격:", "").replace(",", "").trim()); }catch(Exception ignore){}
    // find sale by matching owner skin
    org.bukkit.OfflinePlayer op = null;
    if (im instanceof SkullMeta){
      org.bukkit.OfflinePlayer t = ((SkullMeta)im).getOwningPlayer();
      if (t != null) op = t;
    }
    if (op == null){ p.sendMessage(Text.color("&c소유자 정보를 찾을 수 없습니다.")); return; }
    java.util.Optional<IslandData> buyerIsland = store.findByMember(p.getUniqueId());
    if (!buyerIsland.isPresent()){ p.sendMessage(Text.color("&c구매자에게 섬이 없습니다.")); return; }
    if (!Eco.withdraw(p, price)){ p.sendMessage(Text.color("&c잔액 부족! 필요 금액: &f"+String.format("%,.0f", price))); return; }
    // transfer ownership
    com.signition.samskybridge.data.DataStore.IslandSale targetSale = null;
    for (IslandSale s : store.getMarket()){ if (s.owner.equals(op.getUniqueId())){ targetSale = s; break; }}
    if (targetSale == null){ p.sendMessage(Text.color("&c이미 판매가 완료되었거나 찾을 수 없습니다.")); return; }
    IslandData is = buyerIsland.get();
    // Replace island owner: we transfer the listed owner's island to buyer
    java.util.Optional<IslandData> sellerIsland = java.util.Optional.ofNullable(store.getOrCreate(op.getUniqueId()));
    IslandData src = store.getOrCreate(op.getUniqueId());
    // carry over level/xp/upgrades
    store.getAllOwners().remove(is.getOwner());
    is.setOwner(p.getUniqueId());
    is.setLevel(src.getLevel()); is.setXp(src.getXp());
    is.setSizeLevel(src.getSizeLevel()); is.setTeamLevel(src.getTeamLevel());
    // remove sale
    store.removeSale(targetSale);
    p.sendMessage(Text.color("&a구매 완료! &7섬이 이전되었습니다."));
  }
}
