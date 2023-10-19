package com.meao0525.trickorcollect.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;

public class CommandTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        //タブでコマンド保管
        if (args.length == 1) { //第1引数
            return Arrays.asList("help", "start", "stop", "info", "summon", "spawnpoint", "time", "traitor", "rulebook", "gameevent", "mode", "rule");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("mode")) {
                return Arrays.asList("default", "aprilfool", "halloween");
            } else if (args[0].equalsIgnoreCase("rule")) {
                return Arrays.asList("default", "collectEach");
            }
        }
        //デフォルトコンプリーター
        return null;
    }

}
