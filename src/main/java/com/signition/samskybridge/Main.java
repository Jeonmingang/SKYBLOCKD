
package com.signition.samskybridge;

import com.signition.samskybridge.cmd.IslandCommand;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.listener.JoinListener;
import com.signition.samskybridge.listener.BlockXPListener;
import com.signition.samskybridge.listener.GuiListener;
import com.signition.samskybridge.rank.RankingService;
import com.signition.samskybridge.integration.BentoSync;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static Main inst;
    private DataStore dataStore;
    private LevelService levelService;
    private UpgradeService upgradeService;
    private RankingService rankingService;
    private VaultHook vault;
    private BentoSync bento;
    private BarrierService barrierService;
    private boolean isBarrierActive = true;  // Track whether the barrier is active

    @Override
    public void onEnable() {
        instance = this;
        // Initialize services
        this.dataStore = new DataStore();
        this.levelService = new LevelService(dataStore);
        this.upgradeService = new UpgradeService(dataStore);
        this.rankingService = new RankingService(dataStore);
        this.vault = new VaultHook(this);
        this.bento = new BentoSync(this);
        this.barrierService = new BarrierService(dataStore);

        // Register events
        getServer().getPluginManager().registerEvents(new BlockXPListener(dataStore), this);
        getServer().getPluginManager().registerEvents(new GuiListener(), this);
        getServer().getPluginManager().registerEvents(new JoinListener(), this);

        // Register island command
        getCommand("섬").setExecutor(new IslandCommand());

        // Setup island borders based on size
        setupIslandBorders();
    }
    
    private void setupIslandBorders() {
        // Assume we get islandId and newSize from the upgrade process or player data
        String islandId = "island_123"; // Example islandId
        int newSize = 50;  // Example of new island size

        // Update the island border size
        updateIslandBorder(islandId, newSize);
    }

    private void updateIslandBorder(String islandId, int newSize) {
        WorldBorder border = // [removed] WorldBorder constructor was here
        
        // Set new border size based on the upgraded island size
        border.setSize(newSize * 2);  // Example: double the size for the border
        
        // Apply the border to all players within the island
        for (Player player : getServer().getOnlinePlayers()) {
            if (isPlayerInsideIsland(player, islandId)) {
                border.setCenter(player.getLocation());  // Center the border on the player
                border.setColor(255, 255, 255);  // Set border color to white (default)
                player.sendMessage("섬 경계가 " + newSize + "로 확장되었습니다.");
            }
        }
    }

    private boolean isPlayerInsideIsland(Player player, String islandId) {
        // Simulate the check whether the player is inside the island area (for now, always return true)
        return true;
    }

    // Toggle island barrier on/off
    public void toggleBarrier(boolean enable) {
        if (enable) {
            // Enable the island barrier
            isBarrierActive = true;
            // Logic to show the border (this can be WorldBorder related logic)
            getServer().broadcastMessage("섬 방벽이 활성화되었습니다.");
        } else {
            // Disable the island barrier
            isBarrierActive = false;
            // Logic to hide the border (disable border visually)
            getServer().broadcastMessage("섬 방벽이 비활성화되었습니다.");
        }
    }
    
    // Functionality for island management, selling, and chat (additional methods will be called from IslandCommand)
}
