package cc.flogi.smp.util;

import cc.flogi.smp.SMP;
import org.bukkit.Bukkit;

/**
 * @author Caden Kriese
 *
 * Created on 02/08/2020.
 */
public class UtilThreading {
    public static void async(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(SMP.get(), runnable);
    }

    public static void asyncDelayed(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(SMP.get(), runnable, delay);
    }

    public static void asyncRepeating(Runnable runnable, long delay, long period) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(SMP.get(), runnable, delay, period);
    }

    public static void sync(Runnable runnable) {
        Bukkit.getScheduler().runTask(SMP.get(), runnable);
    }

    public static void syncDelayed(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLater(SMP.get(), runnable, delay);
    }

    public static void syncRepeating(Runnable runnable, long delay, long period) {
        Bukkit.getScheduler().runTaskTimer(SMP.get(), runnable, delay, period);
    }
}
