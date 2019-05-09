package cc.flogi.dev.megachonker.util;

import cc.flogi.dev.megachonker.Megachonker;
import cc.flogi.dev.megachonker.player.GamePlayerManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-05-08
 */
@Getter @Setter
public class UtilCountdown {

    private BukkitTask task;
    private BukkitTask timerTask;
    private Player toTeleport;

    private boolean interruptable;
    private int ticksUntilTeleport;

    public UtilCountdown(Player toTeleport, int ticksUntilTeleport, Runnable runnable, String message, boolean interruptable) {
        this.toTeleport = toTeleport;
        this.ticksUntilTeleport = ticksUntilTeleport;
        this.interruptable = interruptable;

        GamePlayerManager.getInstance().getGamePlayer(toTeleport).getActiveCountdowns().add(this);

        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }.runTaskLater(Megachonker.getInstance(), ticksUntilTeleport);

        this.timerTask = new BukkitRunnable() {
                    double seconds = ticksUntilTeleport / 20;

                    @Override
                    public void run() {
                        if (seconds <= 0) {
                            UtilUI.sendActionBar(toTeleport, "&aComplete");
                            this.cancel();
                            return;
                        }

                        String secondsString = seconds == 1 ? "second" : "seconds";

                        UtilUI.sendActionBar(toTeleport,
                                "&e"+message+" &d"
                                        + String.format("%.2f", seconds)
                                        + "&e "
                                        + secondsString
                                        + ".");

                        seconds -= 0.05;
                    }
                }.runTaskTimer(Megachonker.getInstance(), 0L, 1L);
    }

    public void cancel() {
        GamePlayerManager.getInstance().getGamePlayer(toTeleport).getActiveCountdowns().remove(this);
        task.cancel();
        timerTask.cancel();
    }
}
