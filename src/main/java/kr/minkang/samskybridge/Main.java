
package kr.minkang.samskybridge;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class Main extends JavaPlugin implements TabExecutor {

    private Storage storage;
    private ChatService chatService;
    private TabRefresher tabRefresher;
    private Integration integration;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        storage = new Storage(this);
        chatService = new ChatService(this, storage);
        integration = new Integration(this);
        Bukkit.getPluginManager().registerEvents(chatService, this);
        getServer().getPluginManager().registerEvents(new KoCommandBridgeListener(this), this);
        getServer().getPluginManager().registerEvents(new SimpleUpgradeClickListener(this, storage, integration), this);
        getCommand("섬").setExecutor(this);
        this.tabRefresher = new TabRefresher(this, storage);
        this.tabRefresher.start();
        getLogger().info("SamSkyBridge enabled.");
    }

    @Override
    public void onDisable() {
        storage.save();
        if (tabRefresher != null) tabRefresher.stop();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("플레이어만 사용 가능");
            return true;
        }
        Player p = (Player) sender;
        if (args.length == 0) {
            p.sendMessage("/섬 <랭킹|업그레이드|채팅|레벨>");
            return true;
        }
        String sub = args[0];
        if (sub.equalsIgnoreCase("채팅")) {
            chatService.toggle(p);
            return true;
        }
        if (sub.equalsIgnoreCase("레벨")) {
            IslandData d = storage.getIslandByPlayer(p.getUniqueId());
            if (d == null) {
                msg(p, getConfig().getString("messages.no-island"));
            } else {
                p.sendMessage("§a섬 레벨: §f" + d.level + " §7(경험치 " + d.xp + ")");
            }
            return true;
        }
        if (sub.equalsIgnoreCase("랭킹")) {
            sendRanking(p);
            return true;
        }
        if (sub.equalsIgnoreCase("업그레이드")) {
            IslandData d = storage.getIslandByOwner(p.getUniqueId());
            if (d == null) {
                msg(p, getConfig().getString("messages.not-owner"));
                return true;
            }
            new UpgradeUI(this, storage, integration).open(p, d);
            return true;
        }
        p.sendMessage("/섬 <랭킹|업그레이드|채팅|레벨>");
        return true;
    }

    private void sendRanking(Player p) {
        List<IslandData> list = storage.getAllIslands();
        list.sort(Comparator.comparingInt(IslandData::getLevel).reversed().thenComparingInt(IslandData::getXp).reversed());
        String fmt = getConfig().getString("messages.rank-format");
        int i = 1;
        for (IslandData d : list.stream().limit(10).collect(Collectors.toList())) {
            String line = fmt.replace("<rank>", String.valueOf(i))
                    .replace("<name>", Bukkit.getOfflinePlayer(d.owner).getName() == null ? "Unknown" : Bukkit.getOfflinePlayer(d.owner).getName())
                    .replace("<level>", String.valueOf(d.level))
                    .replace("<xp>", String.valueOf(d.xp))
                    .replace("<size>", String.valueOf(d.sizeRadius))
                    .replace("<team>", String.valueOf(d.teamMax));
            p.sendMessage(line);
            i++;
        }
    }

    void msg(Player p, String s) {
        if (s == null) return;
        p.sendMessage(getConfig().getString("messages.prefix") + s);
    }

    public FileConfiguration cfg() { return getConfig(); }
}
