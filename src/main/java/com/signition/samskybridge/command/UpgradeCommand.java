
package com.signition.samskybridge.command;
import com.signition.samskybridge.upgrade.UpgradeService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UpgradeCommand implements CommandExecutor {
  private final UpgradeService service;
  public UpgradeCommand(UpgradeService service){ this.service = service; }
  @Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
    if (!(sender instanceof Player)){ sender.sendMessage("Player only."); return true; }
    Player p = (Player) sender;
    service.open(p);
    return true;
  }
}
