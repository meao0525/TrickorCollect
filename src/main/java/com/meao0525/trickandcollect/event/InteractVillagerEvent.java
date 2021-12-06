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

public class InteractVillagerEvent implements Listener {

    private TrickandCollect plugin;

    public InteractVillagerEvent(TrickandCollect plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void InteractVillagerEventListener(PlayerInteractEntityEvent e) {
        //村人をクリックした
        if (!(e.getRightClicked() instanceof Villager)) { return; }
        //デフォのイベントキャンセル
        e.setCancelled(true);

        //プレイヤー取得
        Player player = e.getPlayer();
        //目標アイテムインベントリ表示
        player.openInventory(createrInvetory());
    }

    public Inventory createrInvetory() {
        //表示用目標インベントリ作成
        Inventory passInv = Bukkit.createInventory(null, 18, "目標アイテム");
        //アイテム追加
        for (int i=0; i < GameItems.values().length; i++) {
            passInv.setItem(i, GameItems.values()[i].toItemStack());
        }

        return passInv;
    }
}
