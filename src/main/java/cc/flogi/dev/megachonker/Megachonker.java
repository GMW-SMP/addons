package cc.flogi.dev.megachonker;

import cc.flogi.dev.megachonker.command.HomeCommand;
import cc.flogi.dev.megachonker.listener.PlayerEvent;
import cc.flogi.dev.megachonker.player.GamePlayerManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class Megachonker extends JavaPlugin {
    @Getter private static Megachonker instance;

    @SuppressWarnings("ConstantConditions") @Override
    public void onEnable() {
        instance = this;

        Bukkit.getPluginManager().registerEvents(new PlayerEvent(), this);
        getCommand("home").setExecutor(new HomeCommand());

        GamePlayerManager.getInstance().addPlayers(Bukkit.getOnlinePlayers().toArray(new Player[]{}));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
