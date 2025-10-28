
package com.minkang.ultimate.rps.command;

import com.minkang.ultimate.rps.UltimateRpsPlugin;
import com.minkang.ultimate.rps.gui.RouletteGui;
import com.minkang.ultimate.rps.station.Station;
import com.minkang.ultimate.rps.station.StationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RpsCommand implements CommandExecutor, TabCompleter {

    private final UltimateRpsPlugin plugin;
    private final String P;

    public RpsCommand(UltimateRpsPlugin plugin) {
        this.plugin = plugin;
        this.P = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&a[ 가위바위보 ]&f "));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            help(sender, label);
            return true;
        }
        String sub = args[0].toLowerCase();

        // 목록
        if (sub.equals("목록")) {
            Set<String> names = plugin.stations().getNames();
            if (names.isEmpty()) { sender.sendMessage(P + "생성된 기계가 없습니다."); return true; }
            sender.sendMessage(P + "현재 생성된 기계 목록 (" + names.size() + "개)");
            for (String n : names) {
                Station st = plugin.stations().getByName(n);
                Location l = st.getBlockLocation();
                String world = (l.getWorld()!=null ? l.getWorld().getName() : "-");
                String pos = world + " " + l.getBlockX()+","+l.getBlockY()+","+l.getBlockZ();
                String coin = (st.getCoinItem()!=null ? (st.getCoinItem().hasItemMeta() && st.getCoinItem().getItemMeta().hasDisplayName() ? st.getCoinItem().getItemMeta().getDisplayName() : st.getCoinItem().getType().name()) : "미설정");
                int minMul=0,maxMul=0,idx=1; for (int w : st.getWedges()) { if (w>0) { if (minMul==0) minMul=idx; maxMul=idx; } idx++; }
                sender.sendMessage(ChatColor.GRAY+" - "+ChatColor.AQUA+n+ChatColor.WHITE+" @ "+pos+ChatColor.GRAY+" | 코인: "+ChatColor.WHITE+coin+ChatColor.GRAY+" | 배수: "+(minMul==0?"x1~x1":("x"+minMul+"~x"+maxMul)));
            }
            return true;
        }

        // 설치
        if (sub.equals("설치") || sub.equals("install")) {
            if (!(sender instanceof Player)) { sender.sendMessage(P + "플레이어만 사용할 수 있습니다."); return true; }
            if (!sender.hasPermission("ultimaterps.admin")) { sender.sendMessage(P + "권한이 없습니다."); return true; }
            if (args.length < 2) { sender.sendMessage(P + "/" + label + " 설치 <이름>"); return true; }
            String name = args[1];

            // duplicate name guard
            if (plugin.stations().getByName(name) != null) {
                sender.sendMessage(P + "이미 존재하는 이름입니다.");
                return true;
            }

            Player p = (Player) sender;
            Block target = p.getTargetBlockExact(5);
            Location place = (target != null ? target.getLocation().add(0,1,0) : p.getLocation().getBlock().getLocation());

            // location guard
            if (plugin.stations().getByBlock(place.getBlock()) != null) {
                sender.sendMessage(P + "이 위치에는 이미 다른 기계가 있습니다.");
                return true;
            }
            if (!place.getBlock().getType().isAir()) {
                sender.sendMessage(P + "설치 위치가 비어있지 않습니다.");
                return true;
            }

            place.getBlock().setType(Material.DIAMOND_BLOCK);
            Station st = plugin.stations().createStation(name, place);
            if (st == null) {
                place.getBlock().setType(Material.AIR);
                sender.sendMessage(P + "설치 실패: 이미 존재하는 이름입니다.");
                return true;
            }
            if (plugin.getConfig().getBoolean("hologram.enabled-by-default", true)) {
                st.setHologram(true);
                plugin.holograms().spawnOrRefresh(st);
            }
            sender.sendMessage(P + plugin.getConfig().getString("messages.installed", "설치 완료").replace("{name}", name));
            return true;
        }

        // 제거
        if (sub.equals("제거") || sub.equals("remove")) {
            if (!sender.hasPermission("ultimaterps.admin")) { sender.sendMessage(P + "권한이 없습니다."); return true; }
            if (args.length < 2) { sender.sendMessage(P + "/" + label + " 제거 <이름>"); return true; }
            String name = args[1];
            Station st = plugin.stations().getByName(name);
            if (st == null) { sender.sendMessage(P + "존재하지 않는 이름입니다."); return true; }
            plugin.holograms().despawn(st);
            st.getBlockLocation().getBlock().setType(Material.AIR);
            plugin.stations().removeStation(name);
            try { plugin.stats().pruneStation(name); } catch (Throwable ignored) {}
            try { plugin.leaderboard().spawnOrRefresh(); } catch (Throwable ignored) {}
            sender.sendMessage(P + plugin.getConfig().getString("messages.removed", "제거 완료").replace("{name}", name));
            return true;
        }

        // 코인설정
        if (sub.equals("코인설정") || sub.equals("setcoin")) {
            if (!(sender instanceof Player)) { sender.sendMessage(P + "플레이어만 사용할 수 있습니다."); return true; }
            if (!sender.hasPermission("ultimaterps.admin")) { sender.sendMessage(P + "권한이 없습니다."); return true; }
            if (args.length < 2) { sender.sendMessage(P + "/" + label + " 코인설정 <이름>"); return true; }
            String name = args[1];
            Station st = plugin.stations().getByName(name);
            if (st == null) { sender.sendMessage(P + "존재하지 않는 이름입니다."); return true; }
            Player p = (Player) sender;
            ItemStack hand = p.getInventory().getItemInMainHand();
            if (hand == null || hand.getType() == Material.AIR) { sender.sendMessage(P + "손에 아이템을 들고 실행하세요."); return true; }
            ItemStack cp = hand.clone(); cp.setAmount(1);
            st.setCoinItem(cp); plugin.stations().save();
            sender.sendMessage(P + plugin.getConfig().getString("messages.coin-set").replace("{name}", name));
            if (st.isHologram()) plugin.holograms().spawnOrRefresh(st);
            return true;
        }

        // 보상웨지설정
        if (sub.equals("보상웨지설정") || sub.equals("setwedge")) {
            if (!(sender instanceof Player)) { sender.sendMessage(P + "플레이어만 사용할 수 있습니다."); return true; }
            if (!sender.hasPermission("ultimaterps.admin")) { sender.sendMessage(P + "권한이 없습니다."); return true; }
            if (args.length < 2) { sender.sendMessage(P + "/" + label + " 보상웨지설정 <이름>"); return true; }
            String name = args[1];
            Station st = plugin.stations().getByName(name);
            if (st == null) { sender.sendMessage(P + "존재하지 않는 이름입니다."); return true; }
            RouletteGui.openWedgeEditor((Player) sender, st);
            return true;
        }

        // 랭킹 / 기록
        if (sub.equals("랭킹") || sub.equals("rank")) {
            plugin.stats().sendTop(sender); return true;
        }
        if (sub.equals("기록") || sub.equals("stats")) {
            if (args.length >= 2) plugin.stats().sendStats(sender, args[1]); else plugin.stats().sendStats(sender, sender.getName());
            return true;
        }

        // 홀로그램
        
        if (sub.equals("횟수제한")) {
            if (!sender.hasPermission("ultimaterps.admin")) { sender.sendMessage(P + "권한이 없습니다."); return true; }
            if (args.length < 3) { sender.sendMessage(P + "/"+label+" 횟수제한 <이름> <횟수>"); return true; }
            String name = args[1];
            int limit;
            try { limit = Integer.parseInt(args[2]); } catch (Exception ex) { sender.sendMessage(P + "숫자를 입력하세요."); return true; }
            com.minkang.ultimate.rps.station.Station st = plugin.stations().getByName(name);
            if (st == null) { sender.sendMessage(P + "존재하지 않는 이름입니다."); return true; }
            st.setDailyLimitAttempts(limit);
            plugin.stations().save();
            sender.sendMessage(P + "하루 참여 제한을 설정했습니다: " + name + " -> " + limit + "회");
            return true;
        }

        if (sub.equals("홀로그램") || sub.equals("hologram")) {
            if (!sender.hasPermission("ultimaterps.admin")) { sender.sendMessage(P + "권한이 없습니다."); return true; }
            if (args.length < 2) {
                sender.sendMessage(P + "사용법:");
                sender.sendMessage(P + "/"+label+" 홀로그램 <이름> on|off");
                sender.sendMessage(P + "/"+label+" 홀로그램 추가 <이름> <문구>");
                sender.sendMessage(P + "/"+label+" 홀로그램 삭제 <이름>");
                sender.sendMessage(P + "/"+label+" 홀로그램 적용 <이름>");
                sender.sendMessage(P + "/"+label+" 홀로그램 클리어");
                return true;
            }
            if (args.length == 2 && args[1].equalsIgnoreCase("클리어")) {
                plugin.holograms().despawnAll(); plugin.holograms().hardClearAll();
                try { plugin.leaderboard().despawn(); } catch (Throwable ignored) {}
                for (String n : plugin.stations().getNames()) {
                    Station st = plugin.stations().getByName(n);
                    if (st != null) st.setHologramIds(new java.util.ArrayList<java.util.UUID>());
                }
                plugin.stations().save();
                sender.sendMessage(P + "모든 홀로그램을 제거했습니다.");
                return true;
            }
            if (args.length >= 3 && (args[2].equalsIgnoreCase("on") || args[2].equalsIgnoreCase("off"))) {
                String name = args[1]; String mode = args[2].toLowerCase();
                Station st = plugin.stations().getByName(name);
                if (st == null) { sender.sendMessage(P + "존재하지 않는 이름입니다."); return true; }
                if (mode.equals("on")) { st.setHologram(true); plugin.holograms().spawnOrRefresh(st);
                    sender.sendMessage(P + plugin.getConfig().getString("messages.hologram-on").replace("{name}", name)); }
                else { st.setHologram(false); plugin.holograms().despawn(st);
                    sender.sendMessage(P + plugin.getConfig().getString("messages.hologram-off").replace("{name}", name)); }
                plugin.stations().save(); return true;
            }
            String sub2 = args[1].toLowerCase();
            if (sub2.equals("추가")) {
                if (args.length < 4) { sender.sendMessage(P + "/"+label+" 홀로그램 추가 <이름> <문구>"); return true; }
                String name = args[2];
                Station st = plugin.stations().getByName(name);
                if (st == null) { sender.sendMessage(P + "존재하지 않는 이름입니다."); return true; }
                String text = joinArgs(args, 3);
                List<String> list = st.getCustomHologramLines();
                if (list == null) list = new ArrayList<>();
                list.add(text); st.setCustomHologramLines(list); st.setHologram(true);
                plugin.stations().save(); plugin.holograms().spawnOrRefresh(st);
                sender.sendMessage(P + "홀로그램 문구를 추가했습니다. ("+name+")"); return true;
            } else if (sub2.equals("삭제")) {
                if (args.length < 3) { sender.sendMessage(P + "/"+label+" 홀로그램 삭제 <이름>"); return true; }
                String name = args[2];
                Station st = plugin.stations().getByName(name);
                if (st == null) { sender.sendMessage(P + "존재하지 않는 이름입니다."); return true; }
                st.setCustomHologramLines(new ArrayList<String>()); plugin.stations().save();
                if (st.isHologram()) plugin.holograms().spawnOrRefresh(st);
                sender.sendMessage(P + "홀로그램 문구를 모두 삭제했습니다. 기본 문구 적용 ("+name+")"); return true;
            } else if (sub2.equals("적용")) {
                if (args.length < 3) { sender.sendMessage(P + "/"+label+" 홀로그램 적용 <이름>"); return true; }
                String name = args[2];
                Station st = plugin.stations().getByName(name);
                if (st == null) { sender.sendMessage(P + "존재하지 않는 이름입니다."); return true; }
                st.setHologram(true); plugin.stations().save(); plugin.holograms().spawnOrRefresh(st);
                sender.sendMessage(P + "홀로그램을 적용했습니다. ("+name+")"); return true;
            } else {
                sender.sendMessage(P + "사용법: /"+label+" 홀로그램 <이름> on|off | 추가/삭제/적용 | 클리어"); return true;
            }
        }

        // 랭킹표시
        if (sub.equals("랭킹표시")) {
            if (!(sender instanceof Player)) { sender.sendMessage(P + "플레이어만 사용할 수 있습니다."); return true; }
            if (!sender.hasPermission("ultimaterps.admin")) { sender.sendMessage(P + "권한이 없습니다."); return true; }
            if (args.length < 2) { sender.sendMessage(P + "/"+label+" 랭킹표시 설치 | 제거"); return true; }
            Player p = (Player) sender;
            if (args[1].equalsIgnoreCase("설치")) {
                Block target = p.getTargetBlockExact(5);
                Location place = (target != null ? target.getLocation().add(0,1,0) : p.getLocation().getBlock().getLocation());
                if (!place.getBlock().getType().isAir()) { sender.sendMessage(P + "설치 위치가 비어있지 않습니다."); return true; }
                place.getBlock().setType(Material.BARRIER); UltimateRpsPlugin.get().leaderboard().install(place);
                sender.sendMessage(P + "랭킹표시를 설치했습니다."); return true;
            } else if (args[1].equalsIgnoreCase("제거")) {
                if (UltimateRpsPlugin.get().leaderboard().isInstalled()) {
                    Location l = UltimateRpsPlugin.get().leaderboard().getLocation();
                    if (l != null && l.getBlock().getType() == Material.BARRIER) l.getBlock().setType(Material.AIR);
                    UltimateRpsPlugin.get().leaderboard().remove(); sender.sendMessage(P + "랭킹표시를 제거했습니다.");
                } else sender.sendMessage(P + "설치된 랭킹표시가 없습니다.");
                return true;
            } else { sender.sendMessage(P + "/"+label+" 랭킹표시 설치 | 제거"); return true; }
        }

        // 리로드
        
        if (sub.equals("웨지적용전체")) {
            if (!sender.hasPermission("ultimaterps.admin")) { sender.sendMessage(P + "권한이 없습니다."); return true; }
            java.util.List<Integer> list = plugin.getConfig().getIntegerList("default-wedges");
            int[] def = new int[20];
            for (int i = 0; i < 20; i++) def[i] = i < list.size() ? Math.max(0, list.get(i)) : 1;
            int applied = 0;
            for (String n : plugin.stations().getNames()) {
                com.minkang.ultimate.rps.station.Station st = plugin.stations().getByName(n);
                if (st == null) continue;
                st.setWedges(java.util.Arrays.copyOf(def, def.length));
                applied++;
            }
            plugin.stations().save();
            sender.sendMessage(P + "기본 웨지를 전체 기계에 적용: " + applied + "개");
            return true;
        }

        if (sub.equals("리로드") || sub.equals("reload")) {
            if (!sender.hasPermission("ultimaterps.admin")) { sender.sendMessage(P + "권한이 없습니다."); return true; }
            plugin.reloadConfig(); sender.sendMessage(P + plugin.getConfig().getString("messages.reloaded")); return true;
        }

        help(sender, label); return true;
    }

    private void help(CommandSender s, String label) {
        s.sendMessage(P + "/"+label+" 설치 <이름>");
        s.sendMessage(P + "/"+label+" 제거 <이름>");
        s.sendMessage(P + "/"+label+" 코인설정 <이름>");
        s.sendMessage(P + "/"+label+" 보상웨지설정 <이름>");
        s.sendMessage(P + "/"+label+" 횟수제한 <이름> <횟수>");
        s.sendMessage(P + "/"+label+" 랭킹");
        s.sendMessage(P + "/"+label+" 기록 [닉]");
        s.sendMessage(P + "/"+label+" 홀로그램 <이름> on|off");
        s.sendMessage(P + "/"+label+" 홀로그램 추가 <이름> <문구>");
        s.sendMessage(P + "/"+label+" 홀로그램 삭제 <이름>");
        s.sendMessage(P + "/"+label+" 홀로그램 적용 <이름>");
        s.sendMessage(P + "/"+label+" 홀로그램 클리어");
        s.sendMessage(P + "/"+label+" 랭킹표시 설치|제거");
        s.sendMessage(P + "/"+label+" 웨지적용전체");
        s.sendMessage(P + "/"+label+" 목록");
        s.sendMessage(P + "/"+label+" 리로드");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        StationManager sm = plugin.stations();
        if (args.length == 1) {
            List<String> base = Arrays.asList("설치","제거","코인설정","보상웨지설정","랭킹","기록","홀로그램","리로드","랭킹표시","목록","웨지적용전체");
            return base.stream().filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
        }
        if (args.length == 2) {
            switch (args[0]) {
                case "제거":
                case "코인설정":
                case "보상웨지설정":
                case "횟수제한":
                    return new ArrayList<>(sm.getNames()).stream().filter(n -> n.startsWith(args[1])).collect(Collectors.toList());
                case "홀로그램":
                    return Arrays.asList("on","off","추가","삭제","적용","클리어").stream().filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
                case "기록":
                    return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n->n.startsWith(args[1])).collect(Collectors.toList());
                case "랭킹표시":
                    return Arrays.asList("설치","제거").stream().filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
                default: return new ArrayList<>();
            }
        }
        if (args.length == 3 && args[0].equals("홀로그램")) {
            return new ArrayList<>(sm.getNames()).stream().filter(n -> n.startsWith(args[2])).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private String joinArgs(String[] a, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < a.length; i++) { if (i > from) sb.append(" "); sb.append(a[i]); }
        return sb.toString();
    }
}
