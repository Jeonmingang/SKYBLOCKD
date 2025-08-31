package com.minkang.usp2.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TradeManager implements Listener {

    private final Map<UUID, TradeSession> sessions = new HashMap<UUID, TradeSession>();

    public void open(Player a, Player b){
        if (a==null || b==null || !a.isOnline() || !b.isOnline()) return;
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.AQUA + "거래: " + a.getName() + " ↔ " + b.getName());
        TradeSession s = new TradeSession(a, b, inv);
        sessions.put(a.getUniqueId(), s);
        sessions.put(b.getUniqueId(), s);
        a.openInventory(inv);
        b.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        TradeSession s = sessions.get(p.getUniqueId());
        if (s == null) return;
        if (e.getClickedInventory() == null) return;

        // Only handle clicks in our trade inventory
        if (!ChatColor.stripColor(e.getView().getTitle()).startsWith("거래:")){
            return;
        }

        e.setCancelled(true); // we control all movement
        s.handleClick(p, e);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e){
        if (!(e.getPlayer() instanceof Player)) return;
        Player p = (Player)e.getPlayer();
        TradeSession s = sessions.get(p.getUniqueId());
        if (s==null) return;
        s.cancel("상대가 거래 창을 닫았습니다.");
    }

    class TradeSession {
        final Player a, b;
        final Inventory inv;

        // define simple 3x7 grids for each player (left/right)
        final int[] aSlots = new int[]{10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34};
        final int[] bSlots = new int[]{37,38,39,40,41,42,43, 46,47,48,49,50,51,52};

        TradeSession(Player a, Player b, Inventory inv){
            this.a = a; this.b = b; this.inv = inv;
            // draw separators
            for (int i : new int[]{0,1,2,3,4,5,6,7,8,  9,17, 18,26, 27,35, 36,44, 45,46,47,48,49,50,51,52,53}){
                if (i>=0 && i<inv.getSize()){
                    ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                    ItemMeta m = pane.getItemMeta();
                    if (m!=null){ m.setDisplayName(" "); pane.setItemMeta(m); }
                    inv.setItem(i, pane);
                }
            }
            // accept buttons
            inv.setItem(0, button(Material.LIME_DYE, ChatColor.GREEN+"수락"));
            inv.setItem(8, button(Material.LIME_DYE, ChatColor.GREEN+"수락"));
            inv.setItem(45, button(Material.BARRIER, ChatColor.RED+"취소"));
            inv.setItem(53, button(Material.BARRIER, ChatColor.RED+"취소"));
        }

        private ItemStack button(Material mat, String name){
            ItemStack it = new ItemStack(mat);
            ItemMeta im = it.getItemMeta();
            if (im != null){ im.setDisplayName(name); it.setItemMeta(im); }
            return it;
        }

        boolean isASlot(int idx){
            for (int s : aSlots) if (s==idx) return true;
            return false;
        }
        boolean isBSlot(int idx){
            for (int s : bSlots) if (s==idx) return true;
            return false;
        }

        void handleClick(Player p, InventoryClickEvent e){
            int slot = e.getRawSlot();
            if (slot<0) return;

            // cancel/accept buttons
            if (slot==45 || slot==53){
                cancel("취소되었습니다.");
                return;
            }
            if (slot==0 || slot==8){
                // rudimentary accept sound
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                p.sendMessage(ChatColor.GREEN + "거래 대기중...");
                return;
            }

            // Only allow players to place/remove items from their own side
            if (p.equals(a)){
                if (!(isASlot(slot))){
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    return;
                }
                e.setCancelled(false); // allow normal move on own side
            }else if (p.equals(b)){
                if (!(isBSlot(slot))){
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    return;
                }
                e.setCancelled(false);
            }
        }

        void cancel(String reason){
            // Return items from both sides
            giveBack(a, aSlots);
            giveBack(b, bSlots);
            a.sendMessage(ChatColor.RED + "거래 취소: " + ChatColor.GRAY + reason);
            b.sendMessage(ChatColor.RED + "거래 취소: " + ChatColor.GRAY + reason);
            forceClose();
        }

        void giveBack(Player pl, int[] slots){
            for (int s : slots){
                ItemStack it = inv.getItem(s);
                if (it != null && it.getType() != Material.AIR){
                    java.util.Map<Integer, ItemStack> left = pl.getInventory().addItem(it);
                    for (ItemStack rem : left.values()){
                        pl.getWorld().dropItemNaturally(pl.getLocation(), rem);
                    }
                    inv.setItem(s, null);
                }
            }
        }

        void forceClose(){
            sessions.remove(a.getUniqueId());
            sessions.remove(b.getUniqueId());
            try { a.closeInventory(); } catch (Exception ignored) {}
            try { b.closeInventory(); } catch (Exception ignored) {}
        }
    }
}
