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
    private com.signition.samskybridge.chat.IslandChatService chatService;
    private com.signition.samskybridge.tab.TablistService tablistService;
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
        inst = this;
        saveDefaultConfig();
        saveResource("messages_ko.yml", false);

        this.vault = new VaultHook(this);
        this.bento = new BentoSync(this);
        this.dataStore = new DataStore(this);
        features = new com.signition.samskybridge.feature.FeatureService(this, bento);
        this.levelService = new LevelService(this, dataStore);
        this.upgradeService = new UpgradeService(this, dataStore, levelService, vault);
        this.rankingService = new RankingService(this, dataStore);

        getCommand("섬").setExecutor(new IslandCommand(this));

        // Island chat listeners
        this.chatService = new com.signition.samskybridge.chat.IslandChatService(this);
        Bukkit.getPluginManager().registerEvents(this.chatService, this);
        Bukkit.getPluginManager().registerEvents(new com.signition.samskybridge.chat.IslandChatCommandHook(this.chatService), this);

        Bukkit.getPluginManager().registerEvents(new GuiListener(this, upgradeService), this);
        Bukkit.getPluginManager().registerEvents(new com.signition.samskybridge.listener.FeatureListener(this, features), this);
        Bukkit.getPluginManager().registerEvents(new PortalBlocker(this), this);
        Bukkit.getPluginManager().registerEvents(new JoinListener(this, bento), this);

        long ticks = getConfig().getLong("rank-refresh-interval-ticks", 1200L);
        Bukkit.getScheduler().runTaskTimer(this, () -> rankingService.refreshRanking(), 60L, ticks);

        long saveTicks = getConfig().getLong("save-interval-ticks", 1200L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> dataStore.save(), 100L, saveTicks);

                // Tablist service
        if (getConfig().getBoolean("tablist.enabled", false)){
            this.tablistService = new com.signition.samskybridge.tab.TablistService(this);
            this.tablistService.start();
        }
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

    public com.signition.samskybridge.level.LevelService getLevelService(){ return this.levelService; }
    public com.signition.samskybridge.upgrade.UpgradeService getUpgradeService(){ return this.upgradeService; }
    public com.signition.samskybridge.rank.RankingService getRankingService(){ return this.rankingService; }
    public com.signition.samskybridge.feature.FeatureService getFeatureService(){ return this.features; }
    public com.signition.samskybridge.chat.IslandChatService getChatService(){ return this.chatService; }
    public com.signition.samskybridge.tab.TablistService getTablistService(){ return this.tablistService; }
}
