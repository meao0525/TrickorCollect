package com.meao0525.trickorcollect;

import com.meao0525.trickorcollect.command.CommandTabCompleter;
import com.meao0525.trickorcollect.command.GameCommand;
import com.meao0525.trickorcollect.event.*;
import com.meao0525.trickorcollect.event.gameevent.*;
import com.meao0525.trickorcollect.event.gameevent.GameEvent;
import com.meao0525.trickorcollect.gameevent.GameEventID;
import com.meao0525.trickorcollect.item.AdminBook;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.*;

public final class TrickorCollect extends JavaPlugin {
    // ゲームフラグ
    private boolean game = false;

    //プレイヤーリスト
    private ArrayList<Player> tcPlayers = new ArrayList<>();
    //人狼人数
    private int traitorNum = 1;
    //取り立て屋
    Villager collector;

    // 初期地点
    private Location spawnPoint;
    //収集進捗格納用
    Inventory collects;
    //収集アイテム保存用
    private ArrayList<ItemStack> collectItems = new ArrayList<>();
    private int itemCount = 0;

    //タイマー
    private int time = 20; //分
    GameTimer timer;
    private BossBar timerBar;

    //ゲーム内イベントフラグ
    private boolean gameEventFlag = true;

    //スコアボード
    private ScoreboardManager manager;
    private Scoreboard info; //設定用
    private boolean infoFlag = false;

    //チーム
    private Team collectorTeam;
    private Team traitorTeam;

    //投票プレイヤー <プレイヤー, 投票した人リスト>
    private HashMap<Player, ArrayList<Player>> voteMap = new HashMap<>();
    //追放者リスト
    private ArrayList<Player> exiled = new ArrayList<>();

    //モード
    private String mode = "default";


    @Override
    public void onEnable() {
        // 起動時
        getLogger().info("だまして集める");
        //コマンド設定
        getCommand("tc").setExecutor(new GameCommand(this));
        //タブ保管できるようにする
        getCommand("tc").setTabCompleter(new CommandTabCompleter());
        //初期地点を設定
        spawnPoint = Bukkit.getWorlds().get(0).getSpawnLocation();
        spawnPoint.setX(spawnPoint.getX() + 0.5);
        spawnPoint.setZ(spawnPoint.getZ() + 0.5);
        //タイマーバー作成
        timerBar = Bukkit.createBossBar("残り時間:", BarColor.GREEN, BarStyle.SOLID);
        //スコアボード設定
        manager = Bukkit.getScoreboardManager();
        info = manager.getNewScoreboard();
        reloadInfo();
        //チーム登録
        registerTeam(manager.getMainScoreboard());
        //イベント登録
        registerEvents();
    }

    @Override
    public void onDisable() {
        // 停止時
        if (collector != null) {
            collector.remove();
        }
    }

    public void registerEvents() {
        HandlerList.unregisterAll(this);
        getServer().getPluginManager().registerEvents(new DefaultGameEvent(this), this);
        getServer().getPluginManager().registerEvents(new InteractVillagerEvent(this), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnEvent(this), this);
        getServer().getPluginManager().registerEvents(new PlayerStealItemEvent(this), this);
        getServer().getPluginManager().registerEvents(new PlayerGameChatEvent(this), this);
        getServer().getPluginManager().registerEvents(new PlayerVoteEvent(this), this);
    }

    public void start() {
        // 始める
        if (game == true) {
            return;
        } else {
            game = true;
        }
        //プレイヤーリスト作成
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getGameMode().equals(GameMode.SPECTATOR)) {
                tcPlayers.add(p);
            }
        }

        //収集アイテムの保存
        createCollectItemList();
        //ゲームプレイヤー設定
        makeTcPlayers();
        //取り立て屋のインベントリを空にする
        collects.clear();
        //光らせる
        collector.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, time*60*20, 1, true, false));
        //ノーマルモードにする
        Bukkit.getWorlds().get(0).setDifficulty(Difficulty.NORMAL);
        //追放者リスト空にする
        exiled.clear();

        Bukkit.broadcastMessage(ChatColor.GOLD + "[Trick or Collect]" + ChatColor.RESET + "Game Start!");
        //イベント登録
        registerEvents();
        //タイマースタート
        timer = new GameTimer(this, time);
        timer.runTaskTimer(this, 0, 20);
    }

    public void stop() {
        // 終える
        game = false;
        //結果表示
        result();
        //取り立て屋のインベントリを戻す
        for (int i=0; i<27; i++) {
            ItemStack item = collectItems.get(i);
            if (!item.getType().equals(Material.BARRIER)) {
                collects.setItem(i, item);
            }
        }
        //保存用リストを空にする
        collectItems.clear();
        //光を消す
        collector.removePotionEffect(PotionEffectType.GLOWING);
        itemCount = 0;
        //タイマー止める
        timer.cancel();

        for (Player p : tcPlayers) {
            //チーム解散
            if (collectorTeam.hasEntry(p.getDisplayName())) {
                collectorTeam.removeEntry(p.getDisplayName());
            } else {
                traitorTeam.removeEntry(p.getDisplayName());
            }
            //インベントリを殻にする
            p.getInventory().clear();
            //スコアボード変更
            p.setScoreboard(info);
            //タイマー非表示
            timerBar.removePlayer(p);
            //アドベンチャーモードにする
            p.setGameMode(GameMode.ADVENTURE);
            //エフェクト
            p.sendTitle("", ChatColor.GOLD + "--- finish ---", 0, 60, 20);
            p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.3F, 0.5F);
            //OPだったら管理者ブック + クリエ
            if (p.isOp()) {
                p.getInventory().addItem(new AdminBook().toItemStack());
                p.setGameMode(GameMode.CREATIVE);
            }
        }
        //ピースフルにする
        Bukkit.getWorlds().get(0).setDifficulty(Difficulty.PEACEFUL);

        //プレイヤーリストを空にする
        tcPlayers.clear();
    }

    //取り立て屋（村人）作る
    public void summonCollector() {
        if (collector == null) {
            /* 村人の作り方 */
            World world = spawnPoint.getWorld();
            if (world == null) {
                Bukkit.broadcastMessage("ワールドを読み込めませんでした");
                return;
            }
            //足元のブロック
            spawnPoint.add(0, -0.1, 0);
            spawnPoint.getBlock().setType(Material.DIAMOND_BLOCK);
            spawnPoint.add(0, 0.1, 0);
            //今回の取り立て屋
            collector = (Villager) world.spawnEntity(spawnPoint, EntityType.VILLAGER);
            collector.setAI(false);
            collector.setCustomName("取り立て屋");
            collector.setCustomNameVisible(true);
            //インベントリを与える
            collects = Bukkit.createInventory(collector, 27, "目標アイテム");
            //音
            world.playSound(spawnPoint, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.5f);
        } else {
            //toggleで消す
            collector.remove();
            collector = null;
        }
    }

    //収集アイテム保存するやつ
    public void createCollectItemList() {
        for (int i=0; i<27; i++) {
            ItemStack item = collects.getItem(i);
            if (item != null) {
                //同じの取得してもスタックしないためのNBT追加
                ItemMeta meta = item.getItemMeta();
                meta.setCustomModelData(1);
                item.setItemMeta(meta);
                collectItems.add(item);
                itemCount++;
            } else {
                collectItems.add(new ItemStack(Material.BARRIER));
            }
        }
    }

    //ゲーム開始時の各プレイヤー設定
    public void makeTcPlayers() {
        //チーム振り分け
        Collections.shuffle(tcPlayers);
        int tcount = traitorNum;

        for (Player p : tcPlayers) {
            //初期地点を設定する
            p.setBedSpawnLocation(spawnPoint, true);
            //ゲームモード
            p.setGameMode(GameMode.SURVIVAL);
            String msg;
            //チーム振り分け
            if (tcount > 0) {
                //まだ裏切者を作れる
                traitorTeam.addEntry(p.getDisplayName());
                msg = ChatColor.DARK_RED + "あなたはtraitorになりました...";
                tcount--;
            } else {
                //残りは集める人
                collectorTeam.addEntry(p.getDisplayName());
                msg = ChatColor.AQUA + "あなたはcollectorになりました";
            }
            //インベントリ
            setGameInventory(p);

            //全員を同じ場所に飛ばす
            p.teleport(spawnPoint);
            //スコアボード変更
            p.setScoreboard(manager.getMainScoreboard());
            //サバイバルモードに設定
            p.setGameMode(GameMode.SURVIVAL);
            //タイマー表示
            timerBar.addPlayer(p);
            //合図は大事
            p.sendMessage(msg);
            p.sendTitle("", msg, 10, 120, 20);
            p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 3.0F, 3.0F);
        }
    }

    //ゲーム開始時のインベントリ作るやつ
    public void setGameInventory(Player player) {
        Inventory inv = player.getInventory();
        //まず空っぽ
        inv.clear();

        //インベントリに目標アイテムを表示
        for (int i = 0; i < 27; i++) {
            ItemStack item = collectItems.get(i);
            //目標アイテムをコピーしていく
            inv.setItem(i+9, item);
        }

        //ツール渡す
        for (ItemStack item : createToolsSet()) {
            inv.addItem(item);
        }
    }

    public HashSet<ItemStack> createToolsSet() {
        HashSet<ItemStack> tools = new HashSet<>();
        //ツールセット作成
        tools.add(new ItemStack(Material.IRON_SWORD));
        tools.add(new ItemStack(Material.IRON_PICKAXE));
        tools.add(new ItemStack(Material.IRON_AXE));
        tools.add(new ItemStack(Material.IRON_SHOVEL));
        //食い物やるよ
        tools.add(new ItemStack(Material.COOKED_COD, 64));

        return tools;
    }

    public void toggleInfo() {
        infoFlag = !infoFlag;
        //一応リロード
        reloadInfo();
        //スコアボード切り替え
        Scoreboard board;
        if (infoFlag) {
            board = info;
        } else {
            board = manager.getMainScoreboard();
        }
        //全員に表示
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setScoreboard(board);
        }
    }

    public void reloadInfo() {
        //基本情報
        Objective obj = info.getObjective("info");
        //一度消す
        if (obj != null) {
            obj.unregister();
        }
        //再登録
        obj = info.registerNewObjective("info", "dummy", ChatColor.GOLD + "[Trick or Collect]");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int i = 0;
        //Traitorの数
        Score score = obj.getScore("Traitor: " + ChatColor.AQUA + traitorNum);
        score.setScore(i--);
        //スポーン地点
        score = obj.getScore("Spawn: " + ChatColor.AQUA + spawnPoint.getBlockX() + " " + spawnPoint.getBlockY() + " " + spawnPoint.getBlockZ());
        score.setScore(i--);
        //制限時間
        score = obj.getScore("Time: " + ChatColor.AQUA + time);
        score.setScore(i);
        //ゲーム内イベント
        score = obj.getScore("Event: " + ChatColor.AQUA + gameEventFlag);
        score.setScore(i);
    }

    public void registerTeam(Scoreboard board) {
        //チーム設定
        collectorTeam = board.getTeam("collectorteam");
        traitorTeam = board.getTeam("traitorteam");
        //なかったら作る
        if (collectorTeam == null) {
            collectorTeam = board.registerNewTeam("collectorteam");
            collectorTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            collectorTeam.setAllowFriendlyFire(true);
            collectorTeam.setColor(ChatColor.WHITE);
        }
        if (traitorTeam == null) {
            traitorTeam = board.registerNewTeam("traitorteam");
            traitorTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            traitorTeam.setAllowFriendlyFire(true);
            traitorTeam.setColor(ChatColor.WHITE);
        }
    }

    //結果表示
    public void result() {
        Bukkit.broadcastMessage(ChatColor.GOLD + "==========[ Result ]===========");
        //アイテムのリザルト表示
        for (int i = 0; i < collectItems.size(); i++) {
            ItemStack item = collectItems.get(i);
            //バリアは飛ばす
            if (item.getType().equals(Material.BARRIER)) { continue; }

            String name = item.getType().name(); //アイテム名
            int max; //必要な数
            int result; //集めた数

            max = item.getAmount();
            if (collects.getItem(i) == null) {
                result = 0;
            } else {
                result = collects.getItem(i).getAmount();
            }
            //表示 (アイテム名 --- ◯ / ◯)
            Bukkit.broadcastMessage(ChatColor.AQUA + name + ChatColor.RESET + " --- " + result + " / " + max);
        }

        //人狼の正体
        Bukkit.broadcastMessage(ChatColor.GOLD + "==========[ Traitors ]===========");
        for (String name : traitorTeam.getEntries()) {
            //表示
            Bukkit.broadcastMessage(ChatColor.DARK_RED + name + ChatColor.RESET + ", ");
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
        this.spawnPoint.setX(spawnPoint.getBlockX() + 0.5);
        this.spawnPoint.setZ(spawnPoint.getBlockZ() + 0.5);
        if (collector != null) {
            //足元のブロック
            spawnPoint.add(0, -0.1, 0);
            spawnPoint.getBlock().setType(Material.DIAMOND_BLOCK);
            spawnPoint.add(0, 0.1, 0);
            collector.teleport(spawnPoint);
        }
        reloadInfo();
    }

    public Villager getCollector() {
        return collector;
    }

    public Inventory getCollects() {
        return collects;
    }

    public ArrayList<ItemStack> getCollectItems() {
        return collectItems;
    }

    public int getItemCount() {
        return itemCount;
    }

    public int getTraitorNum() {
        return traitorNum;
    }

    public void setTraitorNum(int traitorNum) {
        this.traitorNum = traitorNum;
        reloadInfo();
    }

    public Scoreboard getInfo() {
        return info;
    }

    public ArrayList<Player> getTcPlayers() {
        return tcPlayers;
    }

    public Team getCollectorTeam() {
        return collectorTeam;
    }

    public Team getTraitorTeam() {
        return traitorTeam;
    }

    public ArrayList<Player> getExiled() {
        return exiled;
    }

    public HashMap<Player, ArrayList<Player>> getVoteMap() {
        return voteMap;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void setGameEventFlag(boolean gameEventFlag) {
        this.gameEventFlag = gameEventFlag;
        reloadInfo();
    }

    public boolean isGameEventFlag() {
        return gameEventFlag;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    //タイマー用内部クラス
    private class GameTimer extends BukkitRunnable {

        private TrickorCollect plugin;
        private int time;
        private double maxTime;
        private GameEvent gameEvent;


        GameTimer(TrickorCollect plugin, int time) {
            this.plugin = plugin;
            this.maxTime = time * 60;
            this.time = time * 60;
        }

        @Override
        public void run() {
            if (time > 0) {
                timerBar.setTitle("残り時間:" + time/60 + "m " + time%60 + "s");
                timerBar.setProgress(time/maxTime);
                //タイマーバーの色を変えるよ
                if (time/maxTime < 0.5) {
                    timerBar.setColor(BarColor.YELLOW);
                } else if (time/maxTime < 0.2) {
                    timerBar.setColor(BarColor.RED);
                }
                //最後のカウントダウン
                if (time < 6) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.3F, 0.5F);
                    }
                }
                //ゲーム内イベント発生
                if (gameEventFlag && time/maxTime == 0.5) {
                    createGameEvent();
                }
            } else {
                //ゲーム終了
                stop();
            }
            //1秒減らす
            time--;
        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
            //ゲーム内イベントキャンセル
            if (gameEvent != null) {
                gameEvent.cancel();
            }
            //タイマーキャンセル
            super.cancel();
        }

        //ゲーム内イベント発生！
        public void createGameEvent() {
            //乱数生成
            GameEventID gameEventID = GameEventID.getRandomGameEvent(mode);
            //イベント分岐
            switch (gameEventID) {
                case SHUFFLE_POSITION:
                    //ランダムに座標を入れ替える
                    gameEvent = new ShufflePositionGameEvent(plugin);
                    break;

                case SHUFFLE_INVENTORY:
                    //ランダムにインベントリを入れ替える
                    gameEvent = new ShuffleInventoryGameEvent(plugin);
                    break;

                case RAID_BATTLE:
                    //襲撃者バトル
                    gameEvent = new RaidBattleGameEvent(plugin);
                    break;

                case BLOCK_LIE:
                    //ブロックが嘘つき始める(Aprilfool)
                    gameEvent = new AprilBlockLieGameEvent(plugin);
                    break;
            }
            //イベント登録
            getServer().getPluginManager().registerEvents(gameEvent, plugin);
        }
    }
}
