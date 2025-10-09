
package com.signition.samskybridge;

import com.signition.samskybridge.command.UpgradeCommand;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.listener.GuiListener;
import com.signition.samskybridge.upgrade.UpgradeService;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
  private DataStore store;
  private UpgradeService upgrade;

  @Override public void onEnable(){
    saveDefaultConfig();
    this.store = new DataStore(getDataFolder());
    this.upgrade = new UpgradeService(this, store);
    getServer().getPluginManager().registerEvents(new GuiListener(this, upgrade), this);
    if (getCommand("섬업그레이드") != null) getCommand("섬업그레이드").setExecutor(new UpgradeCommand(upgrade));
    getLogger().info("SamSkyBridge Upgrade Fix enabled");
  }

  public UpgradeService upgrade(){ return upgrade; }
}
