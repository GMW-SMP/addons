package cc.flogi.smp.player;

import cc.flogi.smp.SMP;
import cc.flogi.smp.util.UtilFile;
import cc.flogi.smp.util.UtilThreading;
import com.google.gson.Gson;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-05-08
 */
public class PlayerManager {
    @Getter private static final PlayerManager instance = new PlayerManager();

    private final Gson GSON = new Gson();
    @Getter private final List<GamePlayer> gamePlayers = new ArrayList<>();
    @Getter private final File dataDir = new File(SMP.get().getDataFolder().getPath() + "/data");

    private PlayerManager() {
        if (!dataDir.exists()) {
            try {
                if (!dataDir.mkdirs()) {
                    throw new IOException("Failed to create data directory.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        for (GamePlayer player : gamePlayers) {
            if (player.getUuid().equals(uuid))
                return player;
        }

        return null;
    }

    /**
     * Adds a list of players to the current list.
     *
     * @param players The players to remove.
     */
    public void addPlayers(Player... players) {
        gamePlayers.addAll(Arrays.stream(players).map(this::loadFromFile).collect(Collectors.toList()));
    }

    /**
     * Removes a list of players to the current list.
     *
     * @param players The players to remove.
     */
    private void removePlayers(Player... players) {
        gamePlayers.removeAll(Arrays.stream(players).map(this::getGamePlayer).collect(Collectors.toList()));
    }

    /**
     * Performs log out tasks for a player, such as, writing data and closing their object.
     *
     * @param player The player that is logging out.
     */
    public void playerLogout(Player player) {
        UtilThreading.syncDelayed(() -> {
            if (!player.isOnline()) {
                saveToFile(getGamePlayer(player));
                removePlayers(player);
            }
        }, 200L);
    }

    /**
     * Loads a GamePlayer from a stored data file.
     * The file will be created if one isn't found.
     *
     * @param player The player to search for.
     * @return The {@link GamePlayer} object of the given player.
     */
    private GamePlayer loadFromFile(Player player) {
        File dataFile = getDataFile(player.getUniqueId());
        if (dataFile == null || dataFile.length() < 4) {
            return new GamePlayer(player);
        }

        String serializedData = UtilFile.read(dataFile);

        GamePlayer gamePlayer = GSON.fromJson(serializedData, GamePlayer.class);

        gamePlayer.setName(player.getName());
        gamePlayer.setActiveCountdowns(new ArrayList<>());
        return gamePlayer;
    }

    /**
     * Saves a players data to a flatfile (asynchronously).
     *
     * @param player The player who's data should be saved.
     */
    public void saveToFile(GamePlayer player) {
        UtilThreading.async(() -> UtilFile.writeAndCreate(player, getDataFile(player.getUuid())));
    }

    private File getDataFile(UUID uuid) {
        File dataFile = new File(dataDir.getAbsolutePath() + "/" + uuid.toString() + ".json");
        if (!dataFile.exists()) {
            try {
                if (!dataFile.createNewFile()) {
                    throw new IOException("Failed to create data file for '" + uuid + "'.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        return dataFile;
    }
}
