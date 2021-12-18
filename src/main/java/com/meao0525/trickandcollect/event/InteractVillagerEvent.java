package com.meao0525.trickandcollect.event;

import com.meao0525.trickandcollect.TrickandCollect;
import com.meao0525.trickandcollect.item.GameItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class InteractVillagerEvent implements Listener {

    private TrickandCollect plugin;

    public InteractVillagerEvent(TrickandCollect plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void InteractVillagerEventListener(PlayerInteractEntityEvent e) {
        //村人をクリックした
        if (!(e.getRightClicked().getCustomName().equalsIgnoreCase("取り立て屋"))) { return; }
        //デフォのイベントキャンセル
        e.setCancelled(true);

        //プレイヤー取得
        Player player = e.getPlayer();
        for (GameItems i : GameItems.values()) {
            //手に持ってるアイテムが納品アイテムか
            ItemStack inMainHand = player.getInventory().getItemInMainHand();
            if (inMainHand.getType().equals(i.getMaterial())) {
                //納品アイテム持ってたら回収
                int dif = collectItem(i.getIndex(), i.getAmount(), inMainHand);
                //回収しまーす
                player.getInventory().getItemInMainHand().setAmount(dif);
                return;
            }
        }

        //該当アイテム持ってないので進捗表示
        player.openInventory(plugin.getCollects());

    }

    public int collectItem(int index, int max, ItemStack item) {
        //アイテムを納品する処理
        Inventory collects = plugin.getCollects();
        //現在の納品数、新しい納品数
        int currentAmount, newAmount;

        newAmount = item.getAmount();
        if (collects.contains(item.getType())) {
            //同じアイテムすでに納品済み
//            int sum = collects.getItem(index).getAmount() + item.getAmount();
//            if (amount - sum < 0) {
//                //あふれてるよ
//                collects.setItem(index, new ItemStack(item.getType(), amount));
//            } else {
//                collects.addItem(item);
//            }
            currentAmount = collects.getItem(index).getAmount();
        } else {
            //そのアイテムは君が最初の納品者だ
//            collects.setItem(index, item);
            currentAmount = 0;
        }

        if (currentAmount + newAmount > max) {
            //あふれちゃう
            collects.setItem(index, new ItemStack(item.getType(), max));
            //差分を返す
            return currentAmount + newAmount - max;
        } else {
            //足した数をセット
            collects.setItem(index, new ItemStack(item.getType(), currentAmount+newAmount));
            //全部もらいまーす
            return 0;
        }
    }
}
