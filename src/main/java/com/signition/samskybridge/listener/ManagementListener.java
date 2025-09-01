
package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.gui.ActionGui;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class ManagementListener implements Listener {
    private final Main plugin;
    private final DataStore store;
    public ManagementListener(Main plugin, DataStore store){ this.plugin=plugin; this.store=store; }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        HumanEntity who = e.getWhoClicked();
        Inventory inv = e.getInventory();
        String title = inv.getTitle();
        ItemStack it = e.getCurrentItem();

        // Member list GUI: click head -> open action GUI
        if (ChatColor.stripColor(title).equals("섬 관리")){
            e.setCancelled(true);
            if (!(who instanceof Player)) return;
            if (it == null || it.getType() != Material.PLAYER_HEAD) return;
            if (!(it.getItemMeta() instanceof SkullMeta)) return;
            SkullMeta sm = (SkullMeta) it.getItemMeta();
            OfflinePlayer op = sm.getOwningPlayer();
            if (op == null) return;
            String name = op.getName()==null? op.getUniqueId().toString(): op.getName();
            ActionGui.open((Player)who, op.getUniqueId(), name);
            return;
        }

        // Action GUI: promote/demote/kick
        if (ChatColor.stripColor(title).startsWith("관리:")){
            e.setCancelled(true);
            if (it == null) return;
            UUID target = com.signition.samskybridge.gui.ActionGui.getTarget(it);
            String tname = com.signition.samskybridge.gui.ActionGui.getTargetName(it);
            if (tname == null && target != null){
                OfflinePlayer op = Bukkit.getOfflinePlayer(target);
                tname = op.getName()==null? target.toString(): op.getName();
            }
            if (!(who instanceof Player) || target == null || tname == null) return;
            Player actor = (Player) who;
            UUID au = actor.getUniqueId();

            IslandData is = store.findByMember(au).orElse(null);
            if (is == null){ actor.sendMessage(Text.color("&c섬이 없습니다.")); actor.closeInventory(); return; }

            Material type = it.getType();
            // permissions: only owner can promote/demote co-owners; co-owner can kick members
            boolean isOwner = au.equals(is.getOwner());
            boolean isCo = is.getCoOwners().contains(au);

            if (type == Material.LIME_DYE){
                if (!isOwner){ actor.sendMessage(Text.color("&c섬장만 승급할 수 있습니다.")); actor.closeInventory(); return; }
                // target must be member (not owner/co)
                if (target.equals(is.getOwner()) || is.getCoOwners().contains(target)){
                    actor.sendMessage(Text.color("&c이미 부섬장이거나 섬장입니다.")); actor.closeInventory(); return;
                }
                is.getMembers().remove(target);
                is.getCoOwners().add(target);
                actor.sendMessage(Text.color("&a부섬장으로 승급했습니다: &f"+tname));
                store.saveAsync();
                actor.closeInventory();
                return;
            }
            if (type == Material.ORANGE_DYE){
                if (!isOwner){ actor.sendMessage(Text.color("&c섬장만 강등할 수 있습니다.")); actor.closeInventory(); return; }
                if (!is.getCoOwners().contains(target)){
                    actor.sendMessage(Text.color("&c부섬장이 아닙니다.")); actor.closeInventory(); return;
                }
                is.getCoOwners().remove(target);
                is.getMembers().add(target);
                actor.sendMessage(Text.color("&e섬원으로 강등했습니다: &f"+tname));
                store.saveAsync();
                actor.closeInventory();
                return;
            }
            if (type == Material.BARRIER){
                if (!(isOwner || isCo)){ actor.sendMessage(Text.color("&c추방 권한이 없습니다.")); actor.closeInventory(); return; }
                if (target.equals(is.getOwner())){
                    actor.sendMessage(Text.color("&c섬장은 추방할 수 없습니다.")); actor.closeInventory(); return;
                }
                // co-owner cannot kick another co-owner
                if (isCo && is.getCoOwners().contains(target)){
                    actor.sendMessage(Text.color("&c부섬장은 다른 부섬장을 추방할 수 없습니다.")); actor.closeInventory(); return;
                }
                is.getCoOwners().remove(target);
                is.getMembers().remove(target);
                actor.sendMessage(Text.color("&c추방했습니다: &f"+tname));
                store.saveAsync();
                actor.closeInventory();
                return;
            }
        }
    }
}
