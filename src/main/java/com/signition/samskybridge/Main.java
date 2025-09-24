package com.signition.samskybridge;

import com.signition.samskybridge.cmd.IslandCommand;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.listener.JoinListener;
import com.signition.samskybridge.listener.GuiListener;
import com.signition.samskybridge.listener.PortalBlocker;
import com.signition.samskybridge.rank.RankingService;
import com.signition.samskybridge.market.MarketService;
import com.signition.samskybridge.integration.BentoSync;
import com.signition.samskybridge.upgrade.UpgradeService;
import com.signition.samskybridge.util.Text;
import com.signition.samskybridge.util.VaultHook;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private com.signition.samskybridge.feature.FeatureService features;
    private static Main inst;
    private DataStore dataStore;
    private LevelService levelService;
    private UpgradeService upgradeService;
    private RankingService rankingService;
    private VaultHook vault;
    private BentoSync bento;

    @Override
    public void onEnable(){
        // Island Chat listeners (soft, no behavior change unless used)
        try {
            com.signition.samskybridge.chat.IslandChatService _ichat = new com.signition.samskybridge.chat.IslandChatService(this);
            getServer().getPluginManager().registerEvents(_ichat, this);
            getServer().getPluginManager().registerEvents(new com.signition.samskybridge.chat.IslandChatCommandHook(this, _ichat), this);
        } catch (Throwable _t) { getLogger().warning("IslandChat hook skipped: " + _t.getMessage()); }

        inst = this;
        saveDefaultConfig();
        saveResource("messages_ko.yml", false);

        this.vault = new VaultHook(this);
        this.bento = new BentoSync(this);
        this.dataStore = new DataStore(this);
        features = new com.signition.samskybridge.feature.FeatureService(this, dataStore);
        this.levelService = new LevelService(this, dataStore);
        this.upgradeService = new UpgradeService(this, dataStore, levelService, vault);
        this.rankingService = new RankingService(this, dataStore, levelService);

        getCommand("섬").setExecutor(new IslandCommand(this, dataStore, levelService, upgradeService, rankingService));

        Bukkit.getPluginManager().registerEvents(new GuiListener(this, upgradeService), this);
        Bukkit.getPluginManager().registerEvents(new com.signition.samskybridge.listener.FeatureListener(this, features), this);
        Bukkit.getPluginManager().registerEvents(new PortalBlocker(this), this);
        Bukkit.getPluginManager().registerEvents(new JoinListener(this, dataStore, bento), this);

        long ticks = getConfig().getLong("rank-refresh-interval-ticks", 1200L);
        Bukkit.getScheduler().runTaskTimer(this, () -> rankingService.refreshRanking(), 60L, ticks);

        long saveTicks = getConfig().getLong("save-interval-ticks", 1200L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> dataStore.save(), 100L, saveTicks);

        getLogger().info(Text.color("&aSamSkyBridge enabled."));
    }

    @Override
    public void onDisable() {
        try { dataStore.save(); } catch (Exception ignored) {}
    }

    public static Main get(){ return inst; }
    public VaultHook getVault(){ return vault; }
    public BentoSync getBento(){ return bento; }
    public com.signition.samskybridge.data.DataStore getDataStore(){ return dataStore; }
    
    public com.signition.samskybridge.feature.FeatureService getFeatures(){ return features; }
}