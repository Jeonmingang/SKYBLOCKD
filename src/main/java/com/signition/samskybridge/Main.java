package com.signition.samskybridge;
import com.signition.samskybridge.cmd.IslandCommand;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.rank.RankingService;
import com.signition.samskybridge.listener.BlockXPListener;
import com.signition.samskybridge.listener.ChatListener;
import com.signition.samskybridge.listener.GuiListener;
import com.signition.samskybridge.listener.JoinListener;
import com.signition.samskybridge.listener.ManagementListener;
import com.signition.samskybridge.listener.MarketListener;
import org.bukkit.plugin.java.JavaPlugin;
public final class Main extends JavaPlugin {
  private static Main instance; public static Main get(){ return instance; }
  private DataStore dataStore; private LevelService levelService; private RankingService rankingService;
  @Override public void onEnable(){
    instance = this; saveDefaultConfig();
    this.dataStore = new DataStore(this);
    this.levelService = new LevelService(this, dataStore);
    this.rankingService = new RankingService(this, dataStore);
    IslandCommand cmd = new IslandCommand(this, dataStore, levelService, rankingService);
    if (getCommand("섬") != null){ getCommand("섬").setExecutor(cmd); getCommand("섬").setTabCompleter(cmd); }
    getServer().getPluginManager().registerEvents(new JoinListener(), this);
    getServer().getPluginManager().registerEvents(new BlockXPListener(this, levelService), this);
    getServer().getPluginManager().registerEvents(new GuiListener(this, dataStore), this);
    getServer().getPluginManager().registerEvents(new ChatListener(this, dataStore), this);
    getServer().getPluginManager().registerEvents(new ManagementListener(this, dataStore), this);
    getServer().getPluginManager().registerEvents(new MarketListener(this, dataStore), this);
    getLogger().info("SamSkyBridge enabled (roles/market/pixelmon XP).");
  }
  @Override public void onDisable(){ if (dataStore!=null) dataStore.save(); }
  public DataStore getDataStore(){ return dataStore; }
  public LevelService getLevelService(){ return levelService; }
  public RankingService getRankingService(){ return rankingService; }
}