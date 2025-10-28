package com.minkang.ultimate.rps.data;

import com.minkang.ultimate.rps.UltimateRpsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

public class StatsManager {
    private final UltimateRpsPlugin plugin;
    private final File file;
    private YamlConfiguration yaml;

    public StatsManager(UltimateRpsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "stats.yml");
        this.yaml = new YamlConfiguration();
    }

    public void load() {
        if (!file.exists()) save();
        try { yaml.load(file); } catch (Exception e) { e.printStackTrace(); }
    }

    public void save() {
        try { yaml.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public void pruneStation(String station) {
        yaml.set("machines."+station, null);
        if (yaml.isConfigurationSection("machinePlayers."+station)) {
            yaml.set("machinePlayers."+station, null);
        }
        save();
    }

    public void pruneNonExisting(Set<String> existing) {
        if (yaml.isConfigurationSection("machines")) {
            Set<String> keys = new HashSet<>(yaml.getConfigurationSection("machines").getKeys(false));
            for (String k : keys) {
                if (!existing.contains(k)) {
                    yaml.set("machines."+k, null);
                    yaml.set("machinePlayers."+k, null);
                }
            }
        }
        save();
    }

    public void addWin(UUID uuid, int coinsWon, int multiplier) {
        yaml.set(uuid+".wins", yaml.getInt(uuid+".wins",0)+1);
        yaml.set(uuid+".coinsWon", yaml.getInt(uuid+".coinsWon",0)+coinsWon);
        int best = yaml.getInt(uuid+".best",1);
        if (multiplier > best) yaml.set(uuid+".best", multiplier);
        save();
    }

    public void addLoss(UUID uuid, int coinsLost) {
        yaml.set(uuid+".losses", yaml.getInt(uuid+".losses",0)+1);
        yaml.set(uuid+".coinsLost", yaml.getInt(uuid+".coinsLost",0)+coinsLost);
        save();
    }

    public void addMachinePayout(String station, int coins) {
        String path = "machines."+station;
        yaml.set(path, yaml.getInt(path, 0) + coins);
        save();
    }

    public void addMachinePlayerPayout(String station, UUID uuid, int coins, String lastKnownName) {
        String path = "machinePlayers."+station+"."+uuid.toString();
        yaml.set(path, yaml.getInt(path, 0) + coins);
        if (lastKnownName != null && !lastKnownName.isEmpty()) {
            yaml.set("machinePlayersName."+station+"."+uuid.toString(), lastKnownName);
        }
        save();
    }

    public List<String> buildTopLines(int limit) {
        List<String> out = new ArrayList<>();
        Map<String, Integer> machines = new HashMap<>();
        if (yaml.isConfigurationSection("machines")) {
            for (String key : yaml.getConfigurationSection("machines").getKeys(false)) {
                machines.put(key, yaml.getInt("machines."+key, 0));
            }
        }
        List<Map.Entry<String,Integer>> list = machines.entrySet().stream()
                .sorted((a,b)->b.getValue()-a.getValue())
                .collect(Collectors.toList());
        int max = Math.max(1, limit);
        int rank = 1;
        for (Map.Entry<String,Integer> e : list) {
            String station = e.getKey();
            if (plugin.stations().getByName(station) == null) continue; // skip removed
            int total = e.getValue();

            String coinName = "미설정";
            com.minkang.ultimate.rps.station.Station st = plugin.stations().getByName(station);
            if (st != null && st.getCoinItem() != null) {
                try {
                    if (st.getCoinItem().hasItemMeta() && st.getCoinItem().getItemMeta().hasDisplayName())
                        coinName = st.getCoinItem().getItemMeta().getDisplayName();
                    else
                        coinName = st.getCoinItem().getType().name();
                } catch (Throwable ignored) {}
            }

            String bestName = null; int bestAmt = 0;
            if (yaml.isConfigurationSection("machinePlayers."+station)) {
                Map<String,Integer> mp = new HashMap<>();
                for (String k : yaml.getConfigurationSection("machinePlayers."+station).getKeys(false)) {
                    mp.put(k, yaml.getInt("machinePlayers."+station+"."+k,0));
                }
                Optional<Map.Entry<String,Integer>> top = mp.entrySet().stream().max(Map.Entry.comparingByValue());
                if (top.isPresent()) {
                    UUID u = UUID.fromString(top.get().getKey());
                    OfflinePlayer op = Bukkit.getOfflinePlayer(u);
                    if (op != null && op.getName()!=null) {
                        bestName = op.getName();
                    } else {
                        // fallback to stored last-known name
                        bestName = yaml.getString("machinePlayersName."+station+"."+u.toString(), null);
                    }
                    bestAmt = top.get().getValue();
                }
            }

            String key = (bestName == null || bestName.isEmpty() || bestAmt <= 0)
                    ? "leaderboard.line-format-noplayer" : "leaderboard.line-format";
            String fmt = plugin.getConfig().getString(key, "&7{rank}. &b{station}&f - &e{coin}&f {amount}개");
            String line = fmt.replace("{rank}", String.valueOf(rank))
                    .replace("{station}", station)
                    .replace("{player}", bestName == null ? "" : bestName)
                    .replace("{amount}", String.valueOf(total))
                    .replace("{coin}", coinName);
            out.add(ChatColor.translateAlternateColorCodes('&', line));
            rank++; if (rank > max) break;
        }
        return out;
    }

    public void sendTop(CommandSender s) {
        String P = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&a[ 가위바위보 ]&f "));
        s.sendMessage(P + "기계별 플레이어 총 지급 코인 TOP 10");
        List<String> lines = buildTopLines(plugin.getConfig().getInt("leaderboard.max-lines", 10));
        if (lines.isEmpty()) s.sendMessage(ChatColor.GRAY + "데이터가 없습니다.");
        else for (String line : lines) s.sendMessage(line);
    }

    public void sendStats(CommandSender s, String name) {
        String P = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&a[ 가위바위보 ]&f "));
        OfflinePlayer op = Bukkit.getOfflinePlayer(name);
        UUID u = op.getUniqueId();
        int wins = yaml.getInt(u+".wins",0);
        int losses = yaml.getInt(u+".losses",0);
        int won = yaml.getInt(u+".coinsWon",0);
        int lost = yaml.getInt(u+".coinsLost",0);
        int best = yaml.getInt(u+".best",1);
        s.sendMessage(P + ChatColor.AQUA + name + ChatColor.WHITE + "님의 전적");
        s.sendMessage(ChatColor.GRAY + "승: " + wins + "  패: " + losses + "  최고배수: x" + best);
        s.sendMessage(ChatColor.GRAY + "총 획득: " + won + "개  총 손실: " + lost + "개");
    }
}
