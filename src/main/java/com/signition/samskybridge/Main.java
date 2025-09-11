
package com.signition.samskybridge;

import com.signition.samskybridge.cmd.IslandCommand;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.rank.RankingService;
import com.signition.samskybridge.listener.BlockXPListener;
import com.signition.samskybridge.listener.DropTagListener;
import com.signition.samskybridge.place.PlacedTracker;
import com.signition.samskybridge.listener.ChatListener;
import com.signition.samskybridge.listener.GuiListener;
import com.signition.samskybridge.listener.JoinListener;
import org.bukkit.plugin.java.JavaPlugin;

import com.signition.samskybridge.tab.TabPrefixRefresher;
import com.signition.samskybridge.tab.TabPrefixManager;

public final class Main extends JavaPlugin {
  private DataStore dataStore;
  private LevelService levelService;
  private RankingService rankingService;
  private GuiListener guiListener;
  private PlacedTracker placedTracker;
  private TabPrefixRefresher tab;
  private TabPrefixManager tabMgr;

  @Override
  public void onEnable(){
    saveDefaultConfig();
        com.signition.samskybridge.util.Configs.ensureDefaults(this);
    this.dataStore = new DataStore(this);
    this.levelService = new LevelService(this, dataStore);
    this.rankingService = new RankingService(this, dataStore);

    IslandCommand cmd = new IslandCommand(this, dataStore, levelService, rankingService);
    getCommand("섬").setExecutor(cmd);
    getCommand("섬").setTabCompleter(cmd);

    this.guiListener = new GuiListener(this);
        this.placedTracker = new com.signition.samskybridge.place.PlacedTracker();
        this.placedTracker = new PlacedTracker();
    getServer().getPluginManager().registerEvents(new com.signition.samskybridge.listener.BlockXPListener(this, dataStore, levelService, placedTracker), this);
        getServer().getPluginManager().registerEvents(new com.signition.samskybridge.listener.DropTagListener(this, placedTracker), this);
        getServer().getPluginManager().registerEvents(new DropTagListener(this, placedTracker), this);
    getServer().getPluginManager().registerEvents(new ChatListener(this, dataStore), this);
    getServer().getPluginManager().registerEvents(guiListener, this);
    getServer().getPluginManager().registerEvents(new JoinListener(this), this);

    // schedule ranking refresh
    int ticks = getConfig().getInt("ranking.refresh-ticks", 6000);
    getServer().getScheduler().runTaskTimer(this, new Runnable(){ public void run(){ rankingService.refresh(); }}, ticks, ticks);
        // Tab prefix refresher
        if (getConfig().getBoolean("tab_prefix.force", false)){
            this.tab = new TabPrefixRefresher(this);
            String fmt = getConfig().getString("tab_prefix.format", "&7[ &a섬 &7] &r");
            int refresh = getConfig().getInt("tab_prefix.refresh_ticks", 200);
            tab.start(fmt, refresh);
            getLogger().info("[TabPrefix] enabled");
            // Dynamic manager with <rank>/<level>
            String fmt2 = getConfig().getString("tab_prefix.format_dynamic", "&7[ &a섬 랭킹 <rank>위 &7| &fLv.&b<level> &7]");
            this.tabMgr = new TabPrefixManager(this, dataStore, rankingService);
            tabMgr.start(fmt2, refresh);
        } else { try { org.bukkit.scoreboard.Scoreboard b = org.bukkit.Bukkit.getScoreboardManager().getMainScoreboard(); org.bukkit.scoreboard.Team t=b.getTeam("samsky_all"); if(t!=null){ t.setPrefix(""); t.unregister(); } } catch(Throwable ignore){} getLogger().info("[TabPrefix] disabled"); }
  }

  @Override
  public void onDisable(){
        if (tab != null) tab.stop();
    if (tabMgr != null) tabMgr.stop();
    if (dataStore != null) dataStore.save();
  }

  public DataStore getDataStore(){ return dataStore; }
  public LevelService getLevelService(){ return levelService; }
  public RankingService getRankingService(){ return rankingService; }
  public GuiListener getGuiListener(){ return guiListener; }
}