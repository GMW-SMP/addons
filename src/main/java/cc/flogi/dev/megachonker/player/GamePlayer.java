package cc.flogi.dev.megachonker.player;

import cc.flogi.dev.megachonker.util.UtilCountdown;
import lombok.Data;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-05-08
 */
@Data public class GamePlayer {
    private Player player;
    private ArrayList<UtilCountdown> activeCountdowns;
    private boolean recentlyBad;

    public GamePlayer(Player player) {
        this.player = player;
    }
}
