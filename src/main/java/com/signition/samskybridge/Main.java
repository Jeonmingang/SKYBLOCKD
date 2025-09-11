package com.signition.samskybridge;

import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.listener.BlockXPListener;
import com.signition.samskybridge.listener.DropPickupTracker;
import com.signition.samskybridge.listener.JoinListener;
import com.signition.samskybridge.listener.ChatListener;
import com.signition.samskybridge.listener.GuiListener;
import com.signition.samskybridge.rank.RankingService;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private DataStore dataStore;
    private LevelService levelService;
    private RankingService rankingService;
    private GuiListener guiListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        dataStore = new DataStore(this);
        levelService = new LevelService(this, dataStore);
        rankingService = new RankingService(this, dataStore);
        guiListener = new GuiListener(this);

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new DropPickupTracker(), this);
        pm.registerEvents(new BlockXPListener(this), this);
        pm.registerEvents(new JoinListener(this), this);
        pm.registerEvents(new ChatListener(this, dataStore), this);
        pm.registerEvents(guiListener, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("섬")) return false;
        if (args.length > 0 && args[0].equalsIgnoreCase("리로드")){
            reloadConfig();
            sender.sendMessage(Text.col("&a설정이 리로드되었습니다."));
            return true;
        }
        sender.sendMessage(Text.col("&e사용법: /섬 리로드"));
        return true;
    }

    public DataStore getDataStore() { return dataStore; }
    public LevelService getLevelService() { return levelService; }
    public RankingService getRankingService() { return rankingService; }
    public GuiListener getGuiListener(){ return guiListener; }

    public FileConfiguration cfg() { return getConfig(); }
}
