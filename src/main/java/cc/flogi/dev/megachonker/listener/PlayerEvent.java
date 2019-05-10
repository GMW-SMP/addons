package cc.flogi.dev.megachonker.listener;

import cc.flogi.dev.megachonker.Megachonker;
import cc.flogi.dev.megachonker.player.GamePlayer;
import cc.flogi.dev.megachonker.player.GamePlayerManager;
import cc.flogi.dev.megachonker.util.UtilUI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
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
    private String[] badWords = new String[]{"nigga", "nigger", "retard", "chink", "faggot"};

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

        String color = player.isOp() ? "&c" : "&3";
        event.setFormat(ChatColor.translateAlternateColorCodes('&', color + player.getName() + " &8: &f" + event.getMessage()));

        //if at least one doesnt match.
        if (!Arrays.stream(badWords).allMatch(word -> event.getMessage().toLowerCase().contains(word))) {
            new BukkitRunnable() {
                @Override public void run() {
                    recentlyBadPlayers.add(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 1, 1);
                    player.sendTitle(ChatColor.DARK_RED + ChatColor.BOLD.toString() + "BAD CHILD", "", 5, 70, 20);

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
        } else if (event.getMessage().toLowerCase().contains("nibba")) {
            UtilUI.sendActionBarSynchronous(player, "&aGood child.");
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            GamePlayer gamePlayer = GamePlayerManager.getInstance().getGamePlayer((Player) event.getEntity());
            gamePlayer.interruptCooldowns("Damage taken.");
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        //Ensure they didnt just move their mouse.
        if (event.getTo() != null && event.getFrom().distance(event.getTo()) > 0) {
            GamePlayer gamePlayer = GamePlayerManager.getInstance().getGamePlayer(event.getPlayer());
            gamePlayer.interruptCooldowns("Movement detected.");
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (recentlyBadPlayers.contains(event.getEntity())) {
            event.setDeathMessage(ChatColor.RED + event.getEntity().getName() + " was a discriminatory cunt.");
            recentlyBadPlayers.remove(event.getEntity());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        GamePlayerManager.getInstance().addPlayers(event.getPlayer());
        event.setJoinMessage("");

        if (event.getPlayer().isOp())
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&8[&a+&8] &c" + event.getPlayer().getName()));
        else
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&8[&a+&8] &3" + event.getPlayer().getName()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        GamePlayerManager.getInstance().playerLogout(event.getPlayer());
        event.setQuitMessage("");

        if (event.getPlayer().isOp())
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&8[&c-&8] &c" + event.getPlayer().getName()));
        else
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&8[&c-&8] &3" + event.getPlayer().getName()));
    }
}
