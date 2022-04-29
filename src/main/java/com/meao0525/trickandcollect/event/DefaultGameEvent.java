package com.meao0525.trickandcollect.event;

import com.meao0525.trickandcollect.TrickandCollect;
import org.bukkit.Bukkit;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

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

        //目標インベントリでも駄目ですよ
        if (e.getClickedInventory().getHolder().equals(plugin.getCollector())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void CollectorDamageEventListener(EntityDamageEvent e) {
        //ダメージを受けたのは取り立て屋ですか？
        if (plugin.isGame() && (e.getEntity() instanceof Villager)) {
            Villager villager = (Villager) e.getEntity();
            if (villager.getName().equalsIgnoreCase("取り立て屋")) {
                //取り立て屋は不死身です
                e.setCancelled(true);
            }
        }
    }

}
