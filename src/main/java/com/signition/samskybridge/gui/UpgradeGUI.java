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

    
    private org.bukkit.inventory.ItemStack createLevelItem(org.bukkit.entity.Player p){
        world.bentobox.bentobox.managers.IslandsManager im = world.bentobox.bentobox.BentoBox.getInstance().getIslands();
        world.bentobox.bentobox.database.objects.Island island = im.getIsland(p.getWorld(), p.getUniqueId());
        org.bukkit.inventory.ItemStack it = new org.bukkit.inventory.ItemStack(levelMat != null ? levelMat : org.bukkit.Material.EXPERIENCE_BOTTLE);
        org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(color(levelName != null ? levelName : "&a섬 레벨 현황"));

        int level = 1;
        long cur = 0L, next = 1L;
        java.util.UUID owner = p.getUniqueId();
        try {
            if (island != null && island.getOwner()!=null) owner = island.getOwner();
            level = levelService.getLevel(owner);
            cur = levelService.getCurrentXp(owner);
            next = levelService.getNextXpRequirement(level + 1);
        } catch (Throwable ignore){}

        double pct = next > 0 ? (cur * 100.0 / next) : 0.0;
        int bars = (int)Math.round(Math.min(10, Math.max(0, pct/10.0)));
        StringBuilder bar = new StringBuilder();
        for (int i=0;i<10;i++){ bar.append(i<bars ? "§a■" : "§7■"); }

        java.util.List<String> lore = new java.util.ArrayList<>();
        java.util.List<String> tmpl = (levelLore!=null && !levelLore.isEmpty()) ? levelLore :
            java.util.Arrays.asList("&7섬 레벨: &f{level}","&7경험치: &f{xp}&7/&f{next} &8({percent}%)","&7진행도: &f{bar}");
        for (String line : tmpl){
            line = line.replace("{level}", String.valueOf(level))
                       .replace("{xp}", String.valueOf(cur))
                       .replace("{next}", String.valueOf(next))
                       .replace("{percent}", String.format(java.util.Locale.US,"%.1f", pct))
                       .replace("{bar}", bar.toString());
            lore.add(color(line));
        }
        meta.setLore(lore);
        it.setItemMeta(meta);
        return it;
    }

    private org.bukkit.inventory.ItemStack createUpgradeItem(org.bukkit.entity.Player p, UpgradeType type){
        org.bukkit.Material mat = (type==UpgradeType.SIZE) ? (sizeMat!=null?sizeMat:org.bukkit.Material.GRASS_BLOCK) : (membersMat!=null?membersMat:org.bukkit.Material.PLAYER_HEAD);
        String name = (type==UpgradeType.SIZE) ? sizeName : membersName;
        java.util.List<String> baseLore = (type==UpgradeType.SIZE) ? sizeLore : membersLore;
        java.util.List<Tier> tiers = (type==UpgradeType.SIZE) ? sizeTiers : memberTiers;

        world.bentobox.bentobox.managers.IslandsManager im = world.bentobox.bentobox.BentoBox.getInstance().getIslands();
        world.bentobox.bentobox.database.objects.Island island = im.getIsland(p.getWorld(), p.getUniqueId());
        int current = 0;
        if (island != null){
            current = (type==UpgradeType.SIZE) ? island.getProtectionRange() : island.getMaxMembers();
        }

        int nowStep = 0;
        for (int i=0;i<tiers.size();i++){
            if (current >= tiers.get(i).value) nowStep = i+1;
        }
        int maxStep = tiers.size();
        Tier nextTier = (nowStep < maxStep) ? tiers.get(nowStep) : null; // next step is index nowStep

        org.bukkit.inventory.ItemStack it = new org.bukkit.inventory.ItemStack(mat);
        org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(color(name));
        java.util.List<String> lore = new java.util.ArrayList<>();
        java.util.List<String> tmpl = (baseLore!=null && !baseLore.isEmpty()) ? baseLore :
            java.util.Arrays.asList("&7현재 레벨: &f{now}/{max}","&7요구 레벨: &fLv.{need}","&7가격: &a{cost}","&8다음 업그레이드 변화: {변화량}","&7클릭: 업그레이드");
        for (String line : tmpl){
            String need = nextTier!=null ? String.valueOf(nextTier.needLevel) : "MAX";
            String cost = nextTier!=null ? String.valueOf(nextTier.cost) : "MAX";
            String delta = nextTier!=null ? (current + " → " + nextTier.value) : "MAX";
            line = line.replace("{now}", String.valueOf(nowStep))
                       .replace("{max}", String.valueOf(maxStep))
                       .replace("{need}", need)
                       .replace("{cost}", cost)
                       .replace("{변화량}", delta)
                       .replace("{delta}", delta);
            lore.add(color(line));
        }
        meta.setLore(lore);
        it.setItemMeta(meta);
        return it;
    }

    private void handleUpgradeClick(org.bukkit.entity.Player p, UpgradeType type){
        world.bentobox.bentobox.managers.IslandsManager im = world.bentobox.bentobox.BentoBox.getInstance().getIslands();
        world.bentobox.bentobox.database.objects.Island island = im.getIsland(p.getWorld(), p.getUniqueId());
        if (island == null){ p.sendMessage("§c섬이 없습니다."); return; }

        java.util.List<Tier> tiers = (type==UpgradeType.SIZE) ? sizeTiers : memberTiers;
        int current = (type==UpgradeType.SIZE) ? island.getProtectionRange() : island.getMaxMembers();
        int nowStep = 0;
        for (int i=0;i<tiers.size();i++){
            if (current >= tiers.get(i).value) nowStep = i+1;
        }
        int maxStep = tiers.size();
        if (nowStep >= maxStep){ p.sendMessage("§c이미 최대 레벨입니다."); return; }
        Tier next = tiers.get(nowStep);

        java.util.UUID owner = island.getOwner()!=null ? island.getOwner() : p.getUniqueId();
        int lv = levelService.getLevel(owner);
        if (lv < next.needLevel){
            p.sendMessage(color("&c요구 레벨 Lv."+next.needLevel+" 필요합니다."));
            return;
        }
        if (economy == null){ p.sendMessage("§c경제 플러그인이 없습니다."); return; }
        if (economy.getBalance(p) < next.cost){
            p.sendMessage(color("&c잔액 부족: &f"+next.cost));
            return;
        }
        economy.withdrawPlayer(p, next.cost);

        if (type==UpgradeType.SIZE){
            island.setProtectionRange(next.value);
        } else {
            island.setMaxMembers(next.value);
        }
        im.save(island);

        try {
            // keep DataStore in sync if needed
            com.signition.samskybridge.data.IslandData is = plugin.getDataStore().getOrCreate(owner, p.getName());
            if (type==UpgradeType.SIZE) is.setSize(next.value); else is.setTeamMax(next.value);
            plugin.getDataStore().put(is);
        } catch (Throwable ignored){}

        p.sendMessage(color("&a업그레이드 완료!"));
        // reopen GUI to refresh
        open(p);
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
