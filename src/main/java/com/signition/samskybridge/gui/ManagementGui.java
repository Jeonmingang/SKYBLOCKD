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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ManagementGui {
    private final Main plugin;
    private final DataStore store;
    public ManagementGui(Main plugin, DataStore store){ this.plugin=plugin; this.store=store; }

    public void open(Player p){
        java.util.Optional<com.signition.samskybridge.data.IslandData> opt = store.findByMember(p.getUniqueId());
        if (!opt.isPresent()){ p.sendMessage(com.signition.samskybridge.util.Text.color("&c섬 정보를 찾을 수 없습니다.")); return; }
        com.signition.samskybridge.data.IslandData is = opt.get();
        if (is == null){
            p.sendMessage(Text.color("&c섬 정보를 찾을 수 없습니다."));
            return;
        }
        Inventory inv = Bukkit.createInventory(p, 54, Text.color("&b섬 관리"));

        inv.addItem(head(is.getOwner(), plugin.getConfig().getString("messages.role.owner","섬장")));
        for (UUID u : is.getCoOwners()) inv.addItem(head(u, plugin.getConfig().getString("messages.role.coowner","부섬장")));
        for (UUID u : is.getMembers())  inv.addItem(head(u, plugin.getConfig().getString("messages.role.member","섬원")));

        p.openInventory(inv);
    }

    private ItemStack head(UUID u, String role){
        ItemStack it = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = it.getItemMeta();
        if (meta instanceof SkullMeta){
            SkullMeta sm = (SkullMeta) meta;
            OfflinePlayer op = Bukkit.getOfflinePlayer(u);
            sm.setOwningPlayer(op);
            String name = op.getName()==null? u.toString(): op.getName();
            sm.setDisplayName(Text.color("&f"+name+" &7("+role+")"));
            List<String> lore = new ArrayList<String>();
            lore.add(Text.color("&7좌클릭: 관리"));
            sm.setLore(lore);
            it.setItemMeta(sm);
        }
        return it;
    }
}
