package com.meao0525.trickandcollect;

import com.meao0525.trickandcollect.command.CommandTabCompleter;
import com.meao0525.trickandcollect.command.GameCommand;
import com.meao0525.trickandcollect.event.DefaultGameEvent;
import com.meao0525.trickandcollect.event.InteractVillagerEvent;
import com.meao0525.trickandcollect.item.GameItems;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class TrickandCollect extends JavaPlugin {
    // ゲームフラグ
    private boolean game = false;

    //プレイヤーリスト
    private ArrayList<Player> tcPlayers = new ArrayList<>();
    //取り立て屋
    Villager collector;

    // 初期地点
    private Location spawnPoint;
    //収集進捗格納用
    Inventory collects;
    //TODO: アイテムリスト

    //TODO: 初期地点に戻させるアイテム
    //TODO: 盗めるアイテム

    //TODO: ゲーム内イベントありかも

    @Override
    public void onEnable() {
        // 起動時
        getLogger().info("だまして集める");
        //コマンド設定
        getCommand("tc").setExecutor(new GameCommand(this));
        //タブ保管できるようにする
        getCommand("tc").setTabCompleter(new CommandTabCompleter());

    }

    @Override
    public void onDisable() {
        // 停止時
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new DefaultGameEvent(this), this);
        getServer().getPluginManager().registerEvents(new InteractVillagerEvent(this), this);
    }

    public void start() {
        // 始める
        game = true;
        //初期地点を設定
        spawnPoint = Bukkit.getWorlds().get(0).getSpawnLocation();
        //プレイヤーリスト作成
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getGameMode().equals(GameMode.CREATIVE)) {
                tcPlayers.add(p);
            }
        }

        /***村人の作り方***/
        World world = Bukkit.getWorlds().get(0);
        //今回の取り立て屋
        collector = (Villager) world.spawnEntity(spawnPoint, EntityType.VILLAGER);
        collector.setAI(false);
        collector.setCustomName("取り立て屋");
        collector.setCustomNameVisible(true);
        //インベントリを与える
        collects = Bukkit.createInventory(collector, 18, "目標アイテム");

        for (Player p : tcPlayers) {
            //TODO: 初期地点を設定する
            //TODO: 目標アイテム設置する
            //TODO: チーム振り分け
            //インベントリ
            setGameInventory(p);

            //TODO: 全員を同じ場所に飛ばす
        }

        //TODO: タイマースタート

        //イベント登録
        registerEvents();

    }

    public void stop() {
        // 終える
        game = false;
        //村人は用済み
        collector.damage(8000);
        //インベントリ空にする
        for (Player p : tcPlayers) {
            //インベントリを殻にする
            p.getInventory().clear();
        }
    }

    //ゲーム開始時のインベントリ作るやつ
    void setGameInventory(Player player) {
        Inventory inv = player.getInventory();
        //まず空っぽ
        inv.clear();
        //TODO: ツール渡す

        //目標アイテム表示
        for (int i = 0; i < GameItems.values().length; i++) {
            inv.setItem(i+9, GameItems.values()[i].toItemStack());
        }
        //残り枠をバリアブロックにする
        for (int i = 27; i < 36; i++) {
            inv.setItem(i, new ItemStack(Material.BARRIER));
        }
    }

    //げったんせったん

    public boolean isGame() {
        return game;
    }

    public void setGame(boolean game) {
        this.game = game;
    }

    public Location getSpawnPoint() {
        return spawnPoint;
    }

    public void setSpawnPoint(Location spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    public Villager getCollector() {
        return collector;
    }

    public Inventory getCollects() {
        return collects;
    }
}
