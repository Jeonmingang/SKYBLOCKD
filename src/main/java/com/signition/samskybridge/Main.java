
package com.signition.samskybridge;

import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.listener.BlockXPListener;
import com.signition.samskybridge.listener.DropPickupTracker;
import com.signition.samskybridge.listener.JoinListener;
import com.signition.samskybridge.rank.RankingService;
import com.signition.samskybridge.util.Configs;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

public class Main extends JavaPlugin {

    private DataStore dataStore;
    private LevelService levelService;
    private RankingService rankingService;

    private DropPickupTracker dropPickupTracker;
    private BlockXPListener blockXPListener;
    private JoinListener joinListener;

    @Override
    public void onEnable() {
        
        
        final org.bukkit.plugin.PluginManager pm = getServer().getPluginManager();
// Load / ensure config
        saveDefaultConfig();
        Configs.ensureDefaults(this);

        this.dataStore = new DataStore(this);
        this.levelService = new LevelService(this, dataStore);
        this.rankingService = new RankingService(this, dataStore);

        this.dropPickupTracker = new DropPickupTracker(this);
        this.blockXPListener = new BlockXPListener(this, dataStore, levelService, dropPickupTracker);
        this.joinListener = new JoinListener(this, rankingService, levelService);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(dropPickupTracker, this);
        pm.registerEvents(blockXPListener, this);
        pm.registerEvents(joinListener, this);

        getLogger().info(Text.col("&a[SamSkyBridge] enabled."));
    }

    @Override
    public void onDisable() {
        if (dataStore != null) dataStore.save();
        getLogger().info(Text.col("&c[SamSkyBridge] disabled."));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("섬")) {
            if (args.length >= 1 && args[0].equalsIgnoreCase("리로드")) {
                reloadConfig();
                Configs.ensureDefaults(this);
                levelService.reload();
                sender.sendMessage(Text.col("&a[SamSkyBridge] 설정을 리로드했습니다."));
                return true;
            }
            sender.sendMessage(Text.col("&e사용법: /섬 리로드"));
            return true;
        }
        return false;
    }

    public DataStore getDataStore() { return dataStore; }
    public LevelService getLevelService() { return levelService; }
    public RankingService getRankingService() { return rankingService; }

    public FileConfiguration cfg() { return getConfig(); }
}
