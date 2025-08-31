
package com.signition.samskybridge.gui;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class MarketGui {
    private final Main plugin;
    private final DataStore store;
    public MarketGui(Main plugin, DataStore store){ this.plugin=plugin; this.store=store; }

    public void open(Player p){
        Inventory inv = Bukkit.createInventory(p, 54, Text.color("&b섬 매물"));
        for (IslandData is : store.getAll()){
            if (!is.isForSale()) continue;
            inv.addItem(head(is));
        }
        p.openInventory(inv);
    }

    private ItemStack head(IslandData is){
        ItemStack it = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta base = it.getItemMeta();
        if (base instanceof SkullMeta){
            SkullMeta sm = (SkullMeta) base;
            OfflinePlayer op = Bukkit.getOfflinePlayer(is.getOwner());
            sm.setOwningPlayer(op);
            String ownerName = (op.getName()==null? is.getOwner().toString() : op.getName());
            sm.setDisplayName(Text.color("&b매물: &f"+ ownerName));

            java.util.List<String> lore = new java.util.ArrayList<String>();
            int sizeDelta = plugin.getConfig().getInt("upgrade.size.delta",5);
            int teamDelta = plugin.getConfig().getInt("upgrade.team.delta",1);
            int sizeStep = sizeDelta>0 ? is.getSizeLevel()/sizeDelta : is.getSizeLevel();
            int teamStep = teamDelta>0 ? is.getTeamLevel()/teamDelta : is.getTeamLevel();

            lore.add(Text.color("&7가격: &f"+(long)is.getPrice()));
            lore.add(Text.color("&7업그레이드: &f크기 "+is.getSizeLevel()+" / 팀원 "+is.getTeamLevel()));
            lore.add(Text.color("&7단계: &f크기 Lv."+sizeStep+" / 팀원 Lv."+teamStep));
            lore.add(Text.color("&a좌클릭: 구매 &7/ &f우클릭: 해당 섬으로 이동(구경)"));
            sm.setLore(lore);
            it.setItemMeta(sm);
        }
        return it;
    }
}
