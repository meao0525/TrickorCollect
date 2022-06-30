package com.meao0525.trickorcollect.item;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class AdminBook {
    private final String TITLE = "Trick or Collect管理用";
    private final String AUTHOR = "Trick or Collect";

    private final String[] CONTENTS = {ChatColor.GOLD + "  [Trick or Collect]\n\n"
                                            + ChatColor.RESET + "         ルール\n\n"
                                            + "-ゲームスタート\n\n"
                                            + "-ゲーム強制終了",
                                        ChatColor.GOLD + "  [Trick or Collect]\n\n"
                                            + ChatColor.RESET + "-取り立て屋を召喚\n\n"
                                            + "-情報を表示\n\n"
                                            + "-スポーン地点を設定\n\n"
                                            + "-traitor + 1\n\n"
                                            + "-traitor - 1\n\n"
                                            + "-ルールブックを配布"};

    public ItemStack toItemStack() {
        //本の生成
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        //本の中身書いていくよ
        meta.setTitle(TITLE);
        meta.setAuthor(AUTHOR);
        for (String page : CONTENTS) {
            meta.addPage(page);
        }
        //メタを戻す
        book.setItemMeta(meta);

        return book;
    }
}
