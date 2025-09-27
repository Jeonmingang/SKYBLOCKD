package com.signition.samskybridge.upgrade;

import com.signition.samskybridge.util.Text;
import com.signition.samskybridge.util.VaultHook;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UpgradeService implements org.bukkit.event.Listener {
    private final org.bukkit.plugin.Plugin plugin;
    private String getUiTemplate(String key, String def){
        String a = plugin.getConfig().getString("upgrades-ui."+key, null);
        return a != null ? a : plugin.getConfig().getString("upgrades."+key, def);
    }
    private org.bukkit.Material getLevelIcon(String feature, int level, org.bukkit.Material def){
        String p = "upgrades." + feature + ".levels." + level + ".icon";
        if (plugin.getConfig().isString(p)){
            org.bukkit.Material m = org.bukkit.Material.matchMaterial(plugin.getConfig().getString(p,""));
            if (m != null) return m;
        }
        return def;
    }
    private String getRequiredPermission(String feature){
        return plugin.getConfig().getString("upgrades." + feature + ".require.permission", "");
    }
     implements Listener {

    private int getMenuSize(){
        int s = plugin.getConfig().getInt("upgrades.gui.size", 27);
        if (s < 9) s = 9;
        if (s > 54) s = 54;
        s = (s / 9) * 9;
        return s;
    }
    private org.bukkit.Material getFillerMaterial(){
        String m = plugin.getConfig().getString("upgrades.gui.filler.material", null);
        if (m == null || m.isEmpty()) return null;
        return org.bukkit.Material.matchMaterial(m);
    }
    private org.bukkit.inventory.ItemStack buildFiller(){
        org.bukkit.Material mat = getFillerMaterial();
        if (mat == null) return null;
        org.bukkit.inventory.ItemStack is = new org.bukkit.inventory.ItemStack(mat);
        org.bukkit.inventory.meta.ItemMeta meta = is.getItemMeta();
        String name = plugin.getConfig().getString("upgrades.gui.filler.display-name", "&r");
        java.util.List<String> lore = plugin.getConfig().getStringList("upgrades.gui.filler.lore");
        meta.setDisplayName(com.signition.samskybridge.util.Text.color(name));
        java.util.List<String> out = new java.util.ArrayList<String>();
        for (String l : lore) out.add(com.signition.samskybridge.util.Text.color(l));
        meta.setLore(out);
        is.setItemMeta(meta);
        return is;
    }
    private String getDisplayName(String feature){
        String def = "&f" + feature.toUpperCase();
        return com.signition.samskybridge.util.Text.color(plugin.getConfig().getString("upgrades." + feature + ".gui.display-name", def));
    }
    private boolean glow(String feature){
        return plugin.getConfig().getBoolean("upgrades." + feature + ".gui.enchant-glow", false);
    }
    private int cmd(String feature){
        return plugin.getConfig().getInt("upgrades." + feature + ".gui.custom-model-data", -1);
    }
    private boolean closeOnUpgrade(){
        return plugin.getConfig().getBoolean("upgrades.gui.close-on-upgrade", true);
    }
    


    private String getOreTableTitle(){
        String a = plugin.getConfig().getString("upgrades.mine.gui.next-ore-table-title", null);
        if (a != null) return a;
        return plugin.getConfig().getString("upgrades.mine.gui-ore-table.title", "&8- &7광물 테이블:");
    }
    private String getOreTableLine(){
        String a = plugin.getConfig().getString("upgrades.mine.gui.next-ore-table-line", null);
        if (a != null) return a;
        return plugin.getConfig().getString("upgrades.mine.gui-ore-table.line", "&8  • &7{mat}&7: &a{chance}%");
    }
    private int getOreTableMaxLines(){
        if (plugin.getConfig().isInt("upgrades.mine.gui.next-ore-table-max-lines"))
            return plugin.getConfig().getInt("upgrades.mine.gui.next-ore-table-max-lines");
        return plugin.getConfig().getInt("upgrades.mine.gui-ore-table.max-lines", 12);
    }
    


    private final Plugin plugin;
    private final com.signition.samskybridge.data.DataStore store;
    private final com.signition.samskybridge.level.LevelService levels;
    private final VaultHook vault;
    private Inventory inv;

    public UpgradeService(Plugin plugin, com.signition.samskybridge.data.DataStore store,
                          com.signition.samskybridge.level.LevelService levels, VaultHook vault){
        this.plugin = plugin; this.store = store; this.levels = levels; this.vault = vault;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public int getLevel(UUID id, String type){ return store.getLevel(id, type); }
    public void setLevel(UUID id, String type, int lv){ store.setLevel(id, type, lv); }

    public int getMaxLevel(String feature){
        if ("mine".equals(feature)) return plugin.getConfig().getInt("features.mine.max-level", 5);
        if ("farm".equals(feature)) return plugin.getConfig().getInt("features.farm.max-level", 5);
        return getDefinedLevels("upgrades." + feature + ".levels");
    }

    private int getDefinedLevels(String path){
        org.bukkit.configuration.ConfigurationSection sec = plugin.getConfig().getConfigurationSection(path);
        if (sec == null) return 5;
        int max = 1;
        for (String k : sec.getKeys(false)) try { max = Math.max(max, Integer.parseInt(k)); } catch (Exception ignored){}
        return max;
    }

    public int getRequiredIslandLevel(String feature, int next){ return plugin.getConfig().getInt("upgrades." + feature + ".levels." + next + ".require-island-level", 0); }
    public int getCost(String feature, int next){ return plugin.getConfig().getInt("upgrades." + feature + ".levels." + next + ".cost", 0); }
    public String[] getLoreTemplate(String feature){ java.util.List<String> l = plugin.getConfig().getStringList("upgrades." + feature + ".gui.lore-template"); return l.toArray(new String[0]); }
    public String getMenuTitle(){ return plugin.getConfig().getString("upgrades.gui.title","&b섬 업그레이드"); }
    public int getSlot(String feature, int def){ return plugin.getConfig().getInt("upgrades.gui.slots." + feature, def); }
    public Material getIcon(String feature, Material def){ String s = plugin.getConfig().getString("upgrades." + feature + ".gui.icon", def.name()); Material m = Material.matchMaterial(s); return m!=null?m:def; }

    public String getNextBonus(String feature, int next){ return plugin.getConfig().getString("upgrades." + feature + ".levels." + next + ".bonus","-"); }
    public String getNextRegen(String feature, int next){
        int v = plugin.getConfig().getInt("upgrades." + feature + ".levels." + next + ".regen",
                plugin.getConfig().getInt("features." + feature + ".levels." + next + ".regen-seconds", 0));
        return v>0? String.valueOf(v):"-";
    }
    public String getNextReplant(String feature, int next){
        int v = plugin.getConfig().getInt("upgrades." + feature + ".levels." + next + ".replant",
                plugin.getConfig().getInt("features." + feature + ".replant.delay-ticks", 0));
        return v>0? String.valueOf(v):"-";
    }
    public int getSizeValue(int lv){ return plugin.getConfig().getInt("upgrades.size.levels." + lv + ".range", 100 + (lv*10)); }
    public int getTeamValue(int lv){ return plugin.getConfig().getInt("upgrades.team.levels." + lv + ".team", 4 + (lv-1)*2); }

    public java.util.List<String> renderNextOreTable(int next){
        java.util.List<String> out = new java.util.ArrayList<String>();
        org.bukkit.configuration.ConfigurationSection sec =
                plugin.getConfig().getConfigurationSection("features.mine.levels." + next + ".weights");
        if (sec == null) return out;
        String title = getOreTableTitle();
        String line = getOreTableLine();
        int maxLines = getOreTableMaxLines();
        out.add(title);
        int c=0;
        for (String k : sec.getKeys(false)){
            String row = line.replace("{mat}", k).replace("{chance}", String.valueOf(sec.getInt(k)));
            out.add(row);
            if (++c >= maxLines) break;
        }
        return out;
    }&7: &a{chance}%");
        int maxLines = plugin.getConfig().getInt("upgrades.mine.gui-ore-table.max-lines", 12);
        out.add(title);
        int c=0;
        for (String k : sec.getKeys(false)){
            String row = line.replace("{mat}", k).replace("{chance}", String.valueOf(sec.getInt(k)));
            out.add(row);
            if (++c >= maxLines) break;
        }
        return out;
    }

    public java.util.List<String> buildLore(Player p, String feature){
        String reqPerm = getRequiredPermission(feature);
        if (reqPerm != null && !reqPerm.isEmpty() && !p.hasPermission(reqPerm)){
            p.sendMessage(Text.color("&c권한이 없습니다: " + reqPerm));
            return;
        }
        int now = getLevel(p.getUniqueId(), feature);
        int max = getMaxLevel(feature);
        int next = Math.min(max, now+1);
        int need = getRequiredIslandLevel(feature, next);
        int cost = getCost(feature, next);

        java.util.List<String> lines = new java.util.ArrayList<String>();
        for (String raw : getLoreTemplate(feature)){
            String line = raw
                    .replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(need))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", getNextBonus(feature, next))
                    .replace("{nextRegen}", getNextRegen(feature, next))
                    .replace("{nextReplant}", getNextReplant(feature, next))
                    .replace("{range}", String.valueOf(getSizeValue(now)))
                    .replace("{rangeNext}", String.valueOf(getSizeValue(next)))
                    .replace("{teamNow}", String.valueOf(getTeamValue(now)))
                    .replace("{teamNext}", String.valueOf(getTeamValue(next)));
            if (line.contains("{nextOreTable}")){
                java.util.List<String> block = renderNextOreTable(next);
                if (!block.isEmpty()){
                    boolean first=true;
                    for (String b : block){
                        lines.add(com.signition.samskybridge.util.Text.color(first ? line.replace("{nextOreTable}", b) : b));
                        first=false;
                    }
                    continue;
                } else {
                    line = line.replace("{nextOreTable}","");
                }
            }
            lines.add(com.signition.samskybridge.util.Text.color(line));
        }
        for (String l : plugin.getConfig().getStringList("upgrades." + feature + ".levels." + next + ".lore")){
            lines.add(com.signition.samskybridge.util.Text.color(l));
        }
        return lines;
    }

    
    // ----- Optional Shop (upgrades.shop.items.*) -----
    private java.util.Map<Integer, String> shopSlots = new java.util.HashMap<Integer, String>();

    private void populateShop(org.bukkit.entity.Player p){
        shopSlots.clear();
        org.bukkit.configuration.ConfigurationSection sec = plugin.getConfig().getConfigurationSection("upgrades.shop.items");
        if (sec == null) return;
        for (String key : sec.getKeys(false)){
            org.bukkit.configuration.ConfigurationSection it = sec.getConfigurationSection(key);
            if (it == null) continue;
            String icon = it.getString("icon","PAPER");
            int slot = it.getInt("slot", 22);
            int amount = it.getInt("amount", 1);
            int cost = it.getInt("cost", 0);
            String name = it.getString("display-name","&fItem");
            java.util.List<String> lore = it.getStringList("lore");
            org.bukkit.Material mat = org.bukkit.Material.matchMaterial(icon);
            if (mat == null) mat = org.bukkit.Material.PAPER;
            org.bukkit.inventory.ItemStack is = new org.bukkit.inventory.ItemStack(mat);
            org.bukkit.inventory.meta.ItemMeta meta = is.getItemMeta();
            meta.setDisplayName(com.signition.samskybridge.util.Text.color(name));
            java.util.List<String> out = new java.util.ArrayList<String>();
            for (String l : lore){
                out.add(com.signition.samskybridge.util.Text.color(l.replace("{amount}", String.valueOf(amount)).replace("{cost}", String.valueOf(cost))));
            }
            meta.setLore(out);
            is.setItemMeta(meta);
            try { inv.setItem(slot, is); shopSlots.put(slot, key); } catch (Throwable ignored){}
        }
    }

    private void handleShopClick(org.bukkit.entity.Player p, int slot){
        String key = shopSlots.get(slot);
        if (key == null) return;
        org.bukkit.configuration.ConfigurationSection it = plugin.getConfig().getConfigurationSection("upgrades.shop.items." + key);
        if (it == null) return;
        String type = it.getString("type","xp");
        int amount = it.getInt("amount", 1);
        int cost = it.getInt("cost", 0);

        // economy
        if (cost > 0){
            if (vault.balance(p) < cost){ p.sendMessage(com.signition.samskybridge.util.Text.color("&c잔액이 부족합니다. 필요: " + cost)); return; }
            if (!vault.withdraw(p, cost)){ p.sendMessage(com.signition.samskybridge.util.Text.color("&6경고: Vault 인출 실패. 무료 처리.")); }
        }

        if ("xp".equalsIgnoreCase(type)){
            p.giveExp(amount);
            p.sendMessage(com.signition.samskybridge.util.Text.color("&a경험치 " + amount + " 획득"));
        } else if ("command".equalsIgnoreCase(type)){
            String cmd = it.getString("command","");
            if (cmd != null && !cmd.isEmpty()){
                cmd = cmd.replace("{player}", p.getName()).replace("{amount}", String.valueOf(amount)).replace("{cost}", String.valueOf(cost));
                org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), cmd);
            }
        } else if ("item".equalsIgnoreCase(type)){
            String mat = it.getString("item","STONE");
            org.bukkit.Material m = org.bukkit.Material.matchMaterial(mat);
            if (m != null){
                p.getInventory().addItem(new org.bukkit.inventory.ItemStack(m, Math.max(1, amount)));
            }
        }
    }
    
public void openGUI(Player p){
        String title = com.signition.samskybridge.util.Text.color(getMenuTitle());
        inv = Bukkit.createInventory(null, getMenuSize(), title);
        inv.setItem(getSlot("team",12), iconFor(p,"team", getIcon("team", Material.PLAYER_HEAD)));
        inv.setItem(getSlot("size",14), iconFor(p,"size", getIcon("size", Material.GRASS_BLOCK)));
        inv.setItem(getSlot("mine",24), iconFor(p,"mine", getIcon("mine", Material.IRON_PICKAXE)));
        inv.setItem(getSlot("farm",25), iconFor(p,"farm", getIcon("farm", Material.WHEAT)));

        // XP shop item (optional)
        if (plugin.getConfig().getBoolean("shop.xp.enabled", false)){
            int slot = plugin.getConfig().getInt("shop.xp.slot", 22);
            String icon = plugin.getConfig().getString("shop.xp.icon","EXPERIENCE_BOTTLE");
            Material mat = Material.matchMaterial(icon);
            if (mat == null) mat = Material.EXPERIENCE_BOTTLE;
            ItemStack it = new ItemStack(mat);
            ItemMeta meta = it.getItemMeta();
            String name = plugin.getConfig().getString("shop.xp.name","&d경험치 구매");
            meta.setDisplayName(com.signition.samskybridge.util.Text.color(name));
            java.util.List<String> loreT = plugin.getConfig().getStringList("shop.xp.lore");
            int amount = plugin.getConfig().getInt("shop.xp.amount", 100);
            int price = plugin.getConfig().getInt("shop.xp.price", 1000);
            java.util.List<String> lore = new java.util.ArrayList<String>();
            for (String s : loreT){
                s = s.replace("{amount}", String.valueOf(amount)).replace("{price}", String.valueOf(price));
                lore.add(com.signition.samskybridge.util.Text.color(s));
            }
            meta.setLore(lore);
            if (glow(feature)) { try { meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS); } catch (Throwable ignored){} }
        int data = cmd(feature); if (data >= 0) try { meta.setCustomModelData(data); } catch (Throwable ignored) {}
        it.setItemMeta(meta);
            inv.setItem(slot, it);
        }

        populateShop(p);
        p.openInventory(inv);
    }

    private ItemStack iconFor(Player p, String feature, Material icon){
        Material iconMat = getLevelIcon(feature, now, icon);
        ItemStack it = new ItemStack(iconMat);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(getDisplayName(feature));
        meta.setLore(buildLore(p, feature));
        if (glow(feature)) { try { meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS); } catch (Throwable ignored){} }
        int data = cmd(feature); if (data >= 0) try { meta.setCustomModelData(data); } catch (Throwable ignored) {}
        it.setItemMeta(meta);
        return it;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if (inv == null || e.getInventory() != inv) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();
        // shop first
        handleShopClick(p, slot);


        // XP shop click
        if (plugin.getConfig().getBoolean("shop.xp.enabled", false) && slot == plugin.getConfig().getInt("shop.xp.slot", 22)){
            int amount = plugin.getConfig().getInt("shop.xp.amount", 100);
            int price = plugin.getConfig().getInt("shop.xp.price", 1000);
            if (price > 0){
                if (vault.balance(p) < price){ p.sendMessage(Text.msg("not-enough-money").replace("{cost}", String.valueOf(price))); return; }
                if (!vault.withdraw(p, price)){ p.sendMessage(Text.msg("purchase-xp-fail")); return; }
            }
            p.giveExp(amount);
            p.sendMessage(Text.msg("purchase-xp-success").replace("{amount}", String.valueOf(amount)));
            return;
        }

        String feature = null;
        if (slot == getSlot("team",12)) feature = "team";
        if (slot == getSlot("size",14)) feature = "size";
        if (slot == getSlot("mine",24)) feature = "mine";
        if (slot == getSlot("farm",25)) feature = "farm";
        if (feature == null) return;

        String reqPerm = getRequiredPermission(feature);
        if (reqPerm != null && !reqPerm.isEmpty() && !p.hasPermission(reqPerm)){
            p.sendMessage(Text.color("&c권한이 없습니다: " + reqPerm));
            return;
        }
        int now = getLevel(p.getUniqueId(), feature);
        int max = getMaxLevel(feature);
        if (now >= max){ p.sendMessage(Text.msg("upgrade-max")); return; }
        int next = now + 1;
        int need = getRequiredIslandLevel(feature, next);

        int islandLv = 0;
        try {
            islandLv = new com.signition.samskybridge.integration.BentoSync(org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(getClass())).getIslandLevel(p);
        } catch (Throwable ignored){}

        if (islandLv < need){
            p.sendMessage(Text.msg("require-level-fail").replace("{need}", String.valueOf(need)));
            return;
        }

        int cost = getCost(feature, next);
        if (cost > 0){
            if (vault.balance(p) < cost){ p.sendMessage(Text.msg("not-enough-money").replace("{cost}", String.valueOf(cost))); return; }
            if (!vault.withdraw(p, cost)){ p.sendMessage(Text.msg("upgrade-no-vault")); }
        }
        setLevel(p.getUniqueId(), feature, next);
        p.sendMessage(Text.msg("upgrade-success").replace("{feature}", feature).replace("{now}", String.valueOf(now)).replace("{next}", String.valueOf(next)));
        if (closeOnUpgrade()) p.closeInventory();
        HandlerList.unregisterAll(this);
    }
}
