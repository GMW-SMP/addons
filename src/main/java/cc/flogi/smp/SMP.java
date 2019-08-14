package cc.flogi.smp;

import cc.flogi.smp.command.BookmarkCommand;
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

@SuppressWarnings("ConstantConditions")
public final class SMP extends JavaPlugin {
    @Getter
    private static SMP INSTANCE;
    @Getter
    private ProtocolManager protocolManager;
    @Getter
    private InfluxDatabase influxDatabase;

    @Override
    public void onEnable() {
        INSTANCE = this;

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

        //Events
        Bukkit.getPluginManager().registerEvents(new PlayerEvent(influxDatabase), this);
        Bukkit.getPluginManager().registerEvents(new BlockEvent(), this);

        //Commands
        getCommand("setcolor").setExecutor(new SetColorCommand());
        getCommand("titlebroadcast").setExecutor(new TitleBroadcastCommand());
        getCommand("tbc").setExecutor(new TitleBroadcastCommand());
        getCommand("bookmark").setExecutor(new BookmarkCommand());
        getCommand("bm").setExecutor(new BookmarkCommand());
        getCommand("bookmarks").setExecutor(new BookmarkCommand());
        getCommand("marks").setExecutor(new BookmarkCommand());

        //Classes
        protocolManager = ProtocolLibrary.getProtocolManager();
        PlayerManager.getInstance().addPlayers(Bukkit.getOnlinePlayers().toArray(new Player[]{}));
    }

    public static SMP get() {
        return INSTANCE;
    }
}
