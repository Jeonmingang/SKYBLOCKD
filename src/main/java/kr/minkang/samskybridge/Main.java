
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
            IslandData d = storage.getIslandByPlayer(p2.getUniqueId());
        if (d == null) d = BentoBridge.resolveFromBento(this, p2);
            if (d == null) {
                msg(p, color("&c섬이 없습니다."));
                return true;
            }
            new UpgradeUI().open(p, d);
            return true;
        }

        if (sub.equalsIgnoreCase("레벨")) {
            IslandData d = storage.getIslandByPlayer(p2.getUniqueId());
        if (d == null) d = BentoBridge.resolveFromBento(this, p2);
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
            chatService.toggle(p.getUniqueId());
            return true;
        }

        

if (args.length >= 1 && args[0].equalsIgnoreCase("레벨")) {
    if (!(sender instanceof org.bukkit.entity.Player)) { sender.sendMessage("플레이어만 사용가능"); return true; }
    org.bukkit.entity.Player p2 = (org.bukkit.entity.Player) sender;
    IslandData d = storage.getIslandByPlayer(p2.getUniqueId());
    if (d == null) d = BentoBridge.resolveFromBento(this, p2);
    if (d == null) { p2.sendMessage(color("&c섬이 없습니다.")); return true; }
    int nextReq = Leveling.requiredXpForLevel(this, d.level + 1);
    int remain = Math.max(0, nextReq - d.xp);
    int gain = getConfig().getInt("upgrade.level.xp.per-purchase", 50);
    double cost = getConfig().getDouble("upgrade.level.cost.per-purchase", 20000D);
    p2.sendMessage(color("&a[섬] &f현재 레벨: &b" + d.level));
    p2.sendMessage(color("&a[섬] &f경험치: &b" + d.xp + "&7 / &b" + nextReq + " &7(다음 레벨까지 &b" + remain + "&7)"));
    p2.sendMessage(color("&a[섬] &f구매 1회: &b+" + gain + " &7| 가격: &b" + (cost==((long)cost)? (long)cost : cost)));
    p2.sendMessage(color("&a[섬] &7업그레이드 GUI: &f/섬 업그레이드"));
    return true;
}

if (args.length >= 1 && args[0].equalsIgnoreCase("설정")) {
    if (!sender.isOp()) { sender.sendMessage("OP만 사용가능"); return true; }
    if (args.length < 4) { sender.sendMessage("/섬 설정 <레벨|경험치|경험치추가|크기|팀> <닉네임> <값>"); return true; }
    String item = args[1];
    org.bukkit.OfflinePlayer target = org.bukkit.Bukkit.getOfflinePlayer(args[2]);
    java.util.UUID uuid = target != null ? target.getUniqueId() : null;
    if (uuid == null) { sender.sendMessage("대상 없음"); return true; }
    IslandData d = storage.getIslandByPlayer(uuid);
    if (d == null && sender instanceof org.bukkit.entity.Player) d = BentoBridge.resolveFromBento(this, (org.bukkit.entity.Player)sender);
    if (d == null) { sender.sendMessage("섬 데이터를 찾지 못했습니다."); return true; }
    try {
        int val = Integer.parseInt(args[3]);
        switch(item.toLowerCase()) {
            case "레벨": d.level = Math.max(1, val); break;
            case "경험치": d.xp = Math.max(0, val); break;
            case "경험치추가": d.xp = Math.max(0, d.xp + val); break;
            case "크기": d.sizeRadius = Math.max(1, val); break;
            case "팀":
                d.teamMax = Math.max(1, val);
                applyOwnerTeamPerm(d.owner, d.teamMax);
                break;
            default: sender.sendMessage("/섬 설정 <레벨|경험치|경험치추가|크기|팀> <닉네임> <값>"); return true;
        }
        storage.write(d); storage.save();
        sender.sendMessage("완료");
    } catch (NumberFormatException ex) { sender.sendMessage("값은 숫자"); }
    return true;
}
return false; // 기타는 /is로 포워딩됨
    }

    private void help(Player p) {
        p2.sendMessage(color("&7====== &b섬 도움말 &7(1/1) ======"));
        p2.sendMessage(color("&b/섬 업그레이드 &7- 섬 크기/팀 인원/레벨 경험치 GUI"));
        p2.sendMessage(color("&b/섬 레벨 &7- 내 섬 레벨/XP 요약"));
        p2.sendMessage(color("&b/섬 랭킹 &7- 상위 섬 목록"));
        p2.sendMessage(color("&b/섬 채팅 &7- 섬 전용 채팅 토글"));
        p2.sendMessage(color("&b/섬 초대 <닉> &7- 팀 초대 (&e/섬 초대 수락&7, &e/섬 초대 거절&7)"));
        p2.sendMessage(color("&b/섬 알바 <닉> &7- 협력 권한 부여 (&e/섬 알바해제 <닉>&7)"));
        p2.sendMessage(color("&b/섬 리셋 <설계도> &7- 섬 초기화"));
        p2.sendMessage(color("&7그 외: &f/is <원하는 명령> &7과 동일하게 사용"));
    }

    String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    void msg(Player p, String s) {
        if (s == null) return;
        String prefix = getConfig().getString("messages.prefix", "");
        p2.sendMessage(color(prefix + s));
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
