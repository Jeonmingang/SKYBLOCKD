
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

public class ManagementGui {
  private final Main plugin; private final DataStore store;
  public ManagementGui(Main plugin, DataStore store){ this.plugin=plugin; this.store=store; }
  private static final String TITLE = "섬 관리";

  public void open(Player p){
    Inventory inv = Bukkit.createInventory(p, 27, Text.color("&a"+TITLE));
    inv.setItem(10, make(Material.PAPER, "&b멤버 초대", "&7/섬 초대 <닉네임>"));
    inv.setItem(12, make(Material.BOOK, "&d부섬장 위임", "&7/섬 부섬장 <닉네임>"));
    inv.setItem(14, make(Material.BARRIER, "&c추방", "&7/섬 추방 <닉네임>"));
    inv.setItem(16, make(Material.CHEST, "&6섬 보관함", "&7(준비중)"));
    p.openInventory(inv);
  }

  private ItemStack make(Material m, String name, String... lore){
    ItemStack it = new ItemStack(m);
    ItemMeta im = it.getItemMeta();
    im.setDisplayName(Text.color(name));
    java.util.List<String> ls = new java.util.ArrayList<>();
    for (String s : lore) ls.add(Text.color(s));
    im.setLore(ls);
    it.setItemMeta(im);
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
