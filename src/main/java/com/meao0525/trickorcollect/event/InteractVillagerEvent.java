package com.meao0525.trickorcollect.event;

import com.meao0525.trickorcollect.TrickorCollect;
import com.meao0525.trickorcollect.item.GameItems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class InteractVillagerEvent implements Listener {

    private TrickorCollect plugin;

    public InteractVillagerEvent(TrickorCollect plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void InteractVillagerEventListener(PlayerInteractEntityEvent e) {
        //村人をクリックした
        String entityName = e.getRightClicked().getCustomName();
        if (entityName == null || !entityName.equalsIgnoreCase("取り立て屋")) { return; }
        //デフォのイベントキャンセル
        e.setCancelled(true);

        //プレイヤー取得
        Player player = e.getPlayer();
        //ゲーム中か
        if (plugin.isGame()) {
            //目標アイテム取得
            ArrayList<ItemStack> collectItems = plugin.getCollectItems();
            //手に持ってるアイテムが目標アイテムか
            ItemStack inMainHand = player.getInventory().getItemInMainHand();
            for (int i=0; i<collectItems.size(); i++) {
                if (inMainHand.getType().equals(collectItems.get(i).getType())) {
                    //納品アイテム持ってたら回収
                    int dif = collectItem(i, collectItems.get(i).getAmount(), inMainHand);
                    //回収しまーす
                    player.getInventory().getItemInMainHand().setAmount(dif);
                    //ゲーム終わったかな？？？？
                    check();
                    return;
                }
            }
            //該当アイテム持ってないので進捗表示
            player.openInventory(plugin.getCollects());

        } else {
            //ゲーム中じゃない場合は目標アイテムを設定できるよ
            player.openInventory(plugin.getCollects());
        }
    }

    public int collectItem(int index, int max, ItemStack item) {
        //アイテムを納品する処理
        Inventory collects = plugin.getCollects();
        //現在の納品数、新しい納品数
        int currentAmount, newAmount;

        newAmount = item.getAmount();
        if (collects.contains(item.getType())) {
            //同じアイテムすでに納品済み
            currentAmount = collects.getItem(index).getAmount();
        } else {
            //そのアイテムは君が最初の納品者だ
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

    public void check() {
        //ゲーム終わってたりしないよね？
        int checkcount = 0;
        //現在の納品状況
        Inventory collects = plugin.getCollects();
        //目標アイテム取得
        ArrayList<ItemStack> collectItems = plugin.getCollectItems();
        //必要数たりてるかちぇえええええっく
        for (int i=0; i<collectItems.size(); i++) {
            ItemStack current = collects.getItem(i);
            if (current != null && collectItems.get(i).getAmount() == current.getAmount()) {
                checkcount++;
            }
        }
        //半分以上が必要数に足りている
        Bukkit.broadcastMessage("現在数: " + checkcount);
        int need = plugin.getItemCount() / 2;
        if (checkcount > need) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "[Trick or Collect]" + ChatColor.RESET + "ゲームクリア！");
            plugin.stop();
        }
    }
}
