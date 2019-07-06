package cc.flogi.dev.megachonker;

import cc.flogi.dev.megachonker.command.HomeCommand;
import cc.flogi.dev.megachonker.command.SetColorCommand;
import cc.flogi.dev.megachonker.command.TitleBroadcastCommand;
import cc.flogi.dev.megachonker.listener.BlockEvent;
import cc.flogi.dev.megachonker.listener.PlayerEvent;
import cc.flogi.dev.megachonker.player.PlayerManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("ConstantConditions") public final class Megachonker extends JavaPlugin {
    @Getter private static Megachonker instance;

    @Override public void onEnable() {
        instance = this;
        getConfig().options().copyDefaults(true);
        saveConfig();

        //Events
        Bukkit.getPluginManager().registerEvents(new PlayerEvent(), this);
        Bukkit.getPluginManager().registerEvents(new BlockEvent(), this);

        //Commands
        getCommand("home").setExecutor(new HomeCommand());
        getCommand("setcolor").setExecutor(new SetColorCommand());
        getCommand("titlebroadcast").setExecutor(new TitleBroadcastCommand());
        getCommand("tbc").setExecutor(new TitleBroadcastCommand());

        //Classes
        PlayerManager.getInstance().addPlayers(Bukkit.getOnlinePlayers().toArray(new Player[]{}));
    }

    @Override public void onDisable() {
    }
}
