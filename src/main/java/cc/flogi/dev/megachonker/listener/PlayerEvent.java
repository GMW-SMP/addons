package cc.flogi.dev.megachonker.listener;

import cc.flogi.dev.megachonker.Megachonker;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("You cannot sleep right now; Bonn Lafehr is nearby."));
            }
        }
    }

    @EventHandler
    public void onAsyncChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        //if at least one doesnt match.
        if (!Arrays.stream(badWords).allMatch(word -> event.getMessage().toLowerCase().contains(word))) {
            new BukkitRunnable() {
                @Override public void run() {
                    recentlyBadPlayers.add(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 1, 1);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.DARK_RED+"BAD CHILD"));

                    for (int i = 0; i < 10; i++) {
                        new BukkitRunnable() {
                            @Override public void run() {
                                player.getWorld().strikeLightning(player.getLocation());
                            }
                        }.runTaskLater(Megachonker.getInstance(), i*2);
                    }
                }
            }.runTask(Megachonker.getInstance());
        } else if (event.getMessage().toLowerCase().contains("nibba")) {
            new BukkitRunnable() {
                @Override public void run() {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN+"Good Child"));
                }
            }.runTask(Megachonker.getInstance());
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
        if (event.getPlayer().getName().equals("fl0gic") || event.getPlayer().getName().equals("Memorys_"))
            event.setJoinMessage(ChatColor.translateAlternateColorCodes('&', "&8[&a+&8] &c"+event.getPlayer().getName()));
        else
            event.setJoinMessage(ChatColor.translateAlternateColorCodes('&', "&8[&a+&8] &3"+event.getPlayer().getName()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (event.getPlayer().getName().equals("fl0gic") || event.getPlayer().getName().equals("Memorys_"))
            event.setQuitMessage(ChatColor.translateAlternateColorCodes('&', "&8[&c-&8] &c"+event.getPlayer().getName()));
        else
            event.setQuitMessage(ChatColor.translateAlternateColorCodes('&', "&8[&c-&8] &3"+event.getPlayer().getName()));
    }
}
