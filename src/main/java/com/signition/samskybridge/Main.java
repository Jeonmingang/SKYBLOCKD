
package com.signition.samskybridge;

import com.signition.samskybridge.cmd.IslandCommand;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.rank.RankingService;
import com.signition.samskybridge.listener.BlockXPListener;
import com.signition.samskybridge.listener.ChatListener;
import com.signition.samskybridge.listener.GuiListener;
import com.signition.samskybridge.listener.JoinListener;
import com.signition.samskybridge.util.Configs;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private DataStore dataStore;
    private LevelService levelService;
    private RankingService rankingService;
    private GuiListener guiListener;

    @Override
    public void onEnable(){
        saveDefaultConfig();
        this.dataStore = new DataStore(this);
        this.levelService = new LevelService(this, dataStore);
        this.rankingService = new RankingService(this, dataStore);
        this.guiListener = new GuiListener(this);

        // commands
        if (getCommand("섬") != null) {
            getCommand("섬").setExecutor(new IslandCommand(this, dataStore));
        }

        // listeners
        getServer().getPluginManager().registerEvents(new BlockXPListener(this, dataStore, levelService), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(guiListener, this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);

        // ranking refresh task
        int ticks = getConfig().getInt("ranking.refresh-ticks", 6000);
        getServer().getScheduler().runTaskTimer(this, new Runnable(){
            @Override public void run(){ rankingService.refresh(); }
        }, ticks, ticks);
    }

    @Override
    public void onDisable(){
        if (dataStore != null) dataStore.save();
    }

    public DataStore getDataStore(){ return dataStore; }
    public LevelService getLevelService(){ return levelService; }
    public RankingService getRankingService(){ return rankingService; }
    public GuiListener getGuiListener(){ return guiListener; }
}
