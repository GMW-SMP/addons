package cc.flogi.smp;

import cc.flogi.smp.command.*;
import cc.flogi.smp.database.InfluxDatabase;
import cc.flogi.smp.database.influx.InfluxRetentionPolicy;
import cc.flogi.smp.listener.BlockEvent;
import cc.flogi.smp.listener.ExperienceEvent;
import cc.flogi.smp.listener.PlayerEvent;
import cc.flogi.smp.player.GamePlayer;
import cc.flogi.smp.player.PlayerManager;
import cc.flogi.smp.util.UtilThreading;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import lombok.Getter;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.influxdb.dto.Point;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * SMP Plugin
 *
 * Written by Caden Kriese
 *
 * @since 5/8/2019
 *
 * Copyright © 2019 Caden "flogic" Kriese
 * This code is not to be redistributed or modified in any way internally or commercially.
 */
public final class SMP extends JavaPlugin {
    private static SMP INSTANCE;
    private final long STAT_PUSH_INTERVAL = 150;
    @Getter private ProtocolManager protocolManager;
    @Getter private InfluxDatabase influxDatabase;

    public static SMP get() {
        return INSTANCE;
    }

    @Override public void onEnable() {
        INSTANCE = this;
        protocolManager = ProtocolLibrary.getProtocolManager();

        //Events
        protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO) {
            @Override public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                PlayerInfoData data = packet.getPlayerInfoDataLists().read(0).get(0);
                Player player = Bukkit.getPlayer(data.getProfile().getUUID());
                if (player != null) {
                    GamePlayer gp = PlayerManager.getInstance().getGamePlayer(player);
                    String colorizedName = ComponentSerializer.toString(new ComponentBuilder(player.getName()).color(gp.getNameColor()).create());
                    data = new PlayerInfoData(data.getProfile().withName(""), data.getLatency(), data.getGameMode(), WrappedChatComponent.fromJson(colorizedName));
                    packet.getPlayerInfoDataLists().write(0, Collections.singletonList(data));
                    event.setPacket(packet);
                }
            }
        });

        Bukkit.getPluginManager().registerEvents(new ExperienceEvent(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerEvent(), this);
        Bukkit.getPluginManager().registerEvents(new BlockEvent(), this);

        //Commands
        Stream.of("home", "bedtp", "h")
                .map(this::getCommand).filter(Objects::nonNull)
                .forEach(cmd -> cmd.setExecutor(new HomeCommand()));
        Stream.of("setcolor", "setcolour", "sc")
                .map(this::getCommand).filter(Objects::nonNull)
                .forEach(cmd -> cmd.setExecutor(new SetColorCommand()));
        Stream.of("titlebroadcast", "tbc")
                .map(this::getCommand).filter(Objects::nonNull)
                .forEach(cmd -> cmd.setExecutor(new TitleBroadcastCommand()));
        Stream.of("marks", "bookmarks", "bm", "bookmark")
                .map(this::getCommand).filter(Objects::nonNull)
                .forEach(cmd -> cmd.setExecutor(new BookmarkCommand()));
        Stream.of("message", "tell", "t", "msg", "pm", "reply", "r")
                .map(this::getCommand).filter(Objects::nonNull)
                .forEach(cmd -> cmd.setExecutor(new MessageCommand()));

        //Classes
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
            getLogger().warning("Connection to InfluxDB failed. (" + ex.getMessage() + ")");
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
}
