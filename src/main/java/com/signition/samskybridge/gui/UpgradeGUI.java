package com.signition.samskybridge.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class UpgradeGUI {

    private final JavaPlugin plugin;
    private final Logger log;

    public UpgradeGUI(JavaPlugin plugin){
        this.plugin = plugin;
        this.log = plugin.getLogger();
    }

    private String color(String s){
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }

    public Inventory build(Player viewer){
        String title = plugin.getConfig().getString("upgrade.gui.title", "&f섬 업그레이드");
        Inventory inv = Bukkit.createInventory(viewer, 27, color(title));

        // Buttons: size(12), members(14), level(22) by default
        placeButton(inv, viewer, "size", 12, "GRASS_BLOCK",
                "&a섬 크기 업그레이드",
                new String[]{"&7현재: {size}", "&e가격: {cost}"},
                "섬 업그레이드 크기");

        placeButton(inv, viewer, "members", 14, "PLAYER_HEAD",
                "&b섬 인원 업그레이드",
                new String[]{"&7현재: {members}", "&e가격: {cost}"},
                "섬 업그레이드 인원");

        placeButton(inv, viewer, "level", 22, "EXPERIENCE_BOTTLE",
                "&d섬 레벨 업그레이드",
                new String[]{"&7현재: {level}", "&e가격: {cost}"},
                "섬 업그레이드 레벨");

        return inv;
    }

    private void placeButton(Inventory inv, Player viewer, String key, int defSlot, String defMat,
                             String defName, String[] defLore, String defCommand){
        String path = "upgrade.gui.buttons." + key + ".";
        int slot = plugin.getConfig().getInt(path + "slot", defSlot);
        String matName = plugin.getConfig().getString(path + "material", defMat);
        String name = plugin.getConfig().getString(path + "name", defName);
        List<String> loreList = plugin.getConfig().getStringList(path + "lore");
        String command = plugin.getConfig().getString(path + "command", defCommand);

        if (loreList == null || loreList.isEmpty()){
            loreList = new ArrayList<>();
            for (String s : defLore) loreList.add(s);
        }

        Material mat = parseMaterial(matName, defMat, key);
        ItemStack item;

        if (mat == Material.PLAYER_HEAD){
            item = new ItemStack(mat, 1);
            try {
                SkullMeta skull = (SkullMeta) item.getItemMeta();
                if (skull != null){
                    // show viewer's head by default
                    OfflinePlayer op = viewer;
                    skull.setOwningPlayer(op);
                    skull.setDisplayName(color(name));
                    List<String> coloredLore = new ArrayList<>();
                    for (String s : loreList) coloredLore.add(color(s));
                    skull.setLore(coloredLore);
                    skull.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
                    item.setItemMeta(skull);
                }
            } catch (Throwable t){
                log.warning("[UpgradeGUI] Failed to apply SkullMeta: " + t.getMessage());
            }
        } else {
            item = new ItemStack(mat, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta != null){
                meta.setDisplayName(color(name));
                List<String> coloredLore = new ArrayList<>();
                for (String s : loreList) coloredLore.add(color(s));
                meta.setLore(coloredLore);
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
                item.setItemMeta(meta);
            }
        }

        if (slot >= 0 && slot < inv.getSize()){
            inv.setItem(slot, item);
        } else {
            log.warning("[UpgradeGUI] Invalid slot for '" + key + "': " + slot + " (inv size=" + inv.getSize() + ")");
        }

        // store command meta to PersistentDataContainer? Simpler: we rely on config mapping (listener resolves by slot)
        plugin.getConfig().set("upgrade.gui._slotmap."+slot, key);
        plugin.getConfig().set("upgrade.gui._commands."+key, command);
    }

    private Material parseMaterial(String name, String def, String key){
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (Exception ex){
            try {
                Material fallback = Material.valueOf(def.toUpperCase());
                log.warning("[UpgradeGUI] Unknown material '"+name+"' for key '"+key+"', using "+def);
                return fallback;
            } catch (Exception ex2){
                log.warning("[UpgradeGUI] Unknown material '"+name+"' and invalid default '"+def+"', using BARRIER");
                return Material.BARRIER;
            }
        }
    }
}