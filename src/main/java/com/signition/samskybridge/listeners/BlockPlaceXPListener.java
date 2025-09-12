
package com.signition.samskybridge.listeners;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.xpguard.RecycleGuardService;
import com.signition.samskybridge.xpguard.SlotGuardService;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.*;

public final class BlockPlaceXPListener implements Listener {

    private final Plugin plugin;
    private final RecycleGuardService recycle;
    private final SlotGuardService slots;

    private final boolean enabled;
    private final List<String> worlds;
    private final boolean denyCreative;
    private final boolean denyCancelled;
    private final Map<Material, Double> table = new EnumMap<>(Material.class);
    private final double def;
    private final boolean recycleEnabled;

    private static final NamespacedKey KEY_LVL = new NamespacedKey(Main.get(), "lvl.level");
    private static final NamespacedKey KEY_EXP = new NamespacedKey(Main.get(), "lvl.exp");

    public BlockPlaceXPListener(Plugin plugin, RecycleGuardService recycle, SlotGuardService slots){
        this.plugin = plugin;
        this.recycle = recycle;
        this.slots = slots;

        this.enabled = plugin.getConfig().getBoolean("xp.enabled", true);
        this.worlds = plugin.getConfig().getStringList("xp.allowed-worlds");
        this.denyCreative = plugin.getConfig().getBoolean("xp.deny-creative", true);
        this.denyCancelled = plugin.getConfig().getBoolean("xp.deny-when-cancelled", true);
        this.def = plugin.getConfig().getDouble("xp.default", 0.0D);
        this.recycleEnabled = plugin.getConfig().getBoolean("guards.recycle.enabled", true);

        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("xp.blocks");
        if (sec != null){
            for (String k : sec.getKeys(false)){
                Material m = Material.matchMaterial(k);
                if (m != null){
                    table.put(m, sec.getDouble(k));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e){
        if (!enabled) return;
        if (denyCancelled && e.isCancelled()) return;
        Player p = e.getPlayer();
        if (p == null) return;
        if (worlds != null && !worlds.isEmpty() && !worlds.contains(p.getWorld().getName())) return;
        if (denyCreative && (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR)) return;

        Material type = e.getBlockPlaced().getType();
        double amt = table.getOrDefault(type, def);
        if (amt <= 0.0D) return;

        // Deny recycled one-time
        if (recycleEnabled && recycle.consume(p.getUniqueId(), type)){
            // still mark slot so repeated placements at same slot don't chain
            slots.mark(e.getBlockPlaced().getLocation());
            return;
        }

        // Guard same slot spam (optional)
        if (slots.already(e.getBlockPlaced().getLocation())) {
            return;
        }

        // Award and mark
        addExp(p, amt);
        slots.mark(e.getBlockPlaced().getLocation());
    }

    public static int getStoredLevel(Player p){
        PersistentDataContainer c = p.getPersistentDataContainer();
        Integer v = c.get(KEY_LVL, PersistentDataType.INTEGER);
        if (v == null){
            c.set(KEY_LVL, PersistentDataType.INTEGER, 1);
            return 1;
        }
        return Math.max(1, v);
    }
    public static int getStoredExp(Player p){
        PersistentDataContainer c = p.getPersistentDataContainer();
        Integer v = c.get(KEY_EXP, PersistentDataType.INTEGER);
        if (v == null) return 0;
        return Math.max(0, v);
    }
    public static void setStoredLevel(Player p, int lvl){
        p.getPersistentDataContainer().set(KEY_LVL, PersistentDataType.INTEGER, Math.max(1, lvl));
    }
    public static void setStoredExp(Player p, int exp){
        p.getPersistentDataContainer().set(KEY_EXP, PersistentDataType.INTEGER, Math.max(0, exp));
    }

    private int requiredForNext(Player p){
        // simplified growth using config 'leveling' section if present
        int base = plugin.getConfig().getInt("leveling.base", 100);
        String mode = plugin.getConfig().getString("leveling.growth.mode", "exponential");
        double percent = plugin.getConfig().getDouble("leveling.growth.percent", 20.0D);
        int per = plugin.getConfig().getInt("leveling.growth.per-level", 25);
        int lvl = getStoredLevel(p);
        if ("linear".equalsIgnoreCase(mode)){
            return Math.max(1, base + (lvl - 1) * per);
        }else{
            double factor = Math.pow(1.0 + (percent/100.0), (lvl - 1));
            return Math.max(1, (int)Math.round(base * factor));
        }
    }

    private void addExp(Player p, double amount){
        int add = (int)Math.round(amount);
        if (add <= 0) return;
        int exp = getStoredExp(p) + add;
        int lvl = getStoredLevel(p);
        int need = requiredForNext(p);
        boolean carry = plugin.getConfig().getBoolean("leveling.carry-over", true);
        int max = plugin.getConfig().getInt("leveling.max-level", 100);

        while (exp >= need && lvl < max){
            exp = carry ? (exp - need) : 0;
            lvl += 1;
            try {
                p.sendMessage("§a[XP] Level Up! §f" + (lvl-1) + " §7→ §e" + lvl);
                p.playSound(p.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
            } catch (Throwable ignored){}
            setStoredLevel(p, lvl);
            need = requiredForNext(p);
        }
        setStoredLevel(p, lvl);
        setStoredExp(p, exp);
    }

    // Expose for command
    public static int requiredForNext(Player p){
        return new BlockPlaceXPListener(Main.get(), new RecycleGuardService(), new SlotGuardService()).requiredForNext(p);
    }

    // Unified requiredForNext: public static, reads config from Main.get()
    public static int requiredForNext(org.bukkit.entity.Player p){
        com.signition.samskybridge.Main plugin = com.signition.samskybridge.Main.get();
        if (plugin == null) return 1;
        int base = plugin.getConfig().getInt("leveling.base", 100);
        String mode = plugin.getConfig().getString("leveling.growth.mode", "exponential");
        double percent = plugin.getConfig().getDouble("leveling.growth.percent", 20.0D);
        int per = plugin.getConfig().getInt("leveling.growth.per-level", 25);
        int lvl = getStoredLevel(p);
        if ("linear".equalsIgnoreCase(mode)){
            return Math.max(1, base + (lvl - 1) * per);
        } else {
            double factor = Math.pow(1.0 + (percent/100.0), (lvl - 1));
            return Math.max(1, (int)Math.round(base * factor));
        }
    }

}
