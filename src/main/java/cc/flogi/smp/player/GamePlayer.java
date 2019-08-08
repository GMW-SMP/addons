package cc.flogi.smp.player;

import cc.flogi.smp.SMP;
import cc.flogi.smp.util.Cooldown;
import cc.flogi.smp.util.UtilUI;
import lombok.Data;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-05-08
 */
@Data public class GamePlayer {
    private Player player;
    private ArrayList<Cooldown> activeCountdowns;
    private boolean recentlyBad;
    private ChatColor nameColor;
    private HashMap<String, Location> bookmarks;

    GamePlayer(Player player) {
        this.player = player;
        activeCountdowns = new ArrayList<>();

        ConfigurationSection config = SMP.getInstance().getConfig()
                                              .getConfigurationSection("players."+player.getUniqueId().toString());

        if (config != null) {
            String color = config.getString("name-color");
            if (color != null)
                nameColor = ChatColor.valueOf(color.toUpperCase());

            ConfigurationSection bookmarks = config.getConfigurationSection("bookmarks");

            if (bookmarks != null) {
                Set<String> keys = bookmarks.getKeys(false);

                if (!keys.isEmpty()) {
                    for (String bookmark : keys) {
                        //TODO Plan out how to store the bookmarks before u start righting shit cmonBruh.
                    }
                }
            }
        }
    }

    public void save() {
        FileConfiguration config = SMP.getInstance().getConfig();

        HashMap<String, Map<String, Object>> serializedBookmarks = new HashMap<>();

        for (String key : bookmarks.keySet()) {
            serializedBookmarks.put(key, bookmarks.get(key).serialize());
        }

        config.set("players."+player.getUniqueId().toString()+".bookmarks", serializedBookmarks);
    }

    public void interruptCooldowns(String message) {
        List<Cooldown> cooldownsFiltered = activeCountdowns
                                                   .stream()
                                                   .filter(Cooldown::isInterruptable)
                                                   .collect(Collectors.toList());

        cooldownsFiltered.forEach(Cooldown::cancel);
        activeCountdowns.removeAll(cooldownsFiltered);

        if (cooldownsFiltered.size() > 0)
            UtilUI.sendActionBar(player, "&4&lCANCELLED &8- &7" + message);
    }
}
