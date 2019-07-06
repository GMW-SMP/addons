package cc.flogi.dev.megachonker.player;

import cc.flogi.dev.megachonker.Megachonker;
import cc.flogi.dev.megachonker.util.Cooldown;
import cc.flogi.dev.megachonker.util.UtilUI;
import lombok.Data;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
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

    GamePlayer(Player player) {
        FileConfiguration config = Megachonker.getInstance().getConfig();

        this.player = player;
        activeCountdowns = new ArrayList<>();

        String color = config.getString("players." + player.getUniqueId().toString() + ".name-color");
        if (color != null)
            nameColor = ChatColor.valueOf(color.toUpperCase());
    }

    public void interruptCooldowns(String message) {
        List<Cooldown> cooldownsFiltered = activeCountdowns.stream()
                                                   .filter(Cooldown::isInterruptable)
                                                   .collect(Collectors.toList());

        cooldownsFiltered.forEach(Cooldown::cancel);
        activeCountdowns.removeAll(cooldownsFiltered);

        if (cooldownsFiltered.size() > 0)
            UtilUI.sendActionBar(player, "&4&lCANCELLED &8- &7" + message);
    }
}
