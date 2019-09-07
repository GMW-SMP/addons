package cc.flogi.smp.util;

import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;

import java.util.HashMap;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-05-08
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class UtilUI {
    public static final HashMap<Character, Double> CHAR_WIDTHS = new HashMap<Character, Double>(){{
        put(':', 0.3); put('.', 0.3); put(',', 0.3); put('i', 0.3); put('|', 0.3); put('!', 0.3);
        put('t', 0.678);
        put('[', 0.703); put(']', 0.703);
        put('{', 0.863); put('}', 0.863); put('<', 0.863); put('>', 0.863); put('"', 0.863); put('*', 0.863);
        put('l', 0.5); put('`', 0.5);
        put('@', 1.1875);
    }};


    /**
     * Sends an action bar message to the player.
     *
     * @param player  The player to receive the message.
     * @param message The message to be sent (with color codes to be replaced).
     */
    public static void sendActionBar(Player player, String message) {
        player.sendActionBar('&', message);
    }

    /**
     * Sends a player a title.
     *
     * @param player  The player to receive the title.
     * @param title   The text to be displayed. (Color codes supported)
     * @param fadeIn  The fade in duration in ticks.
     * @param stay    The stay duration in ticks.
     * @param fadeOut The fade out duration in ticks.
     */
    public static void sendTitle(Player player, String title, int fadeIn, int stay, int fadeOut) {
        sendTitle(player, title, "", 20, 40, 20);
    }

    /**
     * Sends a player a title & subtitle.
     *
     * @param player   The player to receive the title.
     * @param title    The text to be displayed. (Color codes supported)
     * @param subtitle The subtitle text to be displayed. (Color codes supported)
     * @param fadeIn   The fade in duration in ticks.
     * @param stay     The stay duration in ticks.
     * @param fadeOut  The fade out duration in ticks.
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(colorize(title), colorize(subtitle), fadeIn, stay, fadeOut);
    }

    /**
     * Generates a progress bar with ChatColors.
     *
     * @param barSize     The length of the progress bar.
     * @param numerator   The numerator of the fraction representing progress on the bar.
     * @param denominator The denominator of the fraction representing progress on the bar.
     * @param barChar     The character the progress bar is made out of.
     * @param used        The color representing the used section of the bar.
     * @param free        The color representing the free section of the bar.
     * @return The generated progress bar.
     */
    public static String progressBar(int barSize, double numerator, double denominator, char barChar, ChatColor used, ChatColor free) {
        String bar = StringUtils.repeat(barChar, barSize);
        int usedAmount = (int) (numerator / denominator * barSize);
        bar = used + bar.substring(0, usedAmount) + free + bar.substring(usedAmount);
        return bar;
    }

    /**
     * Counts the number of lines the given string would take up in a book.
     *
     * @param line The text to be parsed.
     * @return The number of lines the given text would take up in a book.
     */
    public static int countLines(String line) {
        double length = 0;
        line = UtilUI.strip(line);

        length += StringUtils.countMatches(line, "\n");

        for (char ch : line.toCharArray()) {
            Double width = CHAR_WIDTHS.get(ch);

            if (width != null)
                length += width;
            else
                length++;
        }

        return (int) Math.ceil(length / 19);
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

    /**
     * Shorthand notation for ${@link ChatColor#stripColor(String)}
     *
     * @param string The string to clear.
     * @return The string without any color.
     */
    public static String strip(String string) {
        return ChatColor.stripColor(string);
    }
}
