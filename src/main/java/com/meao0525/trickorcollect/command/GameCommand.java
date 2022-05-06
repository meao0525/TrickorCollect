package com.meao0525.trickorcollect.command;

import com.meao0525.trickorcollect.TrickorCollect;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GameCommand implements CommandExecutor {

    private TrickorCollect plugin;

    public GameCommand(TrickorCollect plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //ゲームのコマンド
        if(args[0].equalsIgnoreCase("help")) {
            //ヘルプコマンド
            sender.sendMessage(ChatColor.GOLD + "==========[Trick or Collect]===========\n" + ChatColor.RESET +
                    "/tc start --- ゲームスタート\n" +
                    "/tc stop --- ゲームを強制終了\n" +
                    "/tc info --- tc情報を表示\n" +
                    "/tc summon --- 取り立て屋（村人）を召喚\n" +
                    "/tc spawnpoint --- スポーン地点を設定\n" +
                    "/tc traitor <int> --- 裏切者の人数設定\n");

        } else if (args[0].equalsIgnoreCase("start")) {
            //ゲーム開始コマンド
            if (!plugin.isGame()) {
                if (plugin.getCollector() != null) {
                    if (!plugin.getCollects().isEmpty()) {
                        plugin.start();
                    } else {
                        sender.sendMessage(ChatColor.GRAY + "収集アイテムが設定されていません");
                    }
                } else {
                    sender.sendMessage(ChatColor.GRAY + "まずは /tc summon で収集アイテムを設定してください");
                }
            } else {
                sender.sendMessage(ChatColor.GRAY + "ゲームは始まってるお");
            }


        } else if (args[0].equalsIgnoreCase("stop")) {
            //強制終了コマンド
            if (plugin.isGame()) {
                plugin.stop();
            } else {
                sender.sendMessage(ChatColor.GRAY + "まだ何も始まっていないようだ...");
            }

        } else if (args[0].equalsIgnoreCase("info")) {
            //情報を表示
            plugin.toggleInfo();

        } else if (args[0].equalsIgnoreCase("summon")) {
            //取り立て屋（村人）を召喚
            if (!plugin.isGame()) {
                plugin.summonCollector();
            } else {
                sender.sendMessage(ChatColor.GRAY + "ゲーム中は使えません");
            }

        } else if (args[0].equalsIgnoreCase("spawnpoint")) {
            //スポーン地点設定
            if (sender instanceof Player) {
                plugin.setSpawnPoint(((Player) sender).getLocation().getBlock().getLocation());
                plugin.reloadInfo();
                sender.sendMessage(ChatColor.GOLD + "[Trick or Collect]" + ChatColor.RESET + "スポーン地点を更新しました");

            } else {
                sender.sendMessage(ChatColor.GRAY + "このコマンドはPlayerのみ実行できます");
            }

        } else if (args[0].equalsIgnoreCase("traitor")) {
            //裏切者の人数を設定する
            int num;
            if (args.length == 1) {
                num = 0;
            } else if (args.length == 2) {
                num = Integer.parseInt(args[1]);
            } else {
                return false;
            }
            plugin.setTraitorNum(num);
            plugin.reloadInfo();
            sender.sendMessage(ChatColor.GOLD + "[Trick or Collect]" + ChatColor.RESET + "traitorの人数を更新しました");

        }
        return true;
    }
}
