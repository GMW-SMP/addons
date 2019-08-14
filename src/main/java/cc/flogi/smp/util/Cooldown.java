package cc.flogi.smp.util;

import cc.flogi.smp.SMP;
import cc.flogi.smp.player.GamePlayer;
import cc.flogi.smp.player.PlayerManager;
import lombok.Data;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.MessageFormat;

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
     * @param player        The player to tie the cooldown to.
     * @param delay         The delay of the cooldown in ticks.
     * @param onCompletion  The runnable to be run upon completion.
     * @param message       The message to be sent to the player every 2 ticks.
     *                      It will be formatted with two variables the first is the time remaining in seconds and the second is
     *                      a string with the word 'seconds' or 'second'.
     * @param interruptable Should the cooldown be cancelled when the player moves or receives damage?
     */
    public Cooldown(Player player, int delay, Runnable onCompletion, String message, boolean interruptable) {
        new Cooldown(player, 2, delay, onCompletion, message, "", interruptable);
    }

    /**
     * Creates a cooldown object tied to a specific player.
     *
     * @param player        The player to tie the cooldown to.
     * @param delay         The delay of the cooldown in ticks.
     * @param onCompletion  The runnable to be run upon completion.
     * @param message       The message to be sent to the player every 2 ticks. It contains a few variables that will be replaced,
     *                      <p><ul>
     *                      <li>{0} - Seconds countdown.</li>
     *                      <li>{1} - 'Seconds' or 'second' when seconds = 1.</li>
     *                      <li>{2} - A progress bar.</li>
     *                      </ul></p>
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

                double delaySeconds = delay/20;

                String progressBar = "&8| "+UtilUI.progressBar(10,  delaySeconds-seconds, delaySeconds, '-', ChatColor.GOLD, ChatColor.WHITE)+" &8|";
                String secondsString = seconds == 1 ? "second" : "seconds";

                if (!isCancelled()) {
                    UtilUI.sendActionBar(player, MessageFormat.format(message,
                            String.format("%.2f", seconds),
                            secondsString,
                            progressBar
                    ));
                }

                seconds -= (countInterval / 20d);
            }
        };
    }

    public void start() {
        task.runTaskLater(SMP.get(), delay);
        timerTask.runTaskTimer(SMP.get(), 0L, 1L);
    }

    public void cancel() {
        PlayerManager.getInstance().getGamePlayer(player).getActiveCountdowns().remove(this);
        task.cancel();
        timerTask.cancel();
    }
}
