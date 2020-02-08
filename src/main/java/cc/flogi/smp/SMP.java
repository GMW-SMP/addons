package cc.flogi.smp;

import cc.flogi.smp.command.*;
import cc.flogi.smp.database.InfluxDatabase;
import cc.flogi.smp.database.influx.InfluxRetentionPolicy;
import cc.flogi.smp.listener.BlockEvent;
import cc.flogi.smp.listener.PlayerEvent;
import cc.flogi.smp.player.PlayerManager;
import cc.flogi.smp.util.UtilThreading;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.influxdb.dto.Point;

import java.util.stream.Stream;

/**
 * SMP Plugin
 *
 * Written by Caden Kriese
 * @since 5/8/2019
 *
 * Copyright © 2019 Caden "flogic" Kriese
 * This code is not to be redistributed or modified in any way internally or commercially.
 */
@SuppressWarnings({"ConstantConditions", "FieldCanBeLocal"})
public final class SMP extends JavaPlugin {
    private static SMP INSTANCE;

    @Getter private ProtocolManager protocolManager;
    @Getter private InfluxDatabase influxDatabase;

    private final long STAT_PUSH_INTERVAL = 150;

    @Override public void onEnable() {
        INSTANCE = this;

        //Events
        Bukkit.getPluginManager().registerEvents(new PlayerEvent(), this);
        Bukkit.getPluginManager().registerEvents(new BlockEvent(), this);

        //Commands
        Stream.of("setcolor", "setcolour", "sc")
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
        Stream.of("smpwhitelist")
                .map(this::getCommand)
                .forEach(cmd -> cmd.setExecutor(new SMPWhitelistCommand()));

        //Classes
        protocolManager = ProtocolLibrary.getProtocolManager();
        PlayerManager.getInstance().addPlayers(Bukkit.getOnlinePlayers().toArray(new Player[]{}));

        // Influx
        try {
            influxDatabase = new InfluxDatabase(
                    "http://localhost:8086",
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
        } catch (Exception ex) {
            influxDatabase = null;
            getLogger().warning("Connection to InfluxDB failed. ("+ex.getMessage()+")");
        }

        if (influxDatabase != null) {
            UtilThreading.asyncRepeating(() -> {
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
            }, STAT_PUSH_INTERVAL, STAT_PUSH_INTERVAL);
        } else {
            getLogger().warning("INFLUX DATABASE CONNECTION FAILED, NO STATISTICS WILL BE WRITTEN.");
        }
    }

    public static SMP get() {
        return INSTANCE;
    }
}
