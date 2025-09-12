
package com.signition.samskybridge;

import com.signition.samskybridge.listeners.BlockPlaceXPListener;
import com.signition.samskybridge.listeners.BlockBreakRecycleListener;
import com.signition.samskybridge.xpguard.RecycleGuardService;
import com.signition.samskybridge.xpguard.SlotGuardService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private RecycleGuardService recycle;
    private SlotGuardService slots;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        recycle = new RecycleGuardService();
        slots = new SlotGuardService();
        slots.configureSeconds(getConfig().getInt("guards.slot-ttl-seconds", 86400));

        Bukkit.getPluginManager().registerEvents(new BlockPlaceXPListener(this, recycle, slots), this);
        Bukkit.getPluginManager().registerEvents(new BlockBreakRecycleListener(this, recycle, slots), this);

        getLogger().info("[SkyblockdXP] Enabled v" + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().info("[SkyblockdXP] Disabled.");
    }

    public static Main get() { return instance; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String n = cmd.getName().toLowerCase();
        if ("xpreload".equals(n)) {
            reloadConfig();
            slots.configureSeconds(getConfig().getInt("guards.slot-ttl-seconds", 86400));
            sender.sendMessage("§a[SkyblockdXP] config reloaded.");
            return true;
        } else if ("xpx".equals(n)) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Player only.");
                return true;
            }
            Player p = (Player) sender;
            int req = BlockPlaceXPListener.requiredForNext(p);
            int cur = BlockPlaceXPListener.getStoredExp(p);
            int lvl = BlockPlaceXPListener.getStoredLevel(p);
            sender.sendMessage("§b[SkyblockdXP] Lv §e" + lvl + " §7| §f" + cur + "§7/§f" + req);
            return true;
        }
        return false;
    }

    public com.signition.samskybridge.listener.GuiListener getGuiListener(){ return this.guiListener; }

    public com.signition.samskybridge.data.DataStore getDataStore(){ return this.dataStore; }

    public com.signition.samskybridge.ranking.RankingService getRankingService(){ return this.rankingService; }
}
