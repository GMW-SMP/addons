package cc.flogi.dev.megachonker.util;

import cc.flogi.dev.megachonker.Megachonker;
import cc.flogi.dev.megachonker.player.GamePlayer;
import cc.flogi.dev.megachonker.player.GamePlayerManager;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-05-08
 */
@Data
public class Cooldown {

    private BukkitRunnable task;
    private BukkitRunnable timerTask;
    private Player toTeleport;

    private boolean interruptable;
    private int ticksUntilTeleport;

    public Cooldown(Player toTeleport, int ticksUntilTeleport, Runnable runnable, String message, String completionMessage, boolean interruptable) {
        this.toTeleport = toTeleport;
        this.ticksUntilTeleport = ticksUntilTeleport;
        this.interruptable = interruptable;

        GamePlayer gamePlayer = GamePlayerManager.getInstance().getGamePlayer(toTeleport);

        gamePlayer.getActiveCountdowns().add(this);

        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        };

        Cooldown toRemove = this;
        this.timerTask = new BukkitRunnable() {
            double seconds = ticksUntilTeleport / 20;

            @Override
            public void run() {
                if (seconds <= 0) {
                    UtilUI.sendActionBar(toTeleport, completionMessage);
                    gamePlayer.getActiveCountdowns().remove(toRemove);
                    this.cancel();
                    return;
                }

                String secondsString = seconds == 1 ? "second" : "seconds";

                UtilUI.sendActionBar(toTeleport,
                        "&6" + message + " &e"
                                + String.format("%.2f", seconds)
                                + "&6 "
                                + secondsString
                                + ".");

                seconds -= 0.05;
            }
        };
    }

    public void start() {
        task.runTaskLater(Megachonker.getInstance(), ticksUntilTeleport);
        timerTask.runTaskTimer(Megachonker.getInstance(), 0L, 1L);
    }

    public void cancel() {
        GamePlayerManager.getInstance().getGamePlayer(toTeleport).getActiveCountdowns().remove(this);
        task.cancel();
        timerTask.cancel();
    }
}
