package com.meao0525.trickandcollect.command;

import com.meao0525.trickandcollect.TrickandCollect;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GameCommand implements CommandExecutor {

    private TrickandCollect plugin;

    public GameCommand(TrickandCollect plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //ゲームのコマンド
        if(args[0].equalsIgnoreCase("help")) {
            //ヘルプコマンド
            sender.sendMessage(ChatColor.GOLD + "==========[Trick and Collect]===========\n" + ChatColor.RESET +
                    "/onigo start --- ゲームスタート\n" +
                    "/onigo stop --- ゲームを強制終了\n" +
                    "/onito traitor <int> --- 裏切者の人数設定");

        } else if (args[0].equalsIgnoreCase("start")) {
            //ゲーム開始コマンド
            if (!plugin.isGame()) {
                sender.sendMessage(ChatColor.GOLD + "[Trick and Collect]" + ChatColor.RESET + "ゲームはじまるお");
                plugin.start();
            } else {
                sender.sendMessage(ChatColor.GRAY + "ゲームは始まってるお");
            }


        } else if (args[0].equalsIgnoreCase("stop")) {
            //強制終了コマンド
            if (plugin.isGame()) {
                sender.sendMessage(ChatColor.GOLD + "[Trick and Collect]" + ChatColor.RESET + "おわるお");
                plugin.stop();
            } else {
                sender.sendMessage(ChatColor.GRAY + "まだ何も始まっていないようだ...");
            }

        } else if (args[0].equalsIgnoreCase("traitor")) {
            //裏切者の人数を設定する
            int num = args[1].isEmpty() ? 1 : Integer.parseInt(args[1]);
            plugin.setTraitorNum(num);
            plugin.reloadInfo();
        }

        //TODO: 初期地点変える
        //TODO: info見えるようにする

        return true;
    }
}
