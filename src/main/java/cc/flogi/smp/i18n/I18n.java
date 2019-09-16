package cc.flogi.smp.i18n;

import cc.flogi.smp.util.UtilUI;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

/**
 * @author Caden Kriese
 *
 * Created on 9/15/19.
 */
public class I18n {
    private static HashMap<String, Properties> locales = new HashMap<String, Properties>() {{
        put("en_us", load("en_us"));
        //put("en_gb", load("en_gb"));
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

        if (locale == null || locale.get(key) == null)
            locale = locales.get("en_us");

        sendText(player, UtilUI.format(locale.getProperty(key), variables), prefixed, false);
    }

    /**
     * Sends an error message to a player in their locale.
     *
     * @param player    The player to send the error message to.
     * @param key       The key of the error message to send the player.
     * @param prefixed  Should the error message have the locale's error prefix string appended to the beginning.
     * @param variables The variables to replace in the message, formatted as: key, value, key, value, etc.
     */
    public static void sendError(Player player, String key, boolean prefixed, String... variables) {
        Properties locale = locales.get(player.getLocale());

        if (locale == null || locale.get(key) == null)
            locale = locales.get("en_us");

        sendText(player, UtilUI.format(locale.getProperty(key), variables), prefixed, true);
    }

    @SuppressWarnings("Convert2MethodRef")
    private static void sendText(Player player, String message, boolean prefixed, boolean error) {
        Properties locale = locales.get(player.getLocale());

        message = UtilUI.colorize(message);

        BaseComponent[] parsed = ComponentSerializer.parse(message);
        String parsedString = StringUtils.join(Arrays.stream(parsed)
                                                       .map(baseComponent -> baseComponent.toPlainText())
                                                       .toArray(String[]::new));

        String prefixKey = error ? "error_prefix" : "prefix";

        String prefix = locale.getProperty(prefixKey);
        if (prefix == null)
            prefix = locales.get("en_us").getProperty(prefixKey);

        if (parsedString.equals(message))
            player.sendMessage(prefixed ? prefix + message : message);
        else {
            if (prefixed) {
                BaseComponent[] prefixedMessage = new ComponentBuilder(UtilUI.colorize(prefix)).append(parsed).create();
                player.sendMessage(prefixedMessage);
            } else
                player.sendMessage(parsed);
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
