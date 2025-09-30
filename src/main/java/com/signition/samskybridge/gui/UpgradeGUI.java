package com.signition.samskybridge.gui;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.level.LevelService;
import net.milkbowl.vault.economy.Economy;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.database.objects.Island;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class UpgradeGUI implements Listener {
private enum UpgradeType { XP, SIZE, MEMBERS }

private static class Tier {
    int needLevel;    // required island level
    double cost;      // money
    int value;        // range or max members
    Tier(int needLevel, double cost, int value){
        this.needLevel = needLevel; this.cost = cost; this.value = value;
    }
}


    private final Main plugin;
    private final LevelService levelService;
    private final Economy economy;
    private final String title;
    private final List<Package> packages = new ArrayList<>();
    private boolean levelEnabled = true;
    private Material levelMat;
    private String levelName;
    private java.util.List<String> levelLore;
    private boolean levelOpenXpOnClick = false;
    private final List<Tier> sizeTiers = new ArrayList<>();
    private final List<Tier> memberTiers = new ArrayList<>();
    private boolean sizeEnabled = true;
    private boolean membersEnabled = true;
    private String sizeName, membersName;
    private List<String> sizeLore, membersLore;
    private org.bukkit.Material sizeMat, membersMat;

    public UpgradeGUI(Main plugin, LevelService levelService, Economy economy) {
        this.plugin = plugin;
        this.levelService = levelService;
        this.economy = economy;

        FileConfiguration c = plugin.getConfig();
        this.title = c.getString("xp-purchase.gui-title", "섬 업그레이드");
        ConfigurationSection sec = c.getConfigurationSection("xp-purchase.packages");
        if (sec == null && c.isList("xp-purchase.packages")) {
            // support list format
            List<?> list = c.getList("xp-purchase.packages");
            if (list != null) {
                int i = 0;
                for (Object o : list) {
                    if (o instanceof ConfigurationSection) continue;
                    // ignore; list of maps will be read below via manual section creation
                }
            }
        }
// Parse upgrade-menu
// Level tile (status)
if (um != null){
    ConfigurationSection lsec = um.getConfigurationSection("level");
    if (lsec != null){
        levelEnabled = lsec.getBoolean("enabled", true);
        levelMat = Material.matchMaterial(lsec.getString("material","EXPERIENCE_BOTTLE"));
        levelName = lsec.getString("name","&a섬 레벨 현황");
        levelLore = lsec.getStringList("lore");
        levelOpenXpOnClick = lsec.getBoolean("open-xp-on-click", false);
    }
}

ConfigurationSection um = c.getConfigurationSection("upgrade-menu");
if (um != null && um.getBoolean("enabled", true)){
    ConfigurationSection ssec = um.getConfigurationSection("size");
    if (ssec != null){
        sizeEnabled = ssec.getBoolean("enabled", true);
        sizeName = ssec.getString("name", "&a섬 크기 업그레이드");
        sizeMat = org.bukkit.Material.matchMaterial(ssec.getString("material","GRASS_BLOCK"));
        sizeLore = ssec.getStringList("lore");
        if (ssec.isList("tiers")){
            for (Object o : ssec.getList("tiers")){
                if (o instanceof java.util.Map){
                    @SuppressWarnings("unchecked")
                    java.util.Map<String,Object> m = (java.util.Map<String,Object>) o;
                    int need = ((Number)m.getOrDefault("need",1)).intValue();
                    double cost = ((Number)m.getOrDefault("cost",10000)).doubleValue();
                    int range = ((Number)m.getOrDefault("range",120)).intValue();
                    sizeTiers.add(new Tier(need, cost, range));
                }
            }
        }
    }
    ConfigurationSection msec = um.getConfigurationSection("members");
    if (msec != null){
        membersEnabled = msec.getBoolean("enabled", true);
        membersName = msec.getString("name", "&a섬 인원 업그레이드");
        membersMat = org.bukkit.Material.matchMaterial(msec.getString("material","PLAYER_HEAD"));
        membersLore = msec.getStringList("lore");
        if (msec.isList("tiers")){
            for (Object o : msec.getList("tiers")){
                if (o instanceof java.util.Map){
                    @SuppressWarnings("unchecked")
                    java.util.Map<String,Object> m = (java.util.Map<String,Object>) o;
                    int need = ((Number)m.getOrDefault("need",1)).intValue();
                    double cost = ((Number)m.getOrDefault("cost",15000)).doubleValue();
                    int mx = ((Number)m.getOrDefault("max",4)).intValue();
                    memberTiers.add(new Tier(need, cost, mx));
                }
            }
        }
    }
}

        if (c.isList("xp-purchase.packages")) {
            List<?> list = c.getList("xp-purchase.packages");
            if (list != null) {
                for (Object o : list) {
                    if (o instanceof java.util.Map) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> m = (java.util.Map<String, Object>) o;
                        String mat = String.valueOf(m.getOrDefault("material", "EXPERIENCE_BOTTLE"));
                        String name = String.valueOf(m.getOrDefault("name", "&a섬 경험치"));
                        int amount = ((Number) m.getOrDefault("amount", 100)).intValue();
                        double price = ((Number) m.getOrDefault("price", 1000)).doubleValue();
                        packages.add(new Package(mat, name, amount, price));
                    }
                }
            }
        } else if (c.isConfigurationSection("xp-purchase.packages")) {
            ConfigurationSection psec = c.getConfigurationSection("xp-purchase.packages");
            for (String k : psec.getKeys(false)) {
                ConfigurationSection s = psec.getConfigurationSection(k);
                String mat = s.getString("material", "EXPERIENCE_BOTTLE");
                String name = s.getString("name", "&a섬 경험치");
                int amount = s.getInt("amount", 100);
                double price = s.getDouble("price", 1000D);
                packages.add(new Package(mat, name, amount, price));
            }
        }
    }

    public void open(Player p) {
        int size = Math.max(9, ((packages.size() + 8) / 9) * 9);
        int size = Math.max(9, ((packages.size() + (sizeEnabled?1:0) + (membersEnabled?1:0) + (levelEnabled?1:0) + 8) / 9) * 9);
        Inventory inv = Bukkit.createInventory(null, size, title);
        int slot = 0;
        if (levelEnabled) inv.setItem(slot++, createLevelItem(p));
        if (sizeEnabled) inv.setItem(slot++, createUpgradeItem(p, UpgradeType.SIZE));
        if (membersEnabled) inv.setItem(slot++, createUpgradeItem(p, UpgradeType.MEMBERS));

        for (Package pack : packages) {
            inv.setItem(slot++, pack.toItem());
        }
        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView() == null || e.getView().getTitle() == null) return;
        if (!e.getView().getTitle().equals(title)) return;
        e.setCancelled(true);
        HumanEntity he = e.getWhoClicked();
        if (!(he instanceof Player)) return;
        Player p = (Player) he;
// Handle SIZE or MEMBERS
if (e.getRawSlot() < e.getInventory().getSize()){
    ItemStack clicked = e.getCurrentItem();
    if (clicked != null && clicked.hasItemMeta()){
        String dn = org.bukkit.ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (dn.equals(org.bukkit.ChatColor.stripColor(color(sizeName)))){
            handleUpgradeClick(p, UpgradeType.SIZE);
            return;
        } else if (dn.equals(org.bukkit.ChatColor.stripColor(color(membersName)))){
            handleUpgradeClick(p, UpgradeType.MEMBERS);
            return;
        }
    }
}
        ItemStack it = e.getCurrentItem();
        if (it == null || it.getType() == Material.AIR) return;

        // Match by display name & amount lore
        for (Package pack : packages) {
            if (pack.matches(it)) {
                double bal = economy.getBalance(p);
                if (bal < pack.price) {
                    p.sendMessage("§c잔액이 부족합니다. 필요: " + pack.price);
                    return;
                }
                economy.withdrawPlayer(p, pack.price);
                levelService.addXp(p, pack.amount);
                p.sendMessage("§a섬 경험치 +" + pack.amount + " §7(지불: " + pack.price + ")");
                p.closeInventory();
                return;
            }
        }
    }

    private static class Package {
        final String material;
        final String name;
        final int amount;
        final double price;

        Package(String material, String name, int amount, double price) {
            this.material = material;
            this.name = name;
            this.amount = amount;
            this.price = price;
        }

        ItemStack toItem() {
            Material m;
            try { m = Material.valueOf(material.toUpperCase()); }
            catch (Exception ex) { m = Material.EXPERIENCE_BOTTLE; }
            ItemStack is = new ItemStack(m);
            ItemMeta meta = is.getItemMeta();
            meta.setDisplayName(color(name));
            List<String> lore = new ArrayList<>();
            lore.add(color("&7경험치: &a+" + amount));
            lore.add(color("&7가격: &f" + price));
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            is.setItemMeta(meta);
            return is;
        }

        boolean matches(ItemStack other) {
            if (other == null || !other.hasItemMeta()) return false;
            ItemMeta om = other.getItemMeta();
            if (!om.hasDisplayName()) return false;
            String dn = org.bukkit.ChatColor.stripColor(om.getDisplayName());
            String bn = org.bukkit.ChatColor.stripColor(color(name));
            return dn.equals(bn);
        }

        private static String color(String s) { return s.replace("&", "§"); }
    }

    private static String color(String s) { return s.replace("&", "§"); }
}
