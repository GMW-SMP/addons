package cc.flogi.smp.i18n;

import cc.flogi.smp.SMP;
import cc.flogi.smp.util.UtilUI;
import com.google.gson.JsonSyntaxException;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;

/**
 * @author Caden Kriese
 *
 * Created on 9/15/19.
 */
public class I18n {
    private static HashMap<String, Properties> locales = new HashMap<String, Properties>() {{
        put("en_us", load("en_us"));
        put("en_gb", load("en_gb"));
        put("en_au", load("en_au"));
        put("en_nz", load("en_nz"));
    }};

    /**
     * Sends a message to a player in their locale.
     *
     * @param player    The player to send the message to.
     * @param key       The key of the message to send the player.
     * @param prefixed  Should the message have the locale's prefix string appended to the beginning.
     * @param variables The variables to replace in the message, formatted as: key, value, key, value, etc.
     */
    public static void sendMessage(Player player, String key, boolean prefixed, String... variables) {
        Properties locale = locales.get(player.getLocale());

        if (locale == null || locale.getProperty(key) == null)
            locale = locales.get("en_us");

        sendText(player, locale, UtilUI.format(locale.getProperty(key), variables), prefixed, false);
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
        Properties locale = locales.get(player.getLocale());

        if (locale == null || locale.getProperty(key) == null)
            locale = locales.get("en_us");

        sendText(player, locale, UtilUI.format(locale.getProperty(key), variables), prefixed, false);

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
        Properties locale = locales.get(player.getLocale());

        if (locale == null || locale.getProperty(key) == null)
            locale = locales.get("en_us");

        sendText(player, locale, UtilUI.format(locale.getProperty(key), variables), prefixed, true);

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
        Properties locale = locales.get("en_us");

        sendText(player, locale, UtilUI.format(locale.getProperty(key), variables), prefixed, true);
    }

    /**
     * Logs a specific message in the console.
     *
     * @param key       The key of the message to be sent.
     * @param level     The logging level for the message.
     * @param variables Variables to replace within the message.
     */
    public static void logMessage(String key, Level level, String... variables) {
        Properties locale = locales.get("en_us");
        SMP.get().getLogger().log(level, UtilUI.format(locale.getProperty(key), variables));
    }

    @SuppressWarnings("Convert2MethodRef")
    private static void sendText(CommandSender player, Properties locale, String message, boolean prefixed, boolean error) {
        message = UtilUI.colorize(message);

        String prefixKey = error ? "error_prefix" : "prefix";
        String prefix = locale.getProperty(prefixKey);
        if (prefix == null)
            prefix = locales.get("en_us").getProperty(prefixKey);

        //Attempt to parse with JSON otherwise use default method to send the message.
        try {
            BaseComponent[] parsed = ComponentSerializer.parse(message);
            String parsedString = StringUtils.join(Arrays.stream(parsed)
                                                           .map(baseComponent -> baseComponent.toPlainText())
                                                           .toArray(String[]::new));

            if (parsedString.equals(message))
                player.sendMessage(prefixed ? UtilUI.colorize(prefix) + message : message);
            else {
                if (prefixed) {
                    BaseComponent[] prefixedMessage = new ComponentBuilder(UtilUI.colorize(prefix)).append(parsed).create();
                    player.sendMessage(prefixedMessage);
                } else
                    player.sendMessage(parsed);
            }
        } catch (JsonSyntaxException ex) {
            player.sendMessage(prefixed ? UtilUI.colorize(prefix) + message : message);
        }
    }

    private static Properties load(String locale) {
        try {
            Properties properties = new Properties();
            properties.load(I18n.class.getResourceAsStream("/i18n/" + locale + ".properties"));
            return properties;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
