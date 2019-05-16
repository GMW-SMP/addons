package cc.flogi.dev.megachonker.player;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-05-08
 */
public class PlayerManager {
    @Getter private static PlayerManager instance = new PlayerManager();
    @Getter private ArrayList<GamePlayer> gamePlayers = new ArrayList<>();

    private PlayerManager() {
    }

    /**
     * Returns the ${@link GamePlayer} of the specified player.
     *
     * @param player The player to retrieve a ${@link GamePlayer} object of.
     * @return The ${@link GamePlayer} object of the given player.
     */
    public GamePlayer getGamePlayer(Player player) {
        return getGamePlayer(player.getUniqueId());
    }

    /**
     * Returns the ${@link GamePlayer} of the specified player.
     *
     * @param uuid The uuid to retrieve a ${@link GamePlayer} object of.
     * @return The ${@link GamePlayer} object of the given player.
     */
    public GamePlayer getGamePlayer(UUID uuid) {
        return gamePlayers.stream()
                       .filter(gp -> gp.getPlayer().getUniqueId().equals(uuid))
                       .findFirst().orElse(null);
    }

    public void addPlayers(Player... players) {
        gamePlayers.addAll(Arrays.stream(players).map(GamePlayer::new).collect(Collectors.toList()));
    }

    public void removePlayers(Player... players) {
        gamePlayers.removeAll(Arrays.stream(players).map(this::getGamePlayer).collect(Collectors.toList()));
    }

    public void playerLogout(Player player) {
        new BukkitRunnable() {
            @Override public void run() {
                if (!player.isOnline())
                    removePlayers(player);
            }
        };
    }
}
