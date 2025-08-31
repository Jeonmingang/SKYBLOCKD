
package com.signition.samskybridge.gui;
import com.signition.samskybridge.util.Text; import org.bukkit.Bukkit; import org.bukkit.Material; import org.bukkit.entity.Player; import org.bukkit.inventory.*; import org.bukkit.inventory.meta.ItemMeta;
public class ActionGui {
  public static void open(Player p, java.util.UUID target, String role){ Inventory inv=Bukkit.createInventory(p,27, Text.color("&a관리: "+target.toString())); inv.setItem(11, item(Material.EMERALD,"&a승급", java.util.Arrays.asList(Text.color("&7섬원을 부섬장으로")))); inv.setItem(13, item(Material.COAL,"&e강등", java.util.Arrays.asList(Text.color("&7부섬장을 섬원으로")))); inv.setItem(15, item(Material.BARRIER,"&c추방", java.util.Arrays.asList(Text.color("&7섬에서 제거")))); p.openInventory(inv); }
  private static ItemStack item(Material m, String name, java.util.List<String> lore){ ItemStack it=new ItemStack(m); ItemMeta im=it.getItemMeta(); if (im!=null){ im.setDisplayName(Text.color(name)); im.setLore(lore); it.setItemMeta(im);} return it; }
}
