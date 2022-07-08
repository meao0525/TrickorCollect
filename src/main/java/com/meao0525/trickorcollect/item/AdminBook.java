package com.meao0525.trickorcollect.item;

import com.meao0525.trickorcollect.TrickorCollect;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class AdminBook {
    private final String TITLE = "Trick or Collect管理用";
    private final String AUTHOR = "Trick or Collect";
    private final String PAGE_HEADER = ChatColor.GOLD + "  [Trick or Collect]\n\n";

    private final String[][] CONTENTS = {{"-ゲームスタート\n\n", "/tc start"},
            {"-ゲーム強制終了\n\n", "/tc stop"},
            {"-取り立て屋を召喚\n\n", "/tc summon"},
            {"-情報を表示\n\n", "/tc info"},
            {"-スポーン地点を設定\n\n", "/tc spawnpoint"},
            {"-traitor + 1\n\n", "/tc traitor +1"},
            {"-traitor - 1\n\n", "/tc traitor -1"},
            {"-ルールブックを配布", "/tc rulebook"}};

    public ItemStack toItemStack() {
        //本の生成
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        //本の中身書いていくよ
        meta.setTitle(TITLE);
        meta.setAuthor(AUTHOR);
        //コンポーネント組み立てる用
        int i = 0;
        ArrayList<BaseComponent> list = new ArrayList<>();

        //1ページ目
        list.add(new TextComponent(PAGE_HEADER));
        //start, stop
        while(i < 2) {
            list.add(createCommandComponent(CONTENTS[i][0], CONTENTS[i][1]));
            i++;
        }
        //ページにセットする
        meta.spigot().addPage(list.toArray(new BaseComponent[list.size()]));

        //2ページ目
        list.clear();
        list.add(new TextComponent(PAGE_HEADER));
        //summon, info, spawnpoint, traitor, rulebook
        while(i < CONTENTS.length) {
            list.add(createCommandComponent(CONTENTS[i][0], CONTENTS[i][1]));
            i++;
        }
        //ページにセットする
        meta.spigot().addPage(list.toArray(new BaseComponent[list.size()]));

        //メタを戻す
        book.setItemMeta(meta);

        return book;
    }

    public BaseComponent createCommandComponent(String text, String command) {
        //コマンドが設定されたコンポーネントを返す
        BaseComponent component = new TextComponent(text);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return component;
    }
}
