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
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Main extends JavaPlugin {
    private static Main inst;
    private DataStore dataStore;
    private LevelService levelService;
    private UpgradeService upgradeService;
    private RankingService rankingService;
    private VaultHook vault;
    private BentoSync bento;

    @Override
    public void onEnable() {
        inst = this;
        saveDefaultConfig();
        saveResource("blocks.yml", false);
        saveResource("messages_ko.yml", false);

        this.vault = new VaultHook(this);
        this.bento = new BentoSync(this);
        this.dataStore = new DataStore(this);
        this.levelService = new LevelService(this, dataStore);
        this.upgradeService = new UpgradeService(this, dataStore, levelService, vault);
        this.rankingService = new RankingService(this, dataStore, levelService);

        getCommand("섬").setExecutor(new IslandCommand(this, dataStore, levelService, upgradeService, rankingService));

        Bukkit.getPluginManager().registerEvents(new GuiListener(this, upgradeService), this);
        Bukkit.getPluginManager().registerEvents(new PortalBlocker(this), this);
        Bukkit.getPluginManager().registerEvents(new JoinListener(this, dataStore, bento), this);

        long ticks = getConfig().getLong("rank-refresh-interval-ticks", 1200L);
        Bukkit.getScheduler().runTaskTimer(this, () -> rankingService.refreshRanking(), 60L, ticks);

        long saveTicks = getConfig().getLong("save-interval-ticks", 1200L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> dataStore.save(), 100L, saveTicks);

        
        // Optional: startup Bento range/team sync
        if (getConfig().getBoolean("integration.bentobox.startup-sync.range", false)) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                try {
                    for (com.signition.samskybridge.data.IslandData is : dataStore.all()) {
                        OfflinePlayer owner = Bukkit.getOfflinePlayer(is.getId());
                        bento.applyRangeInstant(owner, is.getSize());
                    }
                    getLogger().info("[BentoSync] Startup range sync done.");
                } catch (Throwable t) {
                    getLogger().warning("[BentoSync] Startup range sync failed: " + t.getMessage());
                }
            }, 100L);
        }
        if (getConfig().getBoolean("integration.bentobox.startup-sync.team", false)) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                try {
                    for (Player pl : Bukkit.getOnlinePlayers()) {
                        com.signition.samskybridge.data.IslandData is = dataStore.getOrCreate(pl.getUniqueId(), pl.getName());
                        bento.applyTeamMax(pl, is.getTeamMax());
                    }
                    getLogger().info("[BentoSync] Startup team sync done.");
                } catch (Throwable t) {
                    getLogger().warning("[BentoSync] Startup team sync failed: " + t.getMessage());
                }
            }, 120L);
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
    }