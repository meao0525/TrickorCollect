package com.meao0525.trickorcollect.event.gameevent;

import com.meao0525.trickorcollect.TrickorCollect;
import org.bukkit.event.Listener;

public class GameEvent implements Listener {

    protected TrickorCollect plugin;

    public GameEvent(TrickorCollect plugin) {
        this.plugin = plugin;
    }
}
