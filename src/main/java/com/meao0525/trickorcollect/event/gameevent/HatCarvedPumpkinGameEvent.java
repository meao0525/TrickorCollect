package com.meao0525.trickorcollect.event.gameevent;

import com.meao0525.trickorcollect.TrickorCollect;
import com.meao0525.trickorcollect.event.gameevent.GameEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HatCarvedPumpkinGameEvent extends GameEvent {

    public HatCarvedPumpkinGameEvent(TrickorCollect plugin) {
        super(plugin);

        //全員にかぼちゃをかぶせる
        ItemStack pumpkin = new ItemStack(Material.CARVED_PUMPKIN, 1);
        ItemMeta pumpkinMeta = pumpkin.getItemMeta();
        //名前つける
        pumpkinMeta.setDisplayName("Happy Halloween!");
        pumpkin.setItemMeta(pumpkinMeta);
        for (Player player : plugin.getTcPlayers()) {
            player.getInventory().setHelmet(pumpkin);
        }
    }

    @EventHandler
    public void clickHelmetEvent(InventoryClickEvent e) {
        //かぼちゃは脱げない
        if (!plugin.isGame()) { return; }

        //クリックされたアイテムの取得
        ItemStack item = e.getCurrentItem();
        if (item == null) { return; }
        //Materialがかぼちゃで名前が「Happy Halloween!」
        if (item.getType().equals(Material.CARVED_PUMPKIN)) {
            ItemMeta pumpkinMeta = item.getItemMeta();
            if (pumpkinMeta != null && pumpkinMeta.getDisplayName().equals("Happy Halloween!")) {
                e.setCancelled(true);
            }
        }
    }
}
