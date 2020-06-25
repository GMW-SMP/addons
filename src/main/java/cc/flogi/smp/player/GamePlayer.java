package cc.flogi.smp.player;

import cc.flogi.smp.player.data.Bookmark;
import cc.flogi.smp.util.Cooldown;
import cc.flogi.smp.util.UtilUI;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-05-08
 */
@Data public class GamePlayer {

    //Runtime
    private transient boolean recentlyBad;
    private transient UUID lastMessaged;
    private transient ArrayList<Cooldown> activeCountdowns = new ArrayList<>();

    //Serialized
    @SerializedName("name") private String name;
    @SerializedName("unique-id") private UUID uuid;
    @SerializedName("name-color") private String nameColor;
    @SerializedName("bookmarks") private List<Bookmark> bookmarks;

    GamePlayer(Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
    }

    public void interruptCooldowns(String message) {
        List<Cooldown> cooldownsFiltered = activeCountdowns
                                                   .stream()
                                                   .filter(Cooldown::isInterruptable)
                                                   .collect(Collectors.toList());

        cooldownsFiltered.forEach(Cooldown::cancel);
        activeCountdowns.removeAll(cooldownsFiltered);

        if (cooldownsFiltered.size() > 0)
            UtilUI.sendActionBar(getPlayer(), "&4&lCANCELLED &8- &7" + message);
    }

    public void addBookmark(Location location, String name) {
        getBookmarks().add(new Bookmark(
                name,
                location.getWorld().getName(),
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

    public boolean removeBookmark(String name) {
        Bookmark bookmark = bookmarks.stream()
                                    .filter(mark -> mark.getName().equalsIgnoreCase(name))
                                    .findFirst()
                                    .orElse(null);

        bookmarks.remove(bookmark);

        return bookmark != null;
    }

    public ChatColor getNameColor() {
        return nameColor == null ? ChatColor.GRAY : ChatColor.of(nameColor);
    }

    public void setNameColor(ChatColor nameColor) {
        this.nameColor = nameColor.getName();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }
}
