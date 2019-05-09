package cc.flogi.dev.megachonker.util;

import cc.flogi.dev.megachonker.Megachonker;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-05-08
 */
public class UtilUI {
    /**
     * Sends an action bar message to the player.
     *
     * @param player The player to receive the message.
     * @param message The message to be sent (with color codes to be replaced).
     */
    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
    }

    /**
     * Sends an action bar message to the player.
     *
     * @param player The player to receive the message.
     * @param message The message to be sent (with color codes to be replaced).
     */
    public static void sendActionBarSynchronous(Player player, String message) {
        new BukkitRunnable() {
            @Override public void run() {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
            }
        }.runTask(Megachonker.getInstance());
    }
}
