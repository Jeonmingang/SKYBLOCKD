
package com.signition.samskybridge.listener;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.gui.IslandMarketGui;
import com.signition.samskybridge.gui.ManagementGui;
import com.signition.samskybridge.gui.UpgradeGui;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GuiListener implements Listener {
  private final Main plugin;
  private final UpgradeGui upgrade;
  private final IslandMarketGui market;
  private final ManagementGui mgmt;
  public GuiListener(Main plugin){
    this.plugin=plugin;
    this.upgrade = new UpgradeGui(plugin, plugin.getDataStore());
    this.market = new IslandMarketGui(plugin, plugin.getDataStore());
    this.mgmt = new ManagementGui(plugin, plugin.getDataStore());
  }
  public UpgradeGui getUpgrade(){ return upgrade; }
  public IslandMarketGui getMarket(){ return market; }
  public ManagementGui getMgmt(){ return mgmt; }

  @EventHandler public void onClick(InventoryClickEvent e){
    if (!(e.getWhoClicked() instanceof Player)) return;
    Player p = (Player)e.getWhoClicked();
    upgrade.onClick(p, e);
    market.onClick(p, e);
    market.onConfirm(p, e);
    mgmt.onClick(p, e);
  }
}
