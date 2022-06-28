package com.meao0525.trickorcollect.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class RuleBook {
    private final String TITLE = "Trick or Collect";
    private final String AUTHOR = "meao0525";

    private final String PAGE_1 = "1ページ目だよ";

    public ItemStack toItemStack() {
        //本を生成
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        //本の中身作成
        meta.setTitle(TITLE);
        meta.setAuthor(AUTHOR);
        meta.addPage(PAGE_1);
        //メタ戻す
        book.setItemMeta(meta);

        return book;
    }
}
