package cc.flogi.smp;

import cc.flogi.smp.command.BookmarkCommand;
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

@SuppressWarnings("ConstantConditions") public final class SMP extends JavaPlugin {
    @Getter private static SMP instance;
    @Getter private ProtocolManager protocolManager;

    @Override public void onEnable() {
        instance = this;

        //Events
        Bukkit.getPluginManager().registerEvents(new PlayerEvent(), this);
        Bukkit.getPluginManager().registerEvents(new BlockEvent(), this);

        //Commands
        getCommand("setcolor").setExecutor(new SetColorCommand());
        getCommand("titlebroadcast").setExecutor(new TitleBroadcastCommand());
        getCommand("tbc").setExecutor(new TitleBroadcastCommand());
        getCommand("bookmark").setExecutor(new BookmarkCommand());

        //Classes
        protocolManager = ProtocolLibrary.getProtocolManager();
        PlayerManager.getInstance().addPlayers(Bukkit.getOnlinePlayers().toArray(new Player[]{}));
    }
}
