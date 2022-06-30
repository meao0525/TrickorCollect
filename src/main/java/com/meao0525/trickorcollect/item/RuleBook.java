package com.meao0525.trickorcollect.item;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class RuleBook {
    private final String TITLE = "Trick or Collect";
    private final String AUTHOR = "Trick or Collect";

    private final String COLLECTOR = ChatColor.AQUA + "collector" + ChatColor.RESET;
    private final String TRAITOR = ChatColor.RED + "traitor" + ChatColor.RESET;

    private final String[] CONTENTS = {ChatColor.GOLD + "  [Trick or Collect]\n\n"
                                            + ChatColor.RESET + "         ルール\n\n"
                                            + "このゲームはアイテム集めと人狼を足したようなゲームです。\n"
                                            + "裏切者を見つけ出し、指定されたアイテムを制限時間内に集めましょう！",
                                        ChatColor.GOLD + "[" + COLLECTOR + ChatColor.GOLD + "の目標]\n\n"
                                            + ChatColor.RESET + "-勝利条件-\n制限時間内に目標アイテムをすべて集める\n\n"
                                            + "-投票-\n取り立て屋の足元ブロックから怪しい人を投票できる。"
                                            + "投票数が過半数を超えた人は取り立て屋に触ることができない。",
                                        ChatColor.GOLD + "[" + TRAITOR + ChatColor.GOLD + "の目標]\n\n"
                                            + ChatColor.RESET + "-勝利条件-\n目標アイテムが全て集まるのを阻止する\n\n"
                                            + "-盗む-\n鉄剣を持ってプレイヤーまたは取り立て屋を右クリックすると、"
                                            + "ランダムなアイテムを盗むことができる。"
                                            + "ただし投票されている人からは盗めない。(CD:30s)",
                                        ChatColor.GOLD + "[目標アイテム]\n\n"
                                            + ChatColor.RESET + "-ゲーム開始前-\n"
                                            + "[取り立て屋]を右クリックして目標アイテムを設定できる。\n\n"
                                            + "-ゲーム中-\n"
                                            + "目標アイテムはプレイヤーインベントリを開くことで確認できる。",
                                        ChatColor.GOLD + "[取り立て屋]\n\n"
                                            + ChatColor.RESET + "-アイテム納品-\n"
                                            + "目標アイテムを手に持って右クリックすると、目標アイテムを納品できる。\n\n"
                                            + "-進捗の確認-\n"
                                            + "目標アイテム以外を持って右クリックすると、目標アイテムの進捗を確認できる。",
                                        ChatColor.GOLD + "[Tips]\n\n"
                                            + ChatColor.RESET + "-リスポーン-\n"
                                            + "ゲーム中にスニークしてその場にとどまると、初期地点にリスポーンできる。\n"
                                            + "ただし、移動したりスニークを解除したりするとリスポーンがキャンセルされる。\n\n",
                                        ChatColor.GOLD + "[Tips]\n\n"
                                            + ChatColor.RESET + "-チャット-\n"
                                            + ChatColor.RESET + "ゲーム中のチャットは10m以内の人にしか表示されない。"};

    public ItemStack toItemStack() {
        //本を生成
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        //本の中身作成
        meta.setTitle(TITLE);
        meta.setAuthor(AUTHOR);
        for (String page : CONTENTS) {
            meta.addPage(page);
        }
        meta.setCustomModelData(1);
        //メタ戻す
        book.setItemMeta(meta);

        return book;
    }
}
