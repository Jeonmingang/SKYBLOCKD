package com.signition.samskybridge.gui;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.UUID;

public class ActionGui {
    private static final NamespacedKey TARGET_KEY = new NamespacedKey(Main.get(), "mg_target");
    private static final NamespacedKey TARGET_NAME = new NamespacedKey(Main.get(), "mg_tname");

    public static void open(Player viewer, UUID target, String name){
        if (name == null){
            OfflinePlayer op = Bukkit.getOfflinePlayer(target);
            name = op.getName()==null? target.toString(): op.getName();
        }
        Inventory inv = Bukkit.createInventory(viewer, 27, Text.color("&b관리: "+name));

        ItemStack promote = item(Material.LIME_DYE, "&a승급", new String[]{"&7상위 등급으로 올리기"}, target, name);
        ItemStack demote  = item(Material.ORANGE_DYE, "&6강등", new String[]{"&7하위 등급으로 내리기"}, target, name);
        ItemStack kick    = item(Material.BARRIER, "&c추방", new String[]{"&7섬에서 제거"}, target, name);

        inv.setItem(11, promote);
        inv.setItem(13, demote);
        inv.setItem(15, kick);

        viewer.openInventory(inv);
    }

    private static ItemStack item(Material m, String name, String[] lore, UUID target, String tname){
        ItemStack it = new ItemStack(m);
        ItemMeta im = it.getItemMeta();
        if (im != null){
            im.setDisplayName(Text.color(name));
            im.setLore(java.util.Arrays.asList(Text.color(org.apache.commons.lang.StringUtils.join(lore, "\n")).split("\\n")));
            im.getPersistentDataContainer().set(TARGET_KEY, PersistentDataType.STRING, target.toString());
            im.getPersistentDataContainer().set(TARGET_NAME, PersistentDataType.STRING, tname);
            it.setItemMeta(im);
        }
        return it;
    }

    public static UUID getTarget(ItemStack it){
        if (it==null || !it.hasItemMeta()) return null;
        ItemMeta im = it.getItemMeta();
        String s = im.getPersistentDataContainer().get(TARGET_KEY, PersistentDataType.STRING);
        if (s == null) return null;
        try{ return java.util.UUID.fromString(s); }catch(IllegalArgumentException e){ return null; }
    }
    public static String getTargetName(ItemStack it){
        if (it==null || !it.hasItemMeta()) return null;
        ItemMeta im = it.getItemMeta();
        return im.getPersistentDataContainer().get(TARGET_NAME, PersistentDataType.STRING);
    }
}
