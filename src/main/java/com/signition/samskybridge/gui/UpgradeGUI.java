package com.signition.samskybridge.gui;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.upgrade.UpgradeService;
import org.bukkit.entity.Player;

public class UpgradeGUI {
    private final UpgradeService svc;

    public UpgradeGUI(Main plugin, LevelService level){
        this.svc = new UpgradeService(plugin, level);
    }

    public void open(Player p){
        svc.open(p);
    }
}
