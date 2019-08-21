package cc.flogi.smp;

import cc.flogi.smp.command.BookmarkCommand;
import cc.flogi.smp.command.MessageCommand;
import cc.flogi.smp.command.SetColorCommand;
import cc.flogi.smp.command.TitleBroadcastCommand;
import cc.flogi.smp.database.InfluxDatabase;
import cc.flogi.smp.database.influx.InfluxRetentionPolicy;
import cc.flogi.smp.listener.BlockEvent;
import cc.flogi.smp.listener.PlayerEvent;
import cc.flogi.smp.player.PlayerManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.influxdb.dto.Point;

import java.util.stream.Stream;

@SuppressWarnings("ConstantConditions")
public final class SMP extends JavaPlugin {
    private static SMP INSTANCE;

    @Getter private ProtocolManager protocolManager;
    @Getter private InfluxDatabase influxDatabase;

    @Override
    public void onEnable() {
        INSTANCE = this;

        //Events
        Bukkit.getPluginManager().registerEvents(new PlayerEvent(), this);
        Bukkit.getPluginManager().registerEvents(new BlockEvent(), this);

        //Commands
        Stream.of("setcolor", "sc")
                .map(this::getCommand)
                .forEach(cmd -> cmd.setExecutor(new SetColorCommand()));
        Stream.of("titlebroadcast", "tbc")
                .map(this::getCommand)
                .forEach(cmd -> cmd.setExecutor(new TitleBroadcastCommand()));
        Stream.of("marks", "bookmarks", "bm", "bookmark")
                .map(this::getCommand)
                .forEach(cmd -> cmd.setExecutor(new BookmarkCommand()));
        Stream.of("message", "tell", "t", "msg", "pm", "reply", "r")
                .map(this::getCommand)
                .forEach(cmd -> cmd.setExecutor(new MessageCommand()));

        //Classes
        protocolManager = ProtocolLibrary.getProtocolManager();
        PlayerManager.getInstance().addPlayers(Bukkit.getOnlinePlayers().toArray(new Player[]{}));

        // Influx
        influxDatabase = new InfluxDatabase(
                "http://127.0.0.1:8086",
                "smp",
                "ilovetomine"
        ).withDatabase(
                "smp",
                InfluxRetentionPolicy.builder()
                        .name("defaultPolicy")
                        .duration("30d")
                        .replicationPolicy(1)
                        .isDefault(true)
                        .build()
        );

        if (influxDatabase != null) {
            new BukkitRunnable() {
                @Override public void run() {
                    int loadedChunks = Bukkit.getWorlds().stream().mapToInt(world -> world.getLoadedChunks().length).sum();

                    influxDatabase.addPoint(Point.measurement("server_stats")
                                                    .addField("online_players", Bukkit.getOnlinePlayers().size())
                                                    .build()
                    );
                    influxDatabase.addPoint(Point.measurement("server_stats")
                                                    .addField("tps", Bukkit.getTPS()[0])
                                                    .build()
                    );
                    influxDatabase.addPoint(Point.measurement("server_stats")
                                                    .addField("loaded_chunks", loadedChunks)
                                                    .build()
                    );
                }
            }.runTaskTimerAsynchronously(INSTANCE, 150, 150);
        } else {
            getLogger().warning("INFLUX DATABASE CONNECTION FAILED, NO STATISTICS WILL BE WRITTEN.");
        }
    }

    public static SMP get() {
        return INSTANCE;
    }
}
