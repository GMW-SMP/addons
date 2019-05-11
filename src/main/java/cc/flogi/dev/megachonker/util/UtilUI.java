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
     * @param player  The player to receive the message.
     * @param message The message to be sent (with color codes to be replaced).
     */
    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(colorize(message)));
    }

    /**
     * Sends an action bar message to the player.
     *
     * @param player  The player to receive the message.
     * @param message The message to be sent (with color codes to be replaced).
     */
    public static void sendActionBarSynchronous(Player player, String message) {
        new BukkitRunnable() {
            @Override public void run() {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(colorize(message)));
            }
        }.runTask(Megachonker.getInstance());
    }

    /**
     * Sends a player a title.
     *
     * @param player The player to receive the title.
     * @param title The text to be displayed. (Color codes supported)
     * @param fadeIn The fade in duration in ticks.
     * @param stay The stay duration in ticks.
     * @param fadeOut The fade out duration in ticks.
     */
    public static void sendTitle(Player player, String title, int fadeIn, int stay, int fadeOut) {
        sendTitle(player, title, "", 20, 40, 20);
    }

    /**
     * Sends a player a title & subtitle.
     *
     * @param player The player to receive the title.
     * @param title The text to be displayed. (Color codes supported)
     * @param subtitle The subtitle text to be displayed. (Color codes supported)
     * @param fadeIn The fade in duration in ticks.
     * @param stay The stay duration in ticks.
     * @param fadeOut The fade out duration in ticks.
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(colorize(title), colorize(subtitle), fadeIn, stay, fadeOut);
    }

    /**
     * Shorthand notation for ${@link ChatColor#translateAlternateColorCodes(char, String)}.
     *
     * @param string The string to be colorized.
     * @return The colorized string.
     */
    public static String colorize(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
