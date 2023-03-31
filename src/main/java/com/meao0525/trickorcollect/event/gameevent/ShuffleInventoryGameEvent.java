package com.meao0525.trickorcollect.event.gameevent;

import com.meao0525.trickorcollect.TrickorCollect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;

public class ShuffleInventoryGameEvent extends GameEvent {

    public ShuffleInventoryGameEvent(TrickorCollect plugin) {
        super(plugin);
        //ログ
        Bukkit.broadcastMessage(ChatColor.GOLD + "[Trick or Collect]" + ChatColor.RESET + "持ち物シャッフルイベント発生！！！");
        Bukkit.broadcastMessage(ChatColor.GRAY + "全員のインベントリがシャッフルされました");
        for (Player player : plugin.getTcPlayers()) {
            //効果音
            player.playSound(player, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 0.1f);
        }

        //ランダムにインベントリを入れ替える
        ArrayList<ArrayList<ItemStack>> inventories = new ArrayList<>();
        //全員のインベントリ取得
        for (Player player : plugin.getTcPlayers()) {
            //ホットバーを取得
            ArrayList<ItemStack> hotBar = new ArrayList<>();
            for (int index = 0; index < 9; index++) {
                hotBar.add(player.getInventory().getItem(index));
            }
            //オフハンドのアイテムも取得
            hotBar.add(player.getInventory().getItemInOffHand());
            //インベントリ記録
            inventories.add(hotBar);
        }
        //SHUFFLE
        Collections.shuffle(inventories);
        //順番に変更していく
        int invIdx = 0;
        for (Player player : plugin.getTcPlayers()) {
            ArrayList<ItemStack> hotBar = inventories.get(invIdx++);
            //まずはホットバーを書き換え
            int index;
            for (index = 0; index < 9; index++) {
                player.getInventory().setItem(index, hotBar.get(index));
            }
            //オフハンド書き換え
            player.getInventory().setItemInOffHand(hotBar.get(index));
        }
    }

    @Override
    public void cancel() {
        //キャンセル処理なし
    }
}
