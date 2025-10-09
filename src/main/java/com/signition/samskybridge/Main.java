package com.signition.samskybridge;

import com.signition.samskybridge.command.IslandCommandRouter;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.info.InfoService;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.listener.GuiListener;
import com.signition.samskybridge.listener.InstantRefreshListener;
import com.signition.samskybridge.rank.RankingService;
import com.signition.samskybridge.rank.RankingUiService;
import com.signition.samskybridge.upgrade.UpgradeService;
import com.signition.samskybridge.util.VaultHook;
import com.signition.samskybridge.util.Text;
import com.signition.samskybridge.chat.IslandChat;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

public class Main extends JavaPlugin {

    private static Main inst;
    private DataStore dataStore;
    private LevelService levelService;
    private UpgradeService upgradeService;
    private RankingUiService rankingUiService;
    private InfoService infoService;
    private RankingService rankingService;
    private VaultHook vault;
    private IslandChat chat;

    public static Main get(){ return inst; }

    @Override
    public void onEnable(){
        inst = this;
        saveDefaultConfig();
        this.vault = new VaultHook(this);
        this.dataStore = new DataStore(this);
        this.levelService = new LevelService(this, dataStore);
        this.upgradeService = new UpgradeService(this, levelService);
                this.rankingService = new RankingService(this, dataStore, levelService);
        this.rankingUiService = new RankingUiService(this, levelService, dataStore);
        try { this.chat = new IslandChat(this, dataStore); } catch (Throwable t){ this.getLogger().warning("IslandChat init failed: " + t.getMessage()); }

        // Register command
        IslandCommandRouter router = new IslandCommandRouter(this);
        if (getCommand("samsky") != null){
            getCommand("samsky").setExecutor(router);
            getCommand("samsky").setTabCompleter(router);
        }
        if (getCommand("섬") != null){
            getCommand("섬").setExecutor(router);
            getCommand("섬").setTabCompleter(router);
        }

        // Register listeners
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new GuiListener(this, upgradeService), this);
        pm.registerEvents(new InstantRefreshListener(this), this);
        this.infoService = new InfoService(this, levelService, dataStore);
        if (chat != null) pm.registerEvents(chat, this);

        getLogger().info("SamSkyBridge enabled.");
    }

    @Override
    public void onDisable(){
        if (dataStore != null) dataStore.save();
        getLogger().info("SamSkyBridge disabled.");
    }

    // === getters (used across services) ===
    public DataStore getDataStore(){ return dataStore; }
    public LevelService getLevelService(){ return levelService; }
    public UpgradeService getUpgradeService(){ return upgradeService; }
    public RankingUiService getRankingUiService(){ return rankingUiService; }
    public RankingService getRankingService(){ return rankingService; }
    public InfoService getInfoService(){ return infoService; }
    public VaultHook getVault(){ return vault; }
    public IslandChat getChat(){ return chat; }
}
