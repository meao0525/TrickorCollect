package com.meao0525.trickandcollect;

import com.meao0525.trickandcollect.command.CommandTabCompleter;
import com.meao0525.trickandcollect.command.GameCommand;
import com.meao0525.trickandcollect.event.DefaultGameEvent;
import com.meao0525.trickandcollect.event.InteractVillagerEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class TrickandCollect extends JavaPlugin {
    // ゲームフラグ
    private boolean game = false;

    // 初期地点
    private Location spawnPoint;
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
        //イベント登録
        registerEvents();

        //初期地点を設定
        spawnPoint = Bukkit.getWorlds().get(0).getSpawnLocation();

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
        //TODO: 初期地点を設定する
        //TODO: 目標アイテム決める
        //TODO: 目標アイテム設置する
        //TODO: チーム振り分け
        //TODO: インベントリ制限
        //TODO: ツール・アイテムを渡す
        //TODO: 全員を同じ場所に飛ばす
        //TODO: タイマースタート

    }

    public void stop() {
        // 終える
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
}
