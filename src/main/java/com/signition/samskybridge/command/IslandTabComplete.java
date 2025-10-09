package com.signition.samskybridge.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class IslandTabComplete implements TabCompleter {

    private static final List<String> ROOT = Arrays.asList("업그레이드", "정보", "레벨", "랭킹", "채팅", "도움말");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return null;

        if (args.length == 1){
            String p = args[0].toLowerCase(Locale.ROOT);
            return ROOT.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(p)).collect(Collectors.toList());
        }

        if (args.length == 2 && "업그레이드".equalsIgnoreCase(args[0])){
            List<String> sub = Arrays.asList("크기","인원","레벨");
            String p = args[1].toLowerCase(Locale.ROOT);
            return sub.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(p)).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}