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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerVoteEvent implements Listener {

    private TrickorCollect plugin;
    //投票
    /*
     * 取り立て屋の足元のブロックをクリックで投票
     * 投票してる人からはアイテムを盗まれない
     * 過半数の投票で盗めなくなる
     */
    //投票プレイヤー <プレイヤー, 投票した人リスト>
    private HashMap<Player, ArrayList<Player>> voteMap = new HashMap<>();
    //投票されたテーブル <プレイヤー, ←に投票した人数>
    private HashMap<Player, Integer> voted = new HashMap<>();
    //過半数
    private int border;
    //投票インベントリ
    private Inventory voteInv;
    //投票スコア
    private Objective obj;
    private final String SCORE_NAME = "vote";

    public PlayerVoteEvent(TrickorCollect plugin) {
        this.plugin = plugin;
        this.voteInv = createVoteinventory();
        this.voteMap = plugin.getVoteMap();

        //今回の過半数
        border = (int)Math.ceil(plugin.getTcPlayers().size() / 2.0);
        //投票スコア
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        obj = manager.getMainScoreboard().getObjective(SCORE_NAME);
        if (obj == null) {
            obj = manager.getMainScoreboard().registerNewObjective(SCORE_NAME, "dummy", "[投票]");
        }
        obj.setDisplaySlot(DisplaySlot.PLAYER_LIST);

        for (Player p : plugin.getTcPlayers()) {
            //投票テーブル作るよ
            ArrayList<Player> list = new ArrayList<>();
            voteMap.put(p, list);
            voted.put(p, 0);
            //スコア
            obj.getScore(p.getDisplayName()).setScore(0);
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
            ItemMeta meta = item.getItemMeta();
            Player votedPlayer = Bukkit.getPlayer(meta.getDisplayName());
            //投票リストに追加
            if (voteMap.containsKey(player) && voted.containsKey(votedPlayer)) {
                ArrayList<Player> list = voteMap.get(player);
                int num = voted.get(votedPlayer);
                List<String> lore = meta.getLore();
                //既に投票済みか
                if (list.contains(votedPlayer)) {
                    //投票解除
                    list.remove(votedPlayer);
                    num--;
                    lore.remove(player.getDisplayName());
                    Bukkit.broadcastMessage(votedPlayer.getDisplayName() + " の投票が取り消されました");
                } else {
                    //投票処理
                    list.add(votedPlayer);
                    num++;
                    lore.add(player.getDisplayName());
                    Bukkit.broadcastMessage(votedPlayer.getDisplayName() + " が投票されました");
                }

                //テーブル更新
                voted.replace(votedPlayer, num);
                //スコア更新
                obj.getScore(votedPlayer.getDisplayName()).setScore(num);
                //Lore更新
                meta.setLore(lore);
                item.setItemMeta(meta);
                //ログ
                Bukkit.broadcastMessage("現在投票数： " + num);
                //追放者リスト取得
                ArrayList<Player> exiled = plugin.getExiled();
                //過半数チェック
                if (num >= border && !exiled.contains(votedPlayer)) {
                    //過半数超えており、追放リストにいないなら追加
                    exiled.add(votedPlayer);
                    //メッセージ
                    Bukkit.broadcastMessage(votedPlayer.getDisplayName() + " が追放されたぞ～！");
                } else if (num < border && exiled.contains(votedPlayer)) {
                    //過半数割っており、追放リストにいたら削除
                    exiled.remove(votedPlayer);
                    //メッセージ
                    Bukkit.broadcastMessage(votedPlayer.getDisplayName() + " の追放が解除された");
                }
            }
        }
    }

    public Inventory createVoteinventory() {
        //投票インベントリをつくる
        int invSize = plugin.getTcPlayers().size();
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
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            //ヘッドの名前をPlayer名にする
            meta.setDisplayName(p.getDisplayName());
            //スキンをPlayerのものにする
            meta.setOwningPlayer(p);
            //Loreに投票してる人を表示
            ArrayList<String> lore = new ArrayList<>();
            lore.add("↓投票してる人↓");
            meta.setLore(lore);
            head.setItemMeta(meta);
            //ヘッドを格納
            inv.addItem(head);
        }

        return inv;
    }
}
