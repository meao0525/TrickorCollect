package com.meao0525.trickorcollect.event;

import com.meao0525.trickorcollect.TrickorCollect;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerVoteEvent implements Listener {

    private TrickorCollect plugin;
    //投票
    /*
     * 取り立て屋の足元のブロックをクリックで投票
     * 投票してる人からはアイテムを盗まれない
     * 過半数の投票で盗めなくなる
     */
    //投票プレイヤー
    private HashMap<Player, ArrayList<Player>> vote;
    //投票インベントリ
    private Inventory voteInv;

    public PlayerVoteEvent(TrickorCollect plugin) {
        this.plugin = plugin;
        vote = new HashMap<>();
        this.voteInv = createVoteinventory();

        //投票テーブル作るよ
        for (Player p : plugin.getTcPlayers()) {
            ArrayList<Player> list = new ArrayList<>();
            vote.put(p, list);
        }
    }

    @EventHandler
    public void voteBlockInteractEventListener(PlayerInteractEvent e) {
        //とうひょうぶろっくをクリックした
        if (!plugin.isGame()) { return; }

        //クリックした人、クリックされたブロック取得
        Player player = e.getPlayer();
        Block target = e.getClickedBlock();
        if (target == null) { return; }

        //ブロックは投票ブロックですかぁ？
        Location loc = target.getLocation();
        loc.add(0, 1, 0);
        if (loc.equals(plugin.getSpawnPoint().getBlock().getLocation())) {
            //投票用インベントリを開く
            player.openInventory(voteInv);
        }
    }

    @EventHandler
    public void voteEventLister(InventoryClickEvent e) {
        //投票インベントリですかぁ？？？
        if (e.getView().getTitle().equalsIgnoreCase("VOTE")) {
            //とりまキャンセル
            e.setCancelled(true);
            //ゲーム中はだめよ
            if (!plugin.isGame()) { return; }

            //クリックしたアイテム取得
            ItemStack item = e.getCurrentItem();
            if (item == null) { return; }
            //投票した人、された人取得
            Player player = (Player) e.getWhoClicked();
            String name = item.getItemMeta().getDisplayName();
            Player voted = Bukkit.getPlayer(name);
            //投票リストに追加
            ArrayList<Player> list = vote.get(player);
            list.add(voted);
            player.sendMessage(list.get(0).getDisplayName());
        }
    }

    public Inventory createVoteinventory() {
        //投票インベントリをつくる
        int invSize = plugin.getTcPlayers().size();
        //TODO: 頭の悪いif文
        if (invSize <= 9) {
            invSize = 9;
        } else if (invSize <= 18) {
            invSize = 18;
        } else if (invSize <= 27) {
            invSize = 27;
        } else if (invSize <= 36) {
            invSize = 36;
        } else if (invSize <= 45) {
            invSize = 45;
        } else if (invSize <= 54) {
            invSize = 54;
        }
        Inventory inv = Bukkit.createInventory(null, invSize, "VOTE");

        for (Player p : plugin.getTcPlayers()) {
            //プレイヤーヘッド取得
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            //ヘッドの名前をPlayer名にする
            ItemMeta meta = head.getItemMeta();
            meta.setDisplayName(p.getDisplayName());
            head.setItemMeta(meta);
            //TODO: Loreに投票してる人を表示ぃ！？
            //ヘッドを格納
            inv.addItem(head);
        }

        return inv;
    }
}
