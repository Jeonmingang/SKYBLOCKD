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
import com.signition.samskybridge.nametag.NametagBridge;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

public class Main extends JavaPlugin {
    private com.signition.samskybridge.util.VaultHook vaultHook;

    private com.signition.samskybridge.nametag.NametagBridge nametagBridge;

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
    public void onEnable(){                if (this.levelService == null) this.levelService = new com.signition.samskybridge.level.LevelService(this);
                if (this.vaultHook == null) this.vaultHook = new com.signition.samskybridge.util.VaultHook(this);

        getLogger().info(Text.color("&aSamSkyBridge enabled."));
    
// --- Minimal wiring (listener registration + tablist scheduler), no feature changes ---
try {
    if (this.dataStore == null) this.dataStore = new com.signition.samskybridge.data.DataStore(this);
} catch (Throwable ignored){}
try {
    if (this.upgradeService == null) this.upgradeService = new com.signition.samskybridge.upgrade.UpgradeService(this, this.dataStore, this.levelService, this.vaultHook, this.bento);
} catch (Throwable ignored){}
try {
    if (this.bento == null) this.bento = new com.signition.samskybridge.integration.BentoSync(this);
} catch (Throwable ignored){}

pm = getServer().getPluginManager();
try { pm.registerEvents(new com.signition.samskybridge.listener.JoinListener(this, this.dataStore, this.bento), this); } catch (Throwable ignored) {}
try { pm.registerEvents(new com.signition.samskybridge.listener.GuiListener(this, this.upgradeService), this); } catch (Throwable ignored) {}
try { pm.registerEvents(new com.signition.samskybridge.listener.PortalBlocker(this), this); } catch (Throwable ignored) {}
try {
    if (this.tablistService == null) this.tablistService = new com.signition.samskybridge.tab.TablistService(this, this.bento);
    pm.registerEvents(this.tablistService, this);
    this.tablistService.start();
} catch (Throwable ignored) {}
// --- end wiring ---

// --- NametagBridge wiring (minimal, config-driven) ---
try {
    if (this.nametagBridge == null) this.nametagBridge = new com.signition.samskybridge.nametag.NametagBridge(this, this.bento);
    pm = getServer().getPluginManager();
    pm.registerEvents(this.nametagBridge, this);
    this.nametagBridge.start();
} catch (Throwable ignored) {}
// --- end NametagBridge wiring ---
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
