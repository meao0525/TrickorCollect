package com.meao0525.trickorcollect.event;

import com.meao0525.trickorcollect.TrickorCollect;
import com.meao0525.trickorcollect.item.GameItems;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

        //プレイヤー取得
        Player player = e.getPlayer();
        ItemStack inMainHand = player.getInventory().getItemInMainHand();
        //ゲーム中か
        if (plugin.isGame()) {
            //追放されているか
            if (plugin.getExiled().contains(player)) {
                player.sendMessage(ChatColor.RED + "追放されているためアクション出来ません");
                return;
            }
            //目標アイテム取得
            ArrayList<ItemStack> collectItems = plugin.getCollectItems();
            //手に持ってるアイテムが目標アイテムか
            for (int i=0; i<collectItems.size(); i++) {
                if (inMainHand.getType().equals(collectItems.get(i).getType())) {
                    //集め終わってるなら飛ばす
                    ItemStack item = plugin.getCollects().getItem(i);
                    if (item != null && item.getEnchantmentLevel(Enchantment.BINDING_CURSE) == 255) { continue; }
                    //納品アイテム持ってたら回収
                    int dif = collectItem(i, collectItems.get(i).getAmount(), inMainHand);
                    //回収しまーす
                    player.getInventory().getItemInMainHand().setAmount(dif);
                    //納品エフェクト
                    plugin.getCollector().playEffect(EntityEffect.VILLAGER_HAPPY);
                    plugin.getCollector().getWorld().playSound(plugin.getSpawnPoint(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 0.5f, 0.5f);
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

        //デフォのイベントキャンセル
        e.setCancelled(true);
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

        if (currentAmount + newAmount >= max) {
            //あふれちゃう
            ItemStack complete = new ItemStack(item.getType(), max);
            //完了エフェクト
            ItemMeta meta = complete.getItemMeta();
            meta.addEnchant(Enchantment.BINDING_CURSE, 255, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            complete.setItemMeta(meta);
            collects.setItem(index, complete);
            //完了通知
            String msg = ChatColor.GOLD + "[Trick or Collect]" + ChatColor.RESET + complete.getType().name() + " を集め終わりました";
            Bukkit.broadcastMessage(msg);
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
//        //半分以上が必要数に足りている
//        int need = plugin.getItemCount() / 2;
//        if (checkcount > need) {
//            Bukkit.broadcastMessage(ChatColor.GOLD + "[Trick or Collect]" + ChatColor.RESET + "ゲームクリア！");
//            plugin.stop();
//        }
        //全種類集め終わった
        int need = plugin.getItemCount();
        if (checkcount == need) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "[Trick or Collect]" + ChatColor.RESET + "ゲームクリア！");
            plugin.stop();
        }
    }
}
