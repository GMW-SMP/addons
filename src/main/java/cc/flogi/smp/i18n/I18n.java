package cc.flogi.smp.i18n;

import cc.flogi.smp.SMP;
import cc.flogi.smp.util.UtilUI;
import com.google.gson.JsonSyntaxException;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;

/**
 * @author Caden Kriese
 *
 * Created on 9/15/19.
 */
public class I18n {
    private static final HashMap<String, Properties> LOCALES = new HashMap<String, Properties>() {{
        put("en_us", load("en_us"));
        put("en_gb", load("en_gb"));
        put("en_au", load("en_au"));
        put("en_nz", load("en_nz"));
    }};
    private static final Properties DEFAULT_LOCALE = LOCALES.get("en_us");

    /**
     * Sends a message to multiple players in their locale.
     *
     * @param players   The players to send the message to.
     * @param key       The key of the message to send the player.
     * @param prefixed  Should the message have the locale's prefix string appended to the beginning.
     * @param sound     Should a sound be played to all the players.
     * @param variables The variables to replace in the message, formatted as: key, value, key, value, etc.
     */
    public static void broadcastMessage(Collection<? extends Player> players, String key, boolean prefixed, boolean sound, String... variables) {
        players.forEach(pl -> sendMessage(pl, key, prefixed, sound, variables));
    }

    /**
     * Sends a message to a player in their locale.
     *
     * @param player    The player to send the message to.
     * @param key       The key of the message to send the player.
     * @param prefixed  Should the message have the locale's prefix string appended to the beginning.
     * @param variables The variables to replace in the message, formatted as: key, value, key, value, etc.
     */
    public static void sendMessage(Player player, String key, boolean prefixed, String... variables) {
        sendMessage(player, key, prefixed, false, variables);
    }

    /**
     * Sends a message to a player in their locale.
     *
     * @param player    The player to send the message to.
     * @param key       The key of the message to send the player.
     * @param prefixed  Should the message have the locale's prefix string appended to the beginning.
     * @param sound     Should the player receive a positive sound.
     * @param variables The variables to replace in the message, formatted as: key, value, key, value, etc.
     */
    public static void sendMessage(Player player, String key, boolean prefixed, boolean sound, String... variables) {
        Properties locale = getFromPlayer(player, key);
        sendText(player, locale, UtilUI.format(locale.getProperty(key), variables), prefixed, false, false);
        if (sound)
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
    }

    /**
     * Sends a message to a player in their locale.
     *
     * @param player    The player to send the message to.
     * @param key       The key of the message to send the player.
     * @param prefixed  Should the message have the locale's prefix string appended to the beginning.
     * @param variables The variables to replace in the message, formatted as: key, value, key, value, etc.
     */
    public static void sendActionBar(Player player, String key, boolean prefixed, String... variables) {
        sendMessage(player, key, prefixed, false, variables);
    }

    /**
     * Sends a message to a player in their locale.
     *
     * @param player    The player to send the message to.
     * @param key       The key of the message to send the player.
     * @param prefixed  Should the message have the locale's prefix string appended to the beginning.
     * @param sound     Should the player receive a positive sound.
     * @param variables The variables to replace in the message, formatted as: key, value, key, value, etc.
     */
    public static void sendActionBar(Player player, String key, boolean prefixed, boolean sound, String... variables) {
        Properties locale = getFromPlayer(player, key);
        sendText(player, locale, UtilUI.format(locale.getProperty(key), variables), prefixed, false, true);
        if (sound)
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
    }

    /**
     * Sends an error message to a player in their locale.
     *
     * @param player    The player to send the error message to.
     * @param key       The key of the error message to send the player.
     * @param prefixed  Should the error message have the locale's error prefix string appended to the beginning.
     * @param sound     Should the player receive a noise telling them there was an error.
     * @param variables The variables to replace in the message, formatted as: key, value, key, value, etc.
     */
    public static void sendError(Player player, String key, boolean prefixed, boolean sound, String... variables) {
        Properties locale = getFromPlayer(player, key);
        sendText(player, locale, UtilUI.format(locale.getProperty(key), variables), prefixed, true, false);
        if (sound)
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
    }

    /**
     * Sends an error message to a player in their locale.
     *
     * @param player    The player to send the error message to.
     * @param key       The key of the error message to send the player.
     * @param prefixed  Should the error message have the locale's error prefix string appended to the beginning.
     * @param variables The variables to replace in the message, formatted as: key, value, key, value, etc.
     */
    public static void sendError(CommandSender player, String key, boolean prefixed, String... variables) {
        sendText(player, DEFAULT_LOCALE, UtilUI.format(DEFAULT_LOCALE.getProperty(key), variables), prefixed, true, false);
    }

    /**
     * Logs a specific message in the console.
     *
     * @param key       The key of the message to be sent.
     * @param level     The logging level for the message.
     * @param variables Variables to replace within the message.
     */
    public static void logMessage(String key, Level level, String... variables) {
        SMP.get().getLogger().log(level, UtilUI.format(DEFAULT_LOCALE.getProperty(key), variables));
    }

    /**
     * Returns the colorized message.
     *
     * @param player    The player to retrieve the locale of.
     * @param key       The key of the message to get.
     * @param variables The variables to replace.
     */
    public static String getMessage(Player player, String key, String... variables) {
        return UtilUI.colorize(UtilUI.format(getFromPlayer(player, key).getProperty(key), variables));
    }

    @SuppressWarnings("Convert2MethodRef")
    private static void sendText(CommandSender player, Properties locale, String message, boolean prefixed, boolean error, boolean actionBar) {
        message = UtilUI.colorize(message);

        String prefixKey = error ? "error_prefix" : "prefix";
        String prefix = locale.getProperty(prefixKey);
        if (prefix == null)
            prefix = DEFAULT_LOCALE.getProperty(prefixKey);

        //Attempt to parse with JSON otherwise use default method to send the message.
        try {
            BaseComponent[] parsed = ComponentSerializer.parse(message);
            String parsedString = StringUtils.join(Arrays.stream(parsed)
                    .map(baseComponent -> baseComponent.toPlainText())
                    .toArray(String[]::new));

            if (parsedString.equals(message))
                player.sendMessage(prefixed ? UtilUI.colorize(prefix) + message : message);
            else {
                if (prefixed)
                    parsed = new ComponentBuilder(UtilUI.colorize(prefix)).append(parsed).create();

                if (actionBar && player instanceof Player)
                    ((Player) player).spigot().sendMessage(ChatMessageType.ACTION_BAR, parsed);
                else
                    player.spigot().sendMessage(parsed);
            }
        } catch (JsonSyntaxException ex) {
            String finalMsg = prefixed ? UtilUI.colorize(prefix) + message : message;
            if (actionBar && player instanceof Player)
                UtilUI.sendActionBar((Player) player, message);
            else
                player.sendMessage(finalMsg);
        }
    }

    /**
     * Retrieves the proper locale.
     *
     * @param player The player whose language should be considered.
     * @param key    The key of the message attempting to be retrieved.
     * @return The proper locale file.
     */
    private static Properties getFromPlayer(Player player, String key) {
        Properties locale = LOCALES.get(player.getLocale());
        if (locale == null || locale.getProperty(key) == null)
            locale = DEFAULT_LOCALE;
        return locale;
    }

    /**
     * Loads a locale from src/main/resources
     *
     * @param locale The key of the locale to load.
     * @return The properties class representing that localization file.
     */
    private static Properties load(String locale) {
        try {
            Properties properties = new Properties();
            properties.load(I18n.class.getResourceAsStream("/i18n/" + locale + ".properties"));
            return properties;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
