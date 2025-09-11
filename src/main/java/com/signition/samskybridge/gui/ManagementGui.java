package com.signition.samskybridge.gui;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ManagementGui {
  private final Main plugin; private final DataStore store;
  public static final String TITLE = "섬 관리";

  public ManagementGui(Main plugin, DataStore store){ this.plugin = plugin; this.store = store; }

  public void open(Player p){
    Inventory inv = Bukkit.createInventory(null, 9, TITLE);
    inv.setItem(0, item(Material.BOOK, "&a가이드", "&7명령어를 사용해 섬을 관리하세요."));
    inv.setItem(1, item(Material.ANVIL, "&b개편 예정", "&7이 GUI는 추후 업데이트됩니다."));
    p.openInventory(inv);
  }

  private ItemStack item(Material m, String name, String... lore){
    ItemStack it = new ItemStack(m);
    ItemMeta im = it.getItemMeta();
    if (im != null){
      im.setDisplayName(Text.color(name));
      List<String> ls = new ArrayList<>();
      for (String s : lore) ls.add(Text.color(s));
      im.setLore(ls);
      it.setItemMeta(im);
    }
    return it;
  }

  public void onClick(Player p, InventoryClickEvent e){
    if (e.getView()==null || e.getView().getTitle()==null || !e.getView().getTitle().contains(TITLE)) return;
    e.setCancelled(true);
    if (e.getCurrentItem()==null) return;
    p.closeInventory();
    p.sendMessage(Text.color("&7해당 기능은 명령어로 제공합니다. 차후 GUI화 예정."));
  }
}
