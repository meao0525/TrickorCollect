package com.meao0525.trickorcollect.event;

import com.meao0525.trickorcollect.TrickorCollect;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.Random;

public class DefaultGameEvent implements Listener {

    private TrickorCollect plugin;

    public DefaultGameEvent(TrickorCollect plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void InventoryHoldItemEventLitener(InventoryClickEvent e) {
        //インベントリアイテム取れなくする
        if (!plugin.isGame()) {
            return;
        }

        //投票インベントリは虫
        if (e.getView().getTitle().equalsIgnoreCase("VOTE")) {
            return;
        }

        //プレイヤーのクイックバー（手持ち）以外のインベントリですかって話
        Inventory inv = e.getClickedInventory();
        if (inv == null) { return; }

        InventoryType type = inv.getType();
        if (type.equals(InventoryType.PLAYER) && e.getSlotType().equals(InventoryType.SlotType.CONTAINER)) {
            e.setCancelled(true);
        }

        //目標インベントリもダメ
        if (e.getView().getTopInventory().getHolder().equals(plugin.getCollector())) {
            //上に目標アイテム
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

    @EventHandler
    public void CantBreakVoteBlock(BlockBreakEvent e) {
        //投票ブロックは壊せない
        if (!plugin.isGame()) {
            return;
        }
        Location loc = e.getBlock().getLocation();
        loc.add(0, 1, 0);
        if (loc.equals(plugin.getSpawnPoint().getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void PlayerDeathEventListener(PlayerDeathEvent e) {
        if (!plugin.isGame()) {
            return;
        }
        //死んだときのログを分からなくする
        e.setDeathMessage(e.getEntity().getDisplayName() + "が死亡しました");
        //目標アイテムをぶちまけない
        for (ItemStack i : e.getDrops()) {
            ItemMeta meta = i.getItemMeta();
            if (i.getType().equals(Material.BARRIER) ||
                    meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == 1) {
                //一旦消しちゃおう
                i.setAmount(0);
            }
        }
    }

    @EventHandler
    public void PlayerRespawnEventListener(PlayerRespawnEvent e) {
        //リスポーン時にもう一度インベントリセット
        if (!plugin.isGame()) {
            return;
        }

        Player player = e.getPlayer();
        Inventory inv = player.getInventory();
        //インベントリに目標アイテムを表示
        for (int i = 0; i < 27; i++) {
            ItemStack item = plugin.getCollectItems().get(i);
            //目標アイテムをコピーしていく
            inv.setItem(i+9, item);
        }
        player.sendMessage("リスポーン");
        if (!player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY)) {
            //ランダムにツール１つと食べ物をあげる
            inv.addItem(getRandomTool());
            inv.addItem(new ItemStack(Material.COOKED_COD, 64));
        }
    }

    public ItemStack getRandomTool() {
        //ランダムなツールを返す
        ItemStack tool;
        Random rand = new Random();
        int num = rand.nextInt(4);

        if (num == 0) {
            tool = new ItemStack(Material.IRON_SWORD);
        } else if (num == 1) {
            tool = new ItemStack(Material.IRON_PICKAXE);
        } else if (num == 2) {
            tool = new ItemStack(Material.IRON_AXE);
        } else {
            tool = new ItemStack(Material.IRON_SHOVEL);
        }

        return tool;
    }

}
