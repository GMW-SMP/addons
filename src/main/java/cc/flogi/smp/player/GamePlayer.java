package cc.flogi.smp.player;

import cc.flogi.smp.player.data.Bookmark;
import cc.flogi.smp.util.Cooldown;
import cc.flogi.smp.util.UtilUI;
import lombok.Data;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
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

    //Runtime
    private transient Player player;
    private transient boolean recentlyBad;
    private transient ArrayList<Cooldown> activeCountdowns = new ArrayList<>();

    //Serialized
    private String nameColor;
    private List<Bookmark> bookmarks;

    GamePlayer(Player player) {
        this.player = player;
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

    public void addBookmark(Location location, String name) {
        getBookmarks().add(new Bookmark(
                name,
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getPitch(),
                location.getYaw()));
    }

    public List<Bookmark> getBookmarks() {
        if (bookmarks == null)
            bookmarks = new ArrayList<>();

        return bookmarks;
    }

    public void setNameColor(ChatColor nameColor) {
        this.nameColor = nameColor.name();
    }

    public ChatColor getNameColor() {
        return nameColor == null ? null : ChatColor.valueOf(nameColor);
    }
}
