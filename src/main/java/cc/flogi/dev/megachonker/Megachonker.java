package cc.flogi.dev.megachonker;

import cc.flogi.dev.megachonker.listener.PlayerEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Megachonker extends JavaPlugin {
    @Getter private static Megachonker instance;

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(new PlayerEvent(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
