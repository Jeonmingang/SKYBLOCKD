package com.signition.samskybridge.util;

import com.signition.samskybridge.Main;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.*;

public class Configs {
  public static void ensureDefaults(Main plugin){
    FileConfiguration c = plugin.getConfig();

    if (!c.contains("debug.xp-blocks")) c.set("debug.xp-blocks", false);

    // Worlds where XP is enabled
    if (!c.contains("xp.allowed-worlds"))
      c.set("xp.allowed-worlds", Arrays.asList("bskyblock_world", "BSkyBlock_world"));

    // Ranking display
    if (!c.contains("ranking.unranked-label"))
      c.set("ranking.unranked-label", "등록안됨");
    if (!c.contains("ranking.tab-prefix"))
      c.set("ranking.tab-prefix", "&7[ &a섬 랭킹 &f<rank> &7| &bLv.<level> &7] &r");

    // XP blocks defaults (example values)
    if (!c.isConfigurationSection("xp.blocks")){
      Map<String,Integer> defaults = new LinkedHashMap<String,Integer>();
      defaults.put("minecraft:stone", 1);
      defaults.put("minecraft:cobblestone", 1);
      defaults.put("minecraft:oak_log", 1);
      defaults.put("minecraft:coal_ore", 2);
      defaults.put("minecraft:iron_ore", 3);
      defaults.put("minecraft:gold_ore", 4);
      defaults.put("minecraft:redstone_ore", 2);
      defaults.put("minecraft:lapis_ore", 3);
      defaults.put("minecraft:diamond_ore", 8);
      defaults.put("minecraft:emerald_ore", 10);
      defaults.put("minecraft:obsidian", 3);
      for (Map.Entry<String,Integer> e : defaults.entrySet()){
        c.set("xp.blocks."+e.getKey(), e.getValue());
      }
    }


// Level requirement curve defaults
if (!c.contains("level.base")) c.set("level.base", 100);
if (!c.contains("level.percent-increase")) c.set("level.percent-increase", 10.0); // % per level
if (!c.contains("level.cap")) c.set("level.cap", 9223372036854775807L); // Long.MAX_VALUE

// Upgrade GUI defaults
if (!c.contains("upgrade.gui.slots.team")) c.set("upgrade.gui.slots.team", 12);
if (!c.contains("upgrade.gui.slots.size")) c.set("upgrade.gui.slots.size", 14);

// Team upgrade defaults
if (!c.contains("upgrade.team.title")) c.set("upgrade.team.title", "&b섬 인원 업그레이드");
if (!c.contains("upgrade.team.lore")) c.set("upgrade.team.lore",
        java.util.Arrays.asList("&7현재 한도: &f{current}명",
                                "&7다음 단계: &a{next}명",
                                "&7요구 레벨: &bLv.{reqLevel}",
                                "&7필요 금액: &a{cost}",
                                "&7업그레이드 레벨: &d{step}",
                                "&8클릭: 업그레이드"));
if (!c.contains("upgrade.team.base-members")) c.set("upgrade.team.base-members", 4);
if (!c.contains("upgrade.team.per-level")) c.set("upgrade.team.per-level", 1);
if (!c.contains("upgrade.team.cost-base")) c.set("upgrade.team.cost-base", 15000.0);
if (!c.contains("upgrade.team.cost-multiplier")) c.set("upgrade.team.cost-multiplier", 1.35);
if (!c.contains("upgrade.team.required-level-base")) c.set("upgrade.team.required-level-base", 2);
if (!c.contains("upgrade.team.required-level-step")) c.set("upgrade.team.required-level-step", 2);

// Size upgrade defaults
if (!c.contains("upgrade.size.title")) c.set("upgrade.size.title", "&e섬 크기 업그레이드");
if (!c.contains("upgrade.size.lore")) c.set("upgrade.size.lore",
        java.util.Arrays.asList("&7현재 보호반경: &f{range} 블럭",
                                "&7다음 단계: &a{next} 블럭",
                                "&7요구 레벨: &bLv.{reqLevel}",
                                "&7필요 금액: &a{cost}",
                                "&7업그레이드 레벨: &d{step}",
                                "&8클릭: 업그레이드"));
if (!c.contains("upgrade.size.base-range")) c.set("upgrade.size.base-range", 50);
if (!c.contains("upgrade.size.per-level")) c.set("upgrade.size.per-level", 10);
if (!c.contains("upgrade.size.cost-base")) c.set("upgrade.size.cost-base", 20000.0);
if (!c.contains("upgrade.size.cost-multiplier")) c.set("upgrade.size.cost-multiplier", 1.25);
if (!c.contains("upgrade.size.required-level-base")) c.set("upgrade.size.required-level-base", 2);
if (!c.contains("upgrade.size.required-level-step")) c.set("upgrade.size.required-level-step", 2);

// Custom upgrades example (no dropspeed by default)
if (!c.contains("upgrade.customs.example.title")) c.set("upgrade.customs.example.title", "&d커스텀 업그레이드");
if (!c.contains("upgrade.customs.example.icon")) c.set("upgrade.customs.example.icon", "BOOK");
if (!c.contains("upgrade.customs.example.slot")) c.set("upgrade.customs.example.slot", 22);
if (!c.contains("upgrade.customs.example.lore")) c.set("upgrade.customs.example.lore",
        java.util.Arrays.asList("&7현재: &f{current}",
                                "&7다음: &a{next}",
                                "&7요구 레벨: &bLv.{reqLevel}",
                                "&7필요 금액: &a{cost}",
                                "&7업그레이드 레벨: &d{step}",
                                "&8클릭: 업그레이드"));
if (!c.contains("upgrade.customs.example.base")) c.set("upgrade.customs.example.base", 0);
if (!c.contains("upgrade.customs.example.per")) c.set("upgrade.customs.example.per", 1);
if (!c.contains("upgrade.customs.example.cost-base")) c.set("upgrade.customs.example.cost-base", 10000.0);
if (!c.contains("upgrade.customs.example.cost-multiplier")) c.set("upgrade.customs.example.cost-multiplier", 1.15);
if (!c.contains("upgrade.customs.example.required-level-base")) c.set("upgrade.customs.example.required-level-base", 1);
if (!c.contains("upgrade.customs.example.required-level-step")) c.set("upgrade.customs.example.required-level-step", 1);
if (!c.contains("upgrade.customs.example.on-success.commands"))
    c.set("upgrade.customs.example.on-success.commands",
          java.util.Arrays.asList("say {player} upgraded example to {step}"));

    plugin.saveConfig();
  }
}