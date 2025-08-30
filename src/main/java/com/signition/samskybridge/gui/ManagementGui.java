package com.signition.samskybridge.gui;
import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit; import org.bukkit.Material; import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player; import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta; import org.bukkit.inventory.meta.SkullMeta;
import java.util.*; import java.util.stream.Collectors;
public class ManagementGui {
  private final Main plugin; private final DataStore store;
  public ManagementGui(Main plugin, DataStore store){ this.plugin = plugin; this.store = store; }
  public void open(Player p){
    IslandData is = store.findByMember(p.getUniqueId()).orElseGet(()->store.getOrCreate(p.getUniqueId()));
    Inventory inv = Bukkit.createInventory(p, 54, Text.color("&a섬 관리"));
    int idx = 0;
    inv.setItem(idx++, head(is.getOwner(), "&c섬장", java.util.Arrays.asList("&7좌클릭: 관리")));
    for (UUID u : is.getCoOwners()) inv.setItem(idx++, head(u, "&6부섬장", java.util.Arrays.asList("&7좌클릭: 관리")));
    for (UUID u : is.getMembers()) inv.setItem(idx++, head(u, "&f섬원", java.util.Arrays.asList("&7좌클릭: 관리")));
    p.openInventory(inv);
  }
  private ItemStack head(UUID u, String name, List<String> lore){
    ItemStack it = new ItemStack(Material.PLAYER_HEAD); ItemMeta im = it.getItemMeta();
    if (im instanceof SkullMeta sm){
      OfflinePlayer op = Bukkit.getOfflinePlayer(u); sm.setOwningPlayer(op);
      sm.setDisplayName(Text.color(name+" &7- &f"+(op.getName()==null?u.toString():op.getName())));
      sm.setLore(lore.stream().map(Text::color).collect(java.util.stream.Collectors.collect(java.util.stream.Collectors.toList()))); it.setItemMeta(sm);
    }
    return it;
  }
}