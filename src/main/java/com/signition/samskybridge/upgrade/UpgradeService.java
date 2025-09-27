package com.signition.samskybridge.upgrade;

import java.util.*;import com.signition.samskybridge.util.Text;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.util.VaultHook;
import com.signition.samskybridge.integration.BentoSync;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class UpgradeService {

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

    private java.util.List<String> buildTopDetailLines(java.util.Map<String,Double> now, java.util.Map<String,Double> nxt, int topN, String lineFmt){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

        java.util.Set<String> keys = new java.util.HashSet<>(); keys.addAll(now.keySet()); keys.addAll(nxt.keySet());
        java.util.List<String> items = new java.util.ArrayList<>(keys);
        items.sort((a,b)-> Double.compare(nxt.getOrDefault(b,0.0), nxt.getOrDefault(a,0.0)));
        java.util.List<String> out = new java.util.ArrayList<>();
        for (int i=0;i<items.size() && i<topN;i++){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            String k = items.get(i);
            double a = Math.round(now.getOrDefault(k,0.0));
            double b = Math.round(nxt.getOrDefault(k,0.0));
            String line = lineFmt.replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
name}", k).replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
now}", String.valueOf((int)a)).replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
next}", String.valueOf((int)b));
            out.add(line);
        }
        return out;
    }

private java.util.Map<String, Double> parseDist(java.util.List<String> defs){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

        java.util.Map<String, Double> out = new java.util.LinkedHashMap<>();
        if (defs == null) return out;
        for (String s : defs){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            try{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

                String[] sp = s.trim().split(":");
                String name = sp[0].trim();
                double v = (sp.length>1)? Double.parseDouble(sp[1].trim()) : 1.0;
                out.put(name, out.getOrDefault(name, 0.0)+v);
            }catch(Throwable ignore){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
}
        }
        double sum = 0.0; for (double v: out.values()) sum += v;
        if (sum <= 0) return out;
        for (java.util.Map.Entry<String,Double> e: out.entrySet()){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            out.put(e.getKey(), (e.getValue()/sum)*100.0);
        }
        return out;
    }

    private final Main plugin;
    private final DataStore store;
    private final LevelService level;
    private final VaultHook vault;
    private final BentoSync bento;

    public UpgradeService(Main plugin, DataStore store, LevelService level, VaultHook vault, BentoSync bento){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

        this.plugin = plugin;
        this.store = store;
        this.level = level;
        this.vault = vault;
        this.bento = bento;
    }
    public UpgradeService(Main plugin, DataStore store, LevelService level, VaultHook vault){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

        this(plugin, store, level, vault, plugin.getBento());
    }


    /* ===================== GUI ===================== */
    public void openGui(Player p){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

        Inventory inv = Bukkit.createInventory(null, 27, plugin.getConfig().getString("gui.title-upgrade","섬 업그레이드"));
        IslandData is = store.getOrCreate(p.getUniqueId(), p.getName());

        int slotSize = plugin.getConfig().getInt("upgrade.gui.slots.size", 14);
        int slotTeam = plugin.getConfig().getInt("upgrade.gui.slots.team", 12);

        // Size pane
        int sizeNow = is.getSize();
        int sizeNext = nextSizeValue(sizeNow);
        int sizeNeedLv = needLevelSize(sizeNow);
        long sizeCost = Math.round(costSize(sizeNow));
        int sizeStepNow = currentSizeStep(sizeNow);
        int sizeStepMax = maxSizeStep();

        List<String> sizeLore = plugin.getConfig().getStringList("upgrade.size.lore");
        if (sizeLore == null || sizeLore.isEmpty()){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            sizeLore = new ArrayList<>();
            sizeLore.add("&7현재 보호반경: &f{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
range} 블럭");
            sizeLore.add("&7다음 단계: &a{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
next} 블럭");
            sizeLore.add("&7요구 레벨: &bLv.{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
reqLevel}");
            sizeLore.add("&7필요 금액: &a{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
cost}");
            sizeLore.add("&7업그레이드 레벨: &d{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
stepNow}/{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
stepMax}");
            sizeLore.add("&8클릭: 업그레이드");
        }
        String[] sizeLoreArr = new String[sizeLore.size()];
        for (int i=0;i<sizeLore.size();i++){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            sizeLoreArr[i] = sizeLore.get(i)
                    .replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
range}", String.valueOf(sizeNow))
                    .replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
next}", String.valueOf(sizeNext))
                    .replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
reqLevel}", String.valueOf(sizeNeedLv))
                    .replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
cost}", String.valueOf(sizeCost))
                    .replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
stepNow}", String.valueOf(sizeStepNow))
                    .replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
stepMax}", sizeStepMax==1000? "∞" : String.valueOf(sizeStepMax));
        }
        inv.setItem(slotSize, named(new ItemStack(Material.MAP),
                plugin.getConfig().getString("upgrade.size.title","&e섬 크기 업그레이드"),
                sizeLoreArr));

        // Team pane
        int teamNow = is.getTeamMax();
        int teamNext = nextTeamValue(teamNow);
        int teamNeedLv = needLevelTeam(teamNow);
        long teamCost = Math.round(costTeam(teamNow));
        int teamStepNow = currentTeamStep(teamNow);
        int teamStepMax = maxTeamStep();

        List<String> teamLore = plugin.getConfig().getStringList("upgrade.team.lore");
        if (teamLore == null || teamLore.isEmpty()){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            teamLore = new ArrayList<>();
            teamLore.add("&7현재 한도: &f{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
current}명");
            teamLore.add("&7다음 단계: &a{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
next}명");
            teamLore.add("&7요구 레벨: &bLv.{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
reqLevel}");
            teamLore.add("&7필요 금액: &a{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
cost}");
            teamLore.add("&7업그레이드 레벨: &d{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
stepNow}/{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
stepMax}");
            teamLore.add("&8클릭: 업그레이드");
        }
        String[] teamLoreArr = new String[teamLore.size()];
        for (int i=0;i<teamLore.size();i++){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            teamLoreArr[i] = teamLore.get(i)
                    .replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
current}", String.valueOf(teamNow))
                    .replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
next}", String.valueOf(teamNext))
                    .replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
reqLevel}", String.valueOf(teamNeedLv))
                    .replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
cost}", String.valueOf(teamCost))
                    .replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
stepNow}", String.valueOf(teamStepNow))
                    .replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
stepMax}", teamStepMax==1000? "∞" : String.valueOf(teamStepMax));
        }
        
        // Mine upgrade pane
        int slotMine = plugin.getConfig().getInt("upgrade.gui.slots.mine", 24);
        int mineLv = plugin.getFeatures().getMineLevel(is.getId());
            int mineMax = plugin.getConfig().getInt("features.mine.max-level", 5);
            java.util.Map<String,Double> distNow = parseDist(plugin.getConfig().getStringList("features.mine.ores."+mineLv));
            java.util.Map<String,Double> distNext = parseDist(plugin.getConfig().getStringList("features.mine.ores."+(mineLv+1)));java.util.List<String> mineLore = plugin.getConfig().getStringList("features.mine.lore");
        if (mineLore == null || mineLore.isEmpty()){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            mineLore = new java.util.ArrayList<>();
            mineLore.add("&7현재 레벨: &f{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
now}/{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
max}");
            mineLore.add("&7요구 레벨: &bLv.{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
need}");
            mineLore.add("&7가격: &a{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
cost}");
            int topN = plugin.getConfig().getInt("upgrade.ui.mine.topN", 5);
String fmt = plugin.getConfig().getString("upgrade.ui.mine.line", "&8- &f{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
name}: &a{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
now}% &7→ &b{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
next}%");
java.util.List<String> detail = buildTopDetailLines(distNow, distNext, topN, fmt);
for (String dl : detail) mineLore.add(dl);
mineLore.add("&8클릭: 업그레이드");
        }
        int needMine = plugin.getConfig().getInt("features.mine.require.base", 3) + mineLv * plugin.getConfig().getInt("features.mine.require.per-step", 1);
        double costMine = plugin.getConfig().getDouble("features.mine.cost.base", 5000.0) * Math.pow(plugin.getConfig().getDouble("features.mine.cost.multiplier", 1.25), mineLv);
        String[] mineLoreArr = new String[mineLore.size()];
        for (int i=0;i<mineLore.size();i++){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            mineLoreArr[i] = mineLore.get(i)
                .replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
now}", String.valueOf(mineLv))
                .replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
max}", String.valueOf(mineMax))
                .replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
need}", String.valueOf(needMine))
                .replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
cost}", String.valueOf((long)costMine));
        }
        inv.setItem(slotMine, named(new ItemStack(Material.IRON_PICKAXE),
                plugin.getConfig().getString("features.mine.title","&6광산 업그레이드"),
                mineLoreArr));

        // Farm upgrade pane
        int slotFarm = plugin.getConfig().getInt("upgrade.gui.slots.farm", 25);
        int farmLv = plugin.getFeatures().getFarmLevel(is.getId());
            int farmMax = plugin.getConfig().getInt("features.farm.max-level", 5);
            java.util.Map<String,Double> fNow = parseDist(plugin.getConfig().getStringList("features.farm.crops."+farmLv));
            java.util.Map<String,Double> fNext = parseDist(plugin.getConfig().getStringList("features.farm.crops."+(farmLv+1)));java.util.List<String> farmLore = plugin.getConfig().getStringList("features.farm.lore");
        if (farmLore == null || farmLore.isEmpty()){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            farmLore = new java.util.ArrayList<>();
            farmLore.add("&7현재 레벨: &f{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
now}/{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
max}");
            farmLore.add("&7요구 레벨: &bLv.{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
need}");
            farmLore.add("&7가격: &a{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
cost}");
            int fTop = plugin.getConfig().getInt("upgrade.ui.farm.topN", 5);
String fFmt = plugin.getConfig().getString("upgrade.ui.farm.line", "&8- &f{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
name}: &a{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
now}% &7→ &b{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
next}%");
java.util.List<String> fDetail = buildTopDetailLines(fNow, fNext, fTop, fFmt);
for (String dl : fDetail) farmLore.add(dl);
farmLore.add("&8클릭: 업그레이드");
        }
        int needFarm = plugin.getConfig().getInt("features.farm.require.base", 2) + farmLv * plugin.getConfig().getInt("features.farm.require.per-step", 1);
        double costFarm = plugin.getConfig().getDouble("features.farm.cost.base", 4000.0) * Math.pow(plugin.getConfig().getDouble("features.farm.cost.multiplier", 1.2), farmLv);
        String[] farmLoreArr = new String[farmLore.size()];
        for (int i=0;i<farmLore.size();i++){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            farmLoreArr[i] = farmLore.get(i)
                .replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
now}", String.valueOf(farmLv))
                .replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
max}", String.valueOf(farmMax))
                .replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
need}", String.valueOf(needFarm))
                .replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
cost}", String.valueOf((long)costFarm));
        }
        inv.setItem(slotFarm, named(new ItemStack(Material.WHEAT),
                plugin.getConfig().getString("features.farm.title","&6농장 업그레이드"),
                farmLoreArr));
    inv.setItem(slotTeam, named(new ItemStack(Material.PLAYER_HEAD),
                plugin.getConfig().getString("upgrade.team.title","&b섬 인원 업그레이드"),
                teamLoreArr));

        // XP Purchase Item injection
        int slotXp = plugin.getConfig().getInt("upgrade.xp.slots.item", 22);
        String xpTitle = plugin.getConfig().getString("upgrade.xp.title","&d경험치 구매");
        java.util.List<String> xpLore = plugin.getConfig().getStringList("upgrade.xp.lore");
        if (xpLore == null || xpLore.isEmpty()){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            xpLore = new java.util.ArrayList<>();
            xpLore.add("&7구매량: &f{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
amount} XP");
            xpLore.add("&7가격: &a{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
price}");
            xpLore.add("&8클릭: 구매");
        }
        String[] xpLoreArr = new String[xpLore.size()];
        long amount = plugin.getConfig().getLong("upgrade.xp.amount", 100);
        long price  = plugin.getConfig().getLong("upgrade.xp.price", 1000);
        for (int i=0;i<xpLore.size();i++){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            xpLoreArr[i] = xpLore.get(i).replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
amount}", String.valueOf(amount)).replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
price}", String.valueOf(price));
        }
        inv.setItem(slotXp, named(new org.bukkit.inventory.ItemStack(org.bukkit.Material.EXPERIENCE_BOTTLE), xpTitle, xpLoreArr));
        p.openInventory(inv);
    }

    public void click(Player p, int slot, boolean shift){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

        IslandData is = store.getOrCreate(p.getUniqueId(), p.getName());
        if (slot == plugin.getConfig().getInt("upgrade.gui.slots.size", 14)){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            int before = is.getSize();
            int next   = nextSizeValue(before);
            int need   = needLevelSize(before);
            if (is.getLevel() < need){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

                String msg = plugin.getConfig().getString("messages.upgrade.not-enough-level","레벨 부족").replace("<need>", String.valueOf(need));
                p.sendMessage(msg);
                return;
            }
            double cost = costSize(before);
            if (!vault.withdraw(p.getName(), cost)){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

                String msg = plugin.getConfig().getString("messages.upgrade.not-enough-money","돈 부족").replace("<cost>", String.valueOf((long)cost));
                p.sendMessage(msg);
                return;
            }
            is.setSize(next);
            store.save();
            p.sendMessage(plugin.getConfig().getString("messages.upgrade.size-success","섬 크기 업그레이드").replace("<radius>", String.valueOf(next)));
            try {

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
 if (bento != null && bento.isEnabled()) {

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
 bento.applyRangeInstant(p, next); } } catch (Throwable t){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
 plugin.getLogger().warning("BentoSync range failed: " + t.getMessage()); }
            if (plugin.getConfig().getBoolean("upgrade.sync.bento.range", false)){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

                try {

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
 Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "is admin range " + p.getName() + " " + next); } catch (Throwable ignored){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
}
            }
        } else if (slot == plugin.getConfig().getInt("upgrade.gui.slots.team", 12)){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            int before = is.getTeamMax();
            int next   = nextTeamValue(before);
            int need   = needLevelTeam(before);
            if (is.getLevel() < need){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

                String msg = plugin.getConfig().getString("messages.upgrade.not-enough-level","레벨 부족").replace("<need>", String.valueOf(need));
                p.sendMessage(msg);
                return;
            }
            double cost = costTeam(before);
            if (!vault.withdraw(p.getName(), cost)){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

                String msg = plugin.getConfig().getString("messages.upgrade.not-enough-money","돈 부족").replace("<cost>", String.valueOf((long)cost));
                p.sendMessage(msg);
                return;
            }
            is.setTeamMax(next);
            store.save();
            p.sendMessage(plugin.getConfig().getString("messages.upgrade.team-success","섬 인원 업그레이드").replace("<max>", String.valueOf(next)));
            try {

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

                if (bento != null && bento.isEnabled()){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

                    bento.applyTeamMax(p, next);
                }
            } catch (Throwable t){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

                plugin.getLogger().warning("BentoSync team-size failed: " + t.getMessage());
            }
        }
    
                if (slot == plugin.getConfig().getInt("upgrade.xp.slots.item", 22)){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

                    long buyAmount = plugin.getConfig().getLong("upgrade.xp.amount", 50);
                    double buyCost = plugin.getConfig().getDouble("upgrade.xp.cost", 1000.0);
                    if (!vault.withdraw(p.getName(), buyCost)){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

                        p.sendMessage(Text.color(plugin.getConfig().getString("messages.not-enough-money","&c잔액이 부족합니다. 필요: &f<cost>").replace("<cost>", String.valueOf((long)buyCost))));
                        return;
                    }
                    level.applyXpPurchase(is, buyAmount);
                    store.save();
                    long needXp = level.requiredXpForLevel(is.getLevel());
                    long remain = Math.max(0, needXp - is.getXp());
                    p.sendMessage(Text.color(plugin.getConfig().getString("messages.xp.bought","&a경험치 &f<amount> &a를 구매했습니다. 남은 필요치: &e<remain>")
                        .replace("<amount>", String.valueOf(buyAmount))
                        .replace("<remain>", String.valueOf(Math.max(0, remain)))));
                    return;
                }
            
                if (slot == plugin.getConfig().getInt("upgrade.gui.slots.mine", 24)){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

                    plugin.getFeatures().upgradeMine(p);
                    return;
                }
                if (slot == plugin.getConfig().getInt("upgrade.gui.slots.farm", 25)){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

                    plugin.getFeatures().upgradeFarm(p);
                    return;
                }
}

    
    /* ===================== Helpers & math ===================== */

    private ItemStack named(ItemStack base, String name, String[] lore){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

        ItemMeta im = base.getItemMeta();
        im.setDisplayName(com.signition.samskybridge.util.Text.color(name));
        List<String> lc = new ArrayList<>();
        for (String s : lore) lc.add(com.signition.samskybridge.util.Text.color(s));
        im.setLore(lc);
        base.setItemMeta(im);
        return base;
    }

    // SIZE
    private int baseRange(){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
 return plugin.getConfig().getInt("upgrade.size.base-range", 50); }
    private int perRange(){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
 return plugin.getConfig().getInt("upgrade.size.per-level", 10); }
    private double costSize(int current){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

        List<Integer> steps = plugin.getConfig().getIntegerList("upgrade.size.steps");
        int idx;
        if (steps != null && !steps.isEmpty()){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            idx = Math.max(0, steps.indexOf(current));
        } else {

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            idx = Math.max(0, (current - baseRange()) / Math.max(1, perRange()));
        }
        double base = plugin.getConfig().getDouble("upgrade.size.cost-base", 20000.0);
        double mul  = plugin.getConfig().getDouble("upgrade.size.cost-multiplier", 1.25);
        return base * Math.pow(mul, idx);
    }
    private int needLevelSize(int current){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

        List<Integer> steps = plugin.getConfig().getIntegerList("upgrade.size.steps");
        int idx;
        if (steps != null && !steps.isEmpty()){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            idx = Math.max(0, steps.indexOf(current));
        } else {

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            idx = Math.max(0, (current - baseRange()) / Math.max(1, perRange()));
        }
        int base = plugin.getConfig().getInt("upgrade.size.required-level-base", 2);
        int step = plugin.getConfig().getInt("upgrade.size.required-level-step", 2);
        return base + idx * step;
    }
    private int nextSizeValue(int current){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

        List<Integer> steps = plugin.getConfig().getIntegerList("upgrade.size.steps");
        if (steps != null && !steps.isEmpty()){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            int idx = steps.indexOf(current);
            if (idx >= 0 && idx+1 < steps.size()) return steps.get(idx+1);
            return current;
        }
        return current + perRange();
    }
    private int currentSizeStep(int value){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

        List<Integer> steps = plugin.getConfig().getIntegerList("upgrade.size.steps");
        if (steps != null && !steps.isEmpty()){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            int idx = steps.indexOf(value);
            return (idx>=0? idx+1 : 1);
        }
        if (value < baseRange()) return 1;
        return 1 + (value - baseRange()) / Math.max(1, perRange());
    }
    private int maxSizeStep(){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

        List<Integer> steps = plugin.getConfig().getIntegerList("upgrade.size.steps");
        if (steps != null && !steps.isEmpty()) return steps.size();
        return 1000;
    }

    // TEAM
    private int baseMembers(){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
 return plugin.getConfig().getInt("upgrade.team.base-members", 2); }
    private int perMembers(){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
 return plugin.getConfig().getInt("upgrade.team.per-level", 1); }
    private double costTeam(int current){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

        List<Integer> steps = plugin.getConfig().getIntegerList("upgrade.team.steps");
        int idx;
        if (steps != null && !steps.isEmpty()){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            idx = Math.max(0, steps.indexOf(current));
        } else {

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            idx = Math.max(0, (current - baseMembers()) / Math.max(1, perMembers()));
        }
        double base = plugin.getConfig().getDouble("upgrade.team.cost-base", 15000.0);
        double mul  = plugin.getConfig().getDouble("upgrade.team.cost-multiplier", 1.35);
        return base * Math.pow(mul, idx);
    }
    private int needLevelTeam(int current){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

        List<Integer> steps = plugin.getConfig().getIntegerList("upgrade.team.steps");
        int idx;
        if (steps != null && !steps.isEmpty()){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            idx = Math.max(0, steps.indexOf(current));
        } else {

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            idx = Math.max(0, (current - baseMembers()) / Math.max(1, perMembers()));
        }
        int base = plugin.getConfig().getInt("upgrade.team.required-level-base", 2);
        int step = plugin.getConfig().getInt("upgrade.team.required-level-step", 2);
        return base + idx * step;
    }
    private int nextTeamValue(int current){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

        List<Integer> steps = plugin.getConfig().getIntegerList("upgrade.team.steps");
        if (steps != null && !steps.isEmpty()){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            int idx = steps.indexOf(current);
            if (idx >= 0 && idx+1 < steps.size()) return steps.get(idx+1);
            return current;
        }
        return current + perMembers();
    }
    private int currentTeamStep(int value){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

        List<Integer> steps = plugin.getConfig().getIntegerList("upgrade.team.steps");
        if (steps != null && !steps.isEmpty()){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

            int idx = steps.indexOf(value);
            return (idx>=0? idx+1 : 1);
        }
        if (value < baseMembers()) return 1;
        return 1 + (value - baseMembers()) / Math.max(1, perMembers());
    }
    private int maxTeamStep(){

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

        List<Integer> steps = plugin.getConfig().getIntegerList("upgrade.team.steps");
        if (steps != null && !steps.isEmpty()) return steps.size();
        return 1000;
    }


// --- injected: upgrades-ui-lore-and-owner-check ---

private void appendNextLevelLore(org.bukkit.inventory.meta.ItemMeta meta, String typeKey, int nextLevel) {

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

    if (meta == null) return;
    if (!this.plugin.getConfig().getBoolean("upgrades-ui.show-next-level-lore", true)) return;
    org.bukkit.configuration.ConfigurationSection lvl = this.plugin.getConfig()
        .getConfigurationSection("upgrades." + typeKey + ".levels." + nextLevel);
    if (lvl == null) return;
    java.util.List<String> lore = meta.hasLore() ? meta.getLore() : new java.util.ArrayList<String>();
    lore.add(Text.color(this.plugin.getConfig().getString("upgrades-ui.templates." + typeKey + ".next-header", "&e다음 레벨 효과:")));
    java.util.List<String> add = lvl.getStringList("lore");
    for (String line : add) lore.add(Text.color(line));
    meta.setLore(lore);
}
private void appendCurrentLevelLine(org.bukkit.inventory.meta.ItemMeta meta, String typeKey, int curLevel) {

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

    if (meta == null) return;
    String fmt = this.plugin.getConfig().getString("upgrades-ui.templates." + typeKey + ".current", "&7현재 레벨: &f{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
level}");
    String line = fmt.replace("{

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}
level}", String.valueOf(curLevel));
    java.util.List<String> lore = meta.hasLore() ? meta.getLore() : new java.util.ArrayList<String>();
    lore.add(Text.color(line));
    meta.setLore(lore);
}
private boolean isIslandOwner(org.bukkit.entity.Player p) {

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

    try {

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

        return plugin.getFeatureService().islandOwner(p);
    } catch (Throwable t) {

// GUI lore mapping helpers (support both new and legacy paths)
private List<String> readLoreTemplate(String feature){
    // Primary: upgrades.<feature>.gui.lore-template (List<String>)
    String base = "upgrades."+feature+".gui.lore-template";
    if (this.plugin.getConfig().isList(base)){
        return this.plugin.getConfig().getStringList(base);
    }
    // Fallback legacy: upgrades-ui.<feature>.lore (List<String>)
    String legacy = "upgrades-ui."+feature+".lore";
    if (this.plugin.getConfig().isList(legacy)){
        return this.plugin.getConfig().getStringList(legacy);
    }
    return java.util.Collections.emptyList();
}
private String[] readOreTableFormat(String feature){
    // A: upgrades.<feature>.gui.next-ore-table-{title|line|max-lines}
    String t1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-title", "&8- &7광물 테이블:");
    String l1 = this.plugin.getConfig().getString("upgrades."+feature+".gui.next-ore-table-line", "&8  • &7{mat}&7: &a{chance}%");
    int m1 = this.plugin.getConfig().getInt("upgrades."+feature+".gui.next-ore-table-max-lines", 12);
    // B: upgrades.<feature>.gui-ore-table.{title|line|max-lines}
    String t2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.title", t1);
    String l2 = this.plugin.getConfig().getString("upgrades."+feature+".gui-ore-table.line", l1);
    int m2 = this.plugin.getConfig().getInt("upgrades."+feature+".gui-ore-table.max-lines", m1);
    return new String[]{t2, l2, String.valueOf(m2)};
}
private List<String> renderNextOreTable(int nextLevel){
    Map<org.bukkit.Material,Integer> map = this.features.getOreWeights(nextLevel);
    if (map == null || map.isEmpty()) return java.util.Collections.emptyList();
    String[] fmt = readOreTableFormat("mine");
    String title = fmt[0];
    String line = fmt[1];
    int maxLines = Integer.parseInt(fmt[2]);
    java.util.List<String> out = new java.util.ArrayList<>();
    out.add(title);
    int i=0;
    for (java.util.Map.Entry<org.bukkit.Material,Integer> e : map.entrySet()){
        if (i++ >= maxLines) break;
        String s = line.replace("{mat}", e.getKey().name()).replace("{chance}", String.valueOf(e.getValue()));
        out.add(s);
    }
    return out;
}
private List<String> applyLorePlaceholders(List<String> tpl, int now, int max, int needLevel, String cost, String nextBonus, int nextRegen, int nextLevel){
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String s : tpl){
        String r = s.replace("{now}", String.valueOf(now))
                    .replace("{max}", String.valueOf(max))
                    .replace("{need}", String.valueOf(needLevel))
                    .replace("{cost}", String.valueOf(cost))
                    .replace("{nextBonus}", String.valueOf(nextBonus))
                    .replace("{nextRegen}", String.valueOf(nextRegen));
        if (r.contains("{nextOreTable}")){
            out.addAll(renderNextOreTable(nextLevel));
        } else {
            out.add(r);
        }
    }
    return out;
}

        return p.hasPermission("samskybridge.island.owner");
    }
}

}
