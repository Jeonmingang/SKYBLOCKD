
package com.signition.samskybridge.upgrade;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.gui.UpgradeGui;
import org.bukkit.entity.Player;

public class UpgradeService {
    private final Main plugin;
    private final UpgradeGui gui;

    public UpgradeService(Main plugin){
        this.plugin = plugin;
        this.gui = new UpgradeGui(plugin, plugin.getDataStore());
    }

    public void open(Player p){
        gui.open(p);
    }
}
