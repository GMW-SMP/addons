package cc.flogi.smp;

import cc.flogi.smp.command.BookmarkCommand;
import cc.flogi.smp.command.SetColorCommand;
import cc.flogi.smp.command.TitleBroadcastCommand;
import cc.flogi.smp.listener.BlockEvent;
import cc.flogi.smp.listener.PlayerEvent;
import cc.flogi.smp.player.PlayerManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("ConstantConditions") public final class SMP extends JavaPlugin {
    @Getter private static SMP instance;

    @Override public void onEnable() {
        instance = this;
        getConfig().options().copyDefaults(true);
        saveConfig();

        //Events
        Bukkit.getPluginManager().registerEvents(new PlayerEvent(), this);
        Bukkit.getPluginManager().registerEvents(new BlockEvent(), this);

        //Commands
        //getCommand("home").setExecutor(new HomeCommand());
        getCommand("setcolor").setExecutor(new SetColorCommand());
        getCommand("titlebroadcast").setExecutor(new TitleBroadcastCommand());
        getCommand("tbc").setExecutor(new TitleBroadcastCommand());
        getCommand("bookmark").setExecutor(new BookmarkCommand());

        //Classes
        PlayerManager.getInstance().addPlayers(Bukkit.getOnlinePlayers().toArray(new Player[]{}));
    }
}
