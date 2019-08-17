package cc.flogi.smp.player;

import cc.flogi.smp.SMP;
import cc.flogi.smp.util.UtilFile;
import com.google.gson.Gson;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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
    @Getter private static PlayerManager instance = new PlayerManager();
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
        return gamePlayers.stream()
                       .filter(gp -> gp.getPlayer().getUniqueId().equals(uuid))
                       .findFirst().orElse(null);
    }

    public void addPlayers(Player... players) {
        gamePlayers.addAll(Arrays.stream(players).map(this::loadFromFile).collect(Collectors.toList()));
    }

    public void removePlayers(Player... players) {
        gamePlayers.removeAll(Arrays.stream(players).map(this::getGamePlayer).collect(Collectors.toList()));
    }

    public void playerLogout(Player player) {
        new BukkitRunnable() {
            @Override public void run() {
                if (!player.isOnline()) {
                    saveToFile(getGamePlayer(player));
                    removePlayers(player);
                }
            }
        };
    }

    public GamePlayer loadFromFile(Player player) {
        File dataFile = getDataFile(player.getUniqueId());
        if (dataFile == null || dataFile.length() < 4) {
            return new GamePlayer(player);
        }

        String serializedData = UtilFile.read(dataFile);

        GamePlayer gamePlayer = GSON.fromJson(serializedData, GamePlayer.class);

        gamePlayer.setPlayer(player);
        gamePlayer.setName(player.getName());
        gamePlayer.setActiveCountdowns(new ArrayList<>());
        return gamePlayer;
    }

    public void saveToFile(GamePlayer player) {
        new BukkitRunnable() {
            @Override public void run() {
                UtilFile.writeAndCreate(player, getDataFile(player.getUuid()));
            }
        }.runTaskAsynchronously(SMP.get());
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
