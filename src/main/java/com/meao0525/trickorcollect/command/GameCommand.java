package com.meao0525.trickorcollect.command;

import com.meao0525.trickorcollect.TrickorCollect;
import com.meao0525.trickorcollect.item.AdminBook;
import com.meao0525.trickorcollect.item.RuleBook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.A;

public class GameCommand implements CommandExecutor {

    private TrickorCollect plugin;

    public GameCommand(TrickorCollect plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //ゲームのコマンド
        if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
            //管理者book渡す
            if (sender instanceof Player) {
                AdminBook book = new AdminBook();
                ((Player)sender).getInventory().addItem(book.toItemStack());
            }
            //ヘルプコマンド
            sender.sendMessage(ChatColor.GOLD + "==========[Trick or Collect]===========\n" + ChatColor.RESET +
                    "/tc start --- ゲームスタート\n" +
                    "/tc stop --- ゲームを強制終了\n" +
                    "/tc info --- tc情報を表示\n" +
                    "/tc summon --- 取り立て屋（村人）を召喚\n" +
                    "/tc spawnpoint --- スポーン地点を設定\n" +
                    "/tc traitor <int> --- traitorの人数設定\n" +
                    "/tc rulebook <name> --- ルールブックの配布");

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
                    sender.sendMessage(ChatColor.GRAY + "まずは 取り立て屋 を召喚して収集アイテムを設定してください");
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
                try {
                    num = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    return false;
                }
            } else {
                return false;
            }
            plugin.setTraitorNum(num);
            plugin.reloadInfo();
            sender.sendMessage(ChatColor.GOLD + "[Trick or Collect]" + ChatColor.RESET + "traitorの人数を更新しました");

        } else if (args[0].equalsIgnoreCase("rulebook")) {
            //ルールブックの配布
            if (!plugin.isGame()) {
                RuleBook book = new RuleBook();
                if (args.length == 1) {
                    //引数なしなら全員に
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.getInventory().addItem(book.toItemStack());
                    }
                    sender.sendMessage(ChatColor.GOLD + "[Trick or Collect]" +
                            ChatColor.RESET + "全員にルールブックを渡しました。");
                } else if (args.length == 2) {
                    //引数一つなら指定された人に
                    Player player = Bukkit.getPlayer(args[1]);
                    if (player == null) {
                        sender.sendMessage(ChatColor.GRAY + args[1] + " というプレイヤーはいません。");
                    } else {
                        player.getInventory().addItem(book.toItemStack());
                        sender.sendMessage(ChatColor.GOLD + "[Trick or Collect]" +
                                ChatColor.RESET + args[1] + " にルールブックを渡しました。");
                    }
                } else {
                    return false;
                }
            } else {
                sender.sendMessage(ChatColor.GRAY + "ゲーム中は使えません");
            }
        }
        return true;
    }
}
