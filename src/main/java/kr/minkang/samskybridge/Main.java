
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
        // Auto-wired listeners for Tab/Nametag
        org.bukkit.plugin.PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new com.signition.samskybridge.tab.TabService(this), this);

        pm.registerEvents(new com.signition.samskybridge.tab.TablistService(this), this);

        pm.registerEvents(new kr.minkang.samskybridge.TabRefresher(this), this);

        pm.registerEvents(new kr.minkang.samskybridge.NametagService(this), this);

        saveDefaultConfig();
        this.storage = new Storage(this);
        this.integration = new Integration(this);
        this.chatService = new ChatService(this);
        Bukkit.getPluginManager().registerEvents(chatService, this);
        Bukkit.getPluginManager().registerEvents(new SimpleUpgradeClickListener(this), this);
        Bukkit.getPluginManager().registerEvents(new KoCommandBridgeListener(), this);
        Bukkit.getPluginManager().registerEvents(new JoinImportListener(this), this);
        this.tabRefresher = new TabRefresher(this, storage);
        this.tabRefresher.start();

        if (getCommand("섬") != null) {
            getCommand("섬").setExecutor(this);
            getCommand("섬").setTabCompleter(this);
        }

        // Import/resolve islands for already-online players (reload-safe)
        for (org.bukkit.entity.Player online : Bukkit.getOnlinePlayers()) {
            kr.minkang.samskybridge.IslandData d = storage.getIslandByPlayer(online.getUniqueId());
            if (d == null) d = kr.minkang.samskybridge.BentoBridge.resolveFromBento(this, online);
            if (d != null) {
                int teamMax = kr.minkang.samskybridge.BentoBridge.getTeamMax(online);
                if (teamMax > 0) applyOwnerTeamPerm(d.owner, teamMax);
            }
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
        if (d == null) { msg(p, color("&c섬이 없습니다.")); return true; }
        new UpgradeUI().open(p, d);
        return true;
    }

    if (sub.equalsIgnoreCase("레벨")) {
        IslandData d = storage.getIslandByPlayer(p.getUniqueId());
        if (d == null) d = BentoBridge.resolveFromBento(this, p);
        if (d == null) { msg(p, color("&c섬이 없습니다.")); return true; }
        int nextReq = Leveling.requiredXpForLevel(this, d.level + 1);
        int remain = Math.max(0, nextReq - d.xp);
        int gain = getConfig().getInt("upgrade.level.xp.per-purchase", 50);
        double cost = getConfig().getDouble("upgrade.level.cost.per-purchase", 20000D);
        p.sendMessage(color("&a[섬] &f현재 레벨: &b" + d.level));
        p.sendMessage(color("&a[섬] &f경험치: &b" + d.xp + "&7 / &b" + nextReq + " &7(다음 레벨까지 &b" + remain + "&7)"));
        p.sendMessage(color("&a[섬] &f구매 1회: &b+" + gain + " &7| 가격: &b" + (cost==((long)cost)? (long)cost : cost)));
        p.sendMessage(color("&a[섬] &7업그레이드 GUI: &f/섬 업그레이드"));
        return true;
    }

    if (sub.equalsIgnoreCase("랭킹")) {
        java.util.List<IslandData> list = storage.getAllIslands();
        list.sort((a,b) -> {
            int c = Integer.compare(b.level, a.level);
            if (c != 0) return c;
            return Integer.compare(b.xp, a.xp);
        });
        sender.sendMessage(color("&7===== &b섬 랭킹 &7====="));
        String fmt = getConfig().getString("messages.rank-format", "&f<rank>위 &7: &f<name> &7- &fLv.<level> &7(경험치 <xp>) &8| &7크기 <size> &7| 인원 <team>");
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
        chatService.toggle(p.getUniqueId());
        return true;
    }

    // 그 외는 KoCommandBridgeListener가 처리 ( /is 포워딩 )
    return false;
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

    // === Team size permission attach (owner) ===
    private final java.util.Map<java.util.UUID, org.bukkit.permissions.PermissionAttachment> teamPerms = new java.util.HashMap<>();

    public void applyOwnerTeamPerm(java.util.UUID owner, int teamMax) {
        try {
            org.bukkit.entity.Player op = getServer().getPlayer(owner);
            if (op == null) return;
            org.bukkit.permissions.PermissionAttachment prev = teamPerms.remove(owner);
            if (prev != null) { try { op.removeAttachment(prev); } catch (Throwable ignore) {} }
            String prefix = getConfig().getString("integration.team.permission-prefix", "bskyblock.team.maxsize.");
            org.bukkit.permissions.PermissionAttachment att = op.addAttachment(this);
            att.setPermission(prefix + teamMax, true);
            teamPerms.put(owner, att);
        } catch (Throwable ignored) {}
    }
}
