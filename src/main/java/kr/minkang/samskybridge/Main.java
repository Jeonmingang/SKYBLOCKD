
package kr.minkang.samskybridge;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class Main extends JavaPlugin implements TabExecutor {

    Storage storage;
    Integration integration;
    ChatService chatService;
    TabRefresher tabRefresher;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.storage = new Storage(this);
        this.integration = new Integration(this);
        this.chatService = new ChatService(this);
        Bukkit.getPluginManager().registerEvents(chatService, this);
        Bukkit.getPluginManager().registerEvents(new SimpleUpgradeClickListener(this), this);
        Bukkit.getPluginManager().registerEvents(new KoCommandBridgeListener(), this);
        this.tabRefresher = new TabRefresher(this, storage);
        this.tabRefresher.start();

        if (getCommand("섬") != null) {
            getCommand("섬").setExecutor(this);
            getCommand("섬").setTabCompleter(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("섬")) return false;
        if (!(sender instanceof Player)) {
            sender.sendMessage("플레이어만 사용할 수 있습니다.");
            return true;
        }
        Player p = (Player) sender;
        if (args.length == 0) {
            help(p);
            return true;
        }
        String sub = args[0];

        if (sub.equalsIgnoreCase("업그레이드")) {
            IslandData d = storage.getIslandByPlayer(p.getUniqueId());
        if (d == null) d = BentoBridge.resolveFromBento(this, p);
            if (d == null) {
                msg(p, color("&c섬이 없습니다."));
                return true;
            }
            new UpgradeUI().open(p, d);
            return true;
        }

        if (sub.equalsIgnoreCase("레벨")) {
            IslandData d = storage.getIslandByPlayer(p.getUniqueId());
        if (d == null) d = BentoBridge.resolveFromBento(this, p);
            if (d == null) { msg(p, color("&c섬이 없습니다.")); return true; }
            msg(p, color("&b섬 레벨: &f" + d.level + " &7(경험치 " + d.xp + ")"));
            return true;
        }

        if (sub.equalsIgnoreCase("랭킹")) {
            List<IslandData> list = storage.getAllIslands();
            list.sort((a,b) -> {
                int c = Integer.compare(b.level, a.level);
                if (c != 0) return c;
                return Integer.compare(b.xp, a.xp);
            });
            sender.sendMessage(color("&7===== &b섬 랭킹 &7====="));
            String fmt = getConfig().getString("messages.rank-format", "&f<rank>. &a<name> &7- &fLv.<level> &7(경험치 <xp>) &8| &7크기 <size> &7| 인원 <team>");
            int limit = Math.min(10, list.size());
            for (int i = 0; i < limit; i++) {
                IslandData d = list.get(i);
                String name = getName(d.owner);
                String line = fmt.replace("<rank>", String.valueOf(i+1))
                        .replace("<name>", name == null ? "?" : name)
                        .replace("<level>", String.valueOf(d.level))
                        .replace("<xp>", String.valueOf(d.xp))
                        .replace("<size>", String.valueOf(d.sizeRadius))
                        .replace("<team>", String.valueOf(d.teamMax));
                sender.sendMessage(color(line));
            }
            return true;
        }

        if (sub.equalsIgnoreCase("채팅")) {
            boolean on = chatService.toggle(p.getUniqueId());
            msg(p, color(on ? cfg().getString("messages.chat-on", "&a섬 채팅이 켜졌습니다.")
                            : cfg().getString("messages.chat-off", "&e섬 채팅이 꺼졌습니다.")));
            return true;
        }

        return false; // 기타는 /is로 포워딩됨
    }

    private void help(Player p) {
        p.sendMessage(color("&7====== &b섬 도움말 &7(1/1) ======"));
        p.sendMessage(color("&b/섬 업그레이드 &7- 섬 크기/팀 인원/레벨 경험치 GUI"));
        p.sendMessage(color("&b/섬 레벨 &7- 내 섬 레벨/XP 요약"));
        p.sendMessage(color("&b/섬 랭킹 &7- 상위 섬 목록"));
        p.sendMessage(color("&b/섬 채팅 &7- 섬 전용 채팅 토글"));
        p.sendMessage(color("&b/섬 초대 <닉> &7- 팀 초대 (&e/섬 초대 수락&7, &e/섬 초대 거절&7)"));
        p.sendMessage(color("&b/섬 알바 <닉> &7- 협력 권한 부여 (&e/섬 알바해제 <닉>&7)"));
        p.sendMessage(color("&b/섬 리셋 <설계도> &7- 섬 초기화"));
        p.sendMessage(color("&7그 외: &f/is <원하는 명령> &7과 동일하게 사용"));
    }

    String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    void msg(Player p, String s) {
        if (s == null) return;
        String prefix = getConfig().getString("messages.prefix", "");
        p.sendMessage(color(prefix + s));
    }

    public FileConfiguration cfg() { return getConfig(); }

    String getName(UUID id) {
        if (id == null) return null;
        Player pl = Bukkit.getPlayer(id);
        if (pl != null) return pl.getName();
        try {
            return Bukkit.getOfflinePlayer(id).getName();
        } catch (Throwable t) {
            return null;
        }
    }
}
