package cc.flogi.dev.megachonker.listener;

import cc.flogi.dev.megachonker.Megachonker;
import cc.flogi.dev.megachonker.player.GamePlayer;
import cc.flogi.dev.megachonker.player.PlayerManager;
import cc.flogi.dev.megachonker.util.UtilUI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-05-07
 */
public class PlayerEvent implements Listener {
    private ArrayList<Player> recentlyBadPlayers = new ArrayList<>();
    private final String[] badWords = new String[]{"nigga", "nigger", "chink"};

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        Player bonn = Bukkit.getPlayer(UUID.fromString("f92a66c0-65bd-4490-bb41-6a87e3ed408e"));
        if (bonn != null) {
            if (player.getNearbyEntities(10, 10, 10).contains(bonn)) {
                event.setCancelled(true);
                UtilUI.sendActionBar(player, "You cannot sleep right now; Bonn Lafehr is nearby.");
            }
        }
    }

    @EventHandler
    public void onAsyncChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = PlayerManager.getInstance().getGamePlayer(player);

        String color = gamePlayer.getNameColor() == null ? "&7" : gamePlayer.getNameColor().toString();
        event.setFormat(UtilUI.colorize(color + player.getName() + " &8: &f" + event.getMessage()));

        if (Arrays.stream(badWords).anyMatch(word -> event.getMessage().toLowerCase().contains(word))) {
            new BukkitRunnable() {
                @Override public void run() {
                    recentlyBadPlayers.add(player);
                    UtilUI.sendTitle(player, ChatColor.DARK_RED + ChatColor.BOLD.toString() + "BAD CHILD", "", 5, 70, 20);

                    for (int i = 0; i < 6; i++) {
                        new BukkitRunnable() {
                            @Override public void run() {
                                player.getWorld().strikeLightning(player.getLocation());
                            }
                        }.runTaskLater(Megachonker.getInstance(), i * 3);
                    }

                    new BukkitRunnable() {
                        @Override public void run() {
                            recentlyBadPlayers.remove(player);
                        }
                    }.runTaskLater(Megachonker.getInstance(), 20 * 10L);
                }
            }.runTask(Megachonker.getInstance());
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            GamePlayer gamePlayer = PlayerManager.getInstance().getGamePlayer((Player) event.getEntity());
            gamePlayer.interruptCooldowns("Damage taken.");
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (PlayerManager.getInstance().getGamePlayer(event.getPlayer()).getActiveCountdowns().size() > 0) {
            //Ensure they didnt just move their mouse.
            if (event.getFrom().distance(event.getTo()) > 0) {
                GamePlayer gamePlayer = PlayerManager.getInstance().getGamePlayer(event.getPlayer());
                gamePlayer.interruptCooldowns("Movement detected.");
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (recentlyBadPlayers.contains(event.getEntity())) {
            event.setDeathMessage(event.getEntity().getName() + " died of racism.");
            recentlyBadPlayers.remove(event.getEntity());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PlayerManager.getInstance().addPlayers(event.getPlayer());
        GamePlayer gamePlayer = PlayerManager.getInstance().getGamePlayer(event.getPlayer());

        event.setJoinMessage("");

        String color = gamePlayer.getNameColor() == null ? "&7" : gamePlayer.getNameColor().toString();
        Bukkit.broadcastMessage(UtilUI.colorize("&8[&a+&8] " + color + event.getPlayer().getName()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        PlayerManager.getInstance().playerLogout(event.getPlayer());
        GamePlayer gamePlayer = PlayerManager.getInstance().getGamePlayer(event.getPlayer());

        event.setQuitMessage("");

        String color = gamePlayer.getNameColor() == null ? "&7" : gamePlayer.getNameColor().toString();
        Bukkit.broadcastMessage(UtilUI.colorize("&8[&c-&8] &3" + color + event.getPlayer().getName()));
    }
}
