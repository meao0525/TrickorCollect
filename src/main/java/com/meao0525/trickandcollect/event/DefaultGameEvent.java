package com.meao0525.trickandcollect.event;

import com.meao0525.trickandcollect.TrickandCollect;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class DefaultGameEvent implements Listener {

    private TrickandCollect plugin;

    public DefaultGameEvent(TrickandCollect plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void InventoryHoldItemEventLitener(InventoryClickEvent e) {
        //インベントリアイテム取れなくする
        if (!plugin.isGame()) {
            return;
        }

        //プレイヤーのクイックバー（手持ち）以外のインベントリですかって話
        InventoryType type = e.getClickedInventory().getType();
        if (type.equals(InventoryType.PLAYER) && e.getSlotType().equals(InventoryType.SlotType.CONTAINER)) {
            e.setCancelled(true);
        }

        //TODO: 目標インベントリでも駄目ですよ
    }

}
