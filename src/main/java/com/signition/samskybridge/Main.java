package com.signition.samskybridge;

import com.signition.samskybridge.cmd.IslandCommand;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.integration.BentoSync;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.market.MarketService;
import com.signition.samskybridge.rank.RankingService;
import com.signition.samskybridge.tab.TablistService;
import com.signition.samskybridge.upgrade.UpgradeService;
import com.signition.samskybridge.util.Text;
import com.signition.samskybridge.util.VaultHook;
import com.signition.samskybridge.feature.FeatureService;
import com.signition.samskybridge.chat.IslandChatService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main inst;
    private VaultHook vault;
    private BentoSync bento;
    private DataStore dataStore;
    private FeatureService features;
    private LevelService levelService;
    private UpgradeService upgradeService;
    private RankingService rankingService;
    private MarketService marketService;
    private IslandChatService chatService;
    private TablistService tablistService;

    public static Main get(){ return inst; }

    @Override
    public void onEnable() {
        inst = this;
        saveDefaultConfig();
        saveResource("messages_ko.yml", false);
        Text.init(this);

        this.vault = new VaultHook(this);
        this.bento = new BentoSync(this);
        this.dataStore = new DataStore(this);
        this.features = new FeatureService(this);
        this.levelService = new LevelService(this, dataStore);
        this.upgradeService = new UpgradeService(this, dataStore, levelService, vault);
        this.rankingService = new RankingService(this, dataStore);
        this.marketService = new MarketService(this);
        this.chatService = new IslandChatService(this);
        this.tablistService = new TablistService(this, bento);

        getCommand("섬").setExecutor(new IslandCommand(this));

        getLogger().info("SamSkyBridge enabled on " + Bukkit.getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().info("SamSkyBridge disabled.");
    }

    public VaultHook getVault(){ return vault; }
    public BentoSync getBento(){ return bento; }
    public DataStore getDataStore(){ return dataStore; }
    public FeatureService getFeatureService(){ return features; }
    public LevelService getLevelService(){ return levelService; }
    public UpgradeService getUpgradeService(){ return upgradeService; }
    public RankingService getRankingService(){ return rankingService; }
    public MarketService getMarketService(){ return marketService; }
    public IslandChatService getChatService(){ return chatService; }
    public TablistService getTablistService(){ return tablistService; }
}
