package com.meao0525.trickorcollect.gameevent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public enum GameEventID {
    //--- default ---
    SHUFFLE_POSITION("default"),
    SHUFFLE_INVENTORY("default"),
    RAID_BATTLE("default"),
    //--- aprilfool ---
    BLOCK_LIE("aprilfool");

    private String mode;

    GameEventID(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }

    //全てのイベントのうちランダムなIDを返す
    public static GameEventID getRandomGameEvent() {
        //全GameEventID取得
        ArrayList<GameEventID> gameEventIDList = new ArrayList<>(Arrays.asList(GameEventID.values()));
        //乱数生成
        Random random = new Random();
        int randomNum = random.nextInt(gameEventIDList.size());

        return gameEventIDList.get(randomNum);
    }

    //指定モードのイベントのうちランダムなIDを返す
    public static GameEventID getRandomGameEvent(String mode) {
        //全GameEventID取得
        ArrayList<GameEventID> gameEventIDList = new ArrayList<>();
        for (GameEventID id : GameEventID.values()) {
            if (id.getMode().equals(mode)) {
                gameEventIDList.add(id);
            }
        }
        //乱数生成
        Random random = new Random();
        int randomNum = random.nextInt(gameEventIDList.size());

        return gameEventIDList.get(randomNum);
    }
}
