package cc.flogi.dev.megachonker.util;

import cc.flogi.dev.megachonker.Megachonker;
import cc.flogi.dev.megachonker.player.GamePlayer;
import cc.flogi.dev.megachonker.player.PlayerManager;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-05-08
 */
@Data public class Cooldown {

    private Player player;
    private BukkitRunnable task;
    private BukkitRunnable timerTask;

    private int delay;
    private int countInterval;
    private boolean interruptable;

    /**
     * Creates a cooldown object tied to a specific player.
     *
     * @param player The player to tie the cooldown to.
     * @param delay The delay of the cooldown in ticks.
     * @param onCompletion The runnable to be run upon completion.
     * @param message The message to be sent to the player every 2 ticks.
     *                It will be formatted with two variables the first is the time remaining in seconds and the second is
     *                a string with the word 'seconds' or 'second'.
     * @param interruptable Should the cooldown be cancelled when the player moves or receives damage?
     */
    public Cooldown(Player player, int delay, Runnable onCompletion, String message, boolean interruptable) {
        new Cooldown(player, 2, delay, onCompletion, message, "", interruptable);
    }

    /**
     * Creates a cooldown object tied to a specific player.
     *
     * @param player The player to tie the cooldown to.
     * @param delay The delay of the cooldown in ticks.
     * @param onCompletion The runnable to be run upon completion.
     * @param message The message to be sent to the player every 2 ticks.
     *                It will be formatted with two variables the first is the time remaining in seconds and the second is
     *                a string with the word 'seconds' or 'second'.
     * @param interruptable Should the cooldown be cancelled when the player moves or receives damage?
     */
    public Cooldown(Player player, int countInterval, int delay, Runnable onCompletion, String message, String completionMessage, boolean interruptable) {
        this.player = player;
        this.delay = delay;
        this.interruptable = interruptable;
        this.countInterval = countInterval;

        GamePlayer gamePlayer = PlayerManager.getInstance().getGamePlayer(player);
        gamePlayer.getActiveCountdowns().add(this);

        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                onCompletion.run();
            }
        };

        Cooldown toRemove = this;
        this.timerTask = new BukkitRunnable() {
            double seconds = delay / 20;

            @Override
            public void run() {
                if (seconds <= 0) {
                    UtilUI.sendActionBar(player, completionMessage);
                    gamePlayer.getActiveCountdowns().remove(toRemove);
                    this.cancel();
                    return;
                }

                String secondsString = seconds == 1 ? "second" : "seconds";

                if (!isCancelled())
                    UtilUI.sendActionBar(player, String.format(message, String.format("%.2f", seconds), secondsString));

                seconds -= (countInterval / 20d);
            }
        };
    }

    public void start() {
        task.runTaskLater(Megachonker.getInstance(), delay);
        timerTask.runTaskTimer(Megachonker.getInstance(), 0L, 1L);
    }

    public void cancel() {
        PlayerManager.getInstance().getGamePlayer(player).getActiveCountdowns().remove(this);
        task.cancel();
        timerTask.cancel();
    }
}
