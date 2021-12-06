package com.meao0525.trickandcollect.event;

import com.meao0525.trickandcollect.TrickandCollect;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;

public class DefaultGameEvent implements Listener {

    private TrickandCollect plugin;

    public DefaultGameEvent(TrickandCollect plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void InventoryHoldItemEventLitener(InventoryClickEvent e) {
        //TODO: インベントリアイテム取れなくする
    }

}
