package com.signition.samskybridge.upgrade;

import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.util.Text;
import com.signition.samskybridge.util.VaultHook;
import com.signition.samskybridge.integration.BentoSync;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class UpgradeService implements Listener {
    private final org.bukkit.plugin.Plugin plugin;
    private final com.signition.samskybridge.data.DataStore store; // kept for future use
    private final LevelService levels;
    private final VaultHook vault;

    public UpgradeService(org.bukkit.plugin.Plugin plugin,
                          com.signition.samskybridge.data.DataStore store,
                          LevelService levelService,
                          VaultHook vault){
        this.plugin = plugin;
        this.store = store;
        this.levels = levelService;
        this.vault = vault;
    }

    // ---------- public API ----------

    /** Opens the island upgrade GUI for the player. */
    public void openGUI(Player p){
        int size = plugin.getConfig().getInt("upgrades.gui.size", 27);
        size = clampInventorySize(size);
        String title = plugin.getConfig().getString("gui.title-upgrade", "섬 업그레이드");
        Inventory inv = Bukkit.createInventory(p, size, title);

        // Build feature items
        addFeatureItem(inv, p, "mine", 10, Material.IRON_PICKAXE);
        addFeatureItem(inv, p, "farm", 12, Material.WHEAT);
        addFeatureItem(inv, p, "size", 14, Material.GRASS_BLOCK);
        addFeatureItem(inv, p, "team", 16, Material.PLAYER_HEAD);

        p.openInventory(inv);
    }

    /** Inventory click entry point from GuiListener. */
    public void click(Player p, int slot, boolean shift){
        // Map slots corresponding to features (consistent with openGUI)
        Map<Integer, String> map = new HashMap<Integer, String>();
        map.put(10, "mine");
        map.put(12, "farm");
        map.put(14, "size");
        map.put(16, "team");

        String feature = map.get(slot);
        if (feature == null) return;
        tryUpgrade(p, feature);
        // Re-open GUI to reflect new state
        openGUI(p);
    }

    /** Returns current upgrade level for feature (per island owner UUID). */
    public int getLevel(java.util.UUID owner, String feature){
        return plugin.getConfig().getInt("progress."+owner.toString()+"."+feature, 0);
    }

    public int getLevel(Player p, String feature){
        return getLevel(p.getUniqueId(), feature);
    }

    // ---------- internal ----------

    private void addFeatureItem(Inventory inv, Player p, String feature, int slot, Material defIcon){
        if (slot < 0 || slot >= inv.getSize()) return;
        int now = getLevel(p, feature);
        int max = maxLevelOf(feature);
        String dn = plugin.getConfig().getString("upgrades."+feature+".gui.display-name", "&f"+feature.toUpperCase());
        List<String> loreTpl = plugin.getConfig().getStringList("upgrades."+feature+".gui.lore-template");
        if (loreTpl == null || loreTpl.isEmpty()){
            loreTpl = Arrays.asList(
                    "&7현재 레벨: &f{now}/{max}",
                    "&7요구 레벨: &fLv.{need}",
                    "&7가격: &a{cost}",
                    "&8다음 변화: &7{next}",
                    "{nextOreTable}"
            );
        }
        ConfigurationSection lvSec = plugin.getConfig().getConfigurationSection("upgrades."+feature+".levels");
        int next = Math.min(now + 1, max);
        int need = 0;
        String nextBonus = "";
        String nextRegen = "";
        int nextReplant = 0;
        double cost = 0D;

        if (lvSec != null && lvSec.contains(String.valueOf(next))){
            ConfigurationSection s = lvSec.getConfigurationSection(String.valueOf(next));
            need = s.getInt("require-island-level", 0);
            cost = s.getDouble("cost", 0D);
            if (feature.equals("mine")){
                nextBonus = s.getString("bonus", "");
                nextRegen = String.valueOf(s.getInt("regen", 0));
            } else if (feature.equals("farm")){
                nextBonus = s.getString("bonus", "");
                nextReplant = s.getInt("replant", 0);
            } else if (feature.equals("size")){
                nextBonus = s.getString("bonus", "");
            } else if (feature.equals("team")){
                nextBonus = s.getString("bonus", "");
            }
        }

        // Build ItemStack
        Material icon = defIcon;
        if (lvSec != null && lvSec.contains(now + ".icon")){
            String mat = lvSec.getString(now + ".icon");
            Material m = Material.matchMaterial(mat == null ? "" : mat);
            if (m != null) icon = m;
        }
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Text.color(dn));

        List<String> lore = new ArrayList<String>();
        for (String line : loreTpl){
            String L = line;
            L = L.replace("{now}", String.valueOf(now))
                 .replace("{max}", String.valueOf(max))
                 .replace("{need}", String.valueOf(need))
                 .replace("{cost}", String.valueOf((long)cost))
                 .replace("{nextBonus}", nextBonus == null ? "" : nextBonus)
                 .replace("{nextRegen}", nextRegen == null ? "" : nextRegen)
                 .replace("{nextReplant}", String.valueOf(nextReplant));
            if (L.contains("{nextOreTable}") && "mine".equals(feature)){
                List<String> table = buildNextOreTable(next);
                for (String tline : table) lore.add(Text.color(tline));
                continue;
            }
            lore.add(Text.color(L));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }

    private List<String> buildNextOreTable(int nextLevel){
        List<String> out = new ArrayList<String>();
        // Support both formats: upgrades.mine.gui.next-ore-table-* OR upgrades.mine.gui-ore-table.*
        String title = plugin.getConfig().getString("upgrades.mine.gui.next-ore-table-title",
                         plugin.getConfig().getString("upgrades.mine.gui-ore-table.title", "&8- &7광물 테이블:"));
        String lineFmt = plugin.getConfig().getString("upgrades.mine.gui.next-ore-table-line",
                         plugin.getConfig().getString("upgrades.mine.gui-ore-table.line", "&8  • &7{mat}&7: &a{chance}%"));
        int maxLines = plugin.getConfig().getInt("upgrades.mine.gui.next-ore-table-max-lines",
                         plugin.getConfig().getInt("upgrades.mine.gui-ore-table.max-lines", 12));
        out.add(title);

        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("features.mine.levels."+nextLevel+".weights");
        if (sec != null){
            int cnt = 0;
            for (String k : sec.getKeys(false)){
                if (cnt++ >= maxLines) break;
                int val = sec.getInt(k, 0);
                String li = lineFmt.replace("{mat}", k).replace("{chance}", String.valueOf(val));
                out.add(li);
            }
        }
        return out;
    }

    private void tryUpgrade(Player p, String feature){
        int now = getLevel(p, feature);
        int max = maxLevelOf(feature);
        if (now >= max){
            p.sendMessage(Text.color("&7이미 최대 레벨입니다."));
            return;
        }
        int next = now + 1;
        ConfigurationSection s = plugin.getConfig().getConfigurationSection("upgrades."+feature+".levels."+next);
        if (s == null){
            p.sendMessage(Text.color("&c다음 레벨 설정이 없습니다."));
            return;
        }
        int need = s.getInt("require-island-level", 0);
        int islandLv = levels.getLevel(p);
        if (islandLv < need){
            p.sendMessage(Text.color("&c섬 레벨이 부족합니다. &7(필요: "+need+", 현재: "+islandLv+")"));
            return;
        }
        double cost = s.getDouble("cost", 0D);
        if (cost > 0 && !vault.withdraw(p, cost)){
            p.sendMessage(Text.color("&c잔액이 부족합니다. &7가격: "+(long)cost));
            return;
        }

        // Apply
        setLevel(p, feature, next);

        // Post effects
        if ("size".equals(feature)){
            int radius = plugin.getConfig().getInt("upgrades.size.levels."+next+".radius", 50);
            if (plugin.getConfig().getBoolean("bento.apply-range-on-upgrade", true)){
                new BentoSync(plugin).applyRangeInstant(p, radius);
            }
            // barrier visualize
            try {
                new com.signition.samskybridge.upgrade.Barrier(plugin).show(p, radius);
            } catch (Throwable ignored){}
        } else if ("team".equals(feature)){
            int team = plugin.getConfig().getInt("upgrades.team.levels."+next+".team", 4);
            if (plugin.getConfig().getBoolean("bento.apply-teammax-on-upgrade", true)){
                new BentoSync(plugin).applyTeamMax(p, team);
            }
        }

        p.sendMessage(Text.color("&a업그레이드 완료: &f"+feature+" &7→ &bLv."+next));
    }

    private int maxLevelOf(String feature){
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("upgrades."+feature+".levels");
        int max = 0;
        if (sec != null){
            for (String k : sec.getKeys(false)){
                try {
                    int n = Integer.parseInt(k);
                    if (n > max) max = n;
                } catch (NumberFormatException ignored){}
            }
        }
        // fallback to features.<feature>.max-level if declared
        int m2 = plugin.getConfig().getInt("features."+feature+".max-level", 0);
        if (m2 > 0) max = Math.max(max, m2);
        if (max <= 0) max = 5;
        return max;
    }

    private void setLevel(Player p, String feature, int lv){
        plugin.getConfig().set("progress."+p.getUniqueId().toString()+"."+feature, lv);
        plugin.saveConfig();
    }

    private int clampInventorySize(int x){
        int[] allowed = {9,18,27,36,45,54};
        for (int v : allowed){ if (x <= v) return v; }
        return 54;
    }
}
