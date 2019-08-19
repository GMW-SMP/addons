package cc.flogi.smp;

import cc.flogi.smp.command.BookmarkCommand;
import cc.flogi.smp.command.MessageCommand;
import cc.flogi.smp.command.SetColorCommand;
import cc.flogi.smp.command.TitleBroadcastCommand;
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
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import java.util.stream.Stream;

@SuppressWarnings("ConstantConditions")
public final class SMP extends JavaPlugin {
    private static SMP INSTANCE;

    @Getter private ProtocolManager protocolManager;
    @Getter private InfluxDB influxDatabase;

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
        influxDatabase = InfluxDBFactory.connect("http://localhost:8086", "smp", "ilovetomine");
        if (influxDatabase != null) {
            influxDatabase.createDatabase("smp");
            influxDatabase.createRetentionPolicy("defaultPolicy", "smp", "30d", 1, true);

            new BukkitRunnable() {
                @Override public void run() {
                    influxDatabase.write("smp", "defaultPolicy", Point.measurement("online_players")
                                                                         .addField("online", Bukkit.getOnlinePlayers().size())
                                                                         .build()
                    );
                }
            }.runTaskTimerAsynchronously(INSTANCE, 100, 100);
        } else {
            getLogger().warning("INFLUX DATABASE CONNECTION FAILED, NO STATISTICS WILL BE WRITTEN.");
        }
    }

    public static SMP get() {
        return INSTANCE;
    }
}
