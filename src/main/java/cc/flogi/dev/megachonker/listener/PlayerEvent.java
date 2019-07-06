package cc.flogi.dev.megachonker.listener;

import cc.flogi.dev.megachonker.Megachonker;
import cc.flogi.dev.megachonker.player.GamePlayer;
import cc.flogi.dev.megachonker.player.PlayerManager;
import cc.flogi.dev.megachonker.util.UtilUI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-05-07
 */
public class PlayerEvent implements Listener {
    private final String[] blacklistedWords = new String[]{"nigga", "nigger", "chink"};
    private ArrayList<Player> recentlyBadPlayers = new ArrayList<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        GamePlayer gp = PlayerManager.getInstance().getGamePlayer(player);

        if (!event.isCancelled() && event.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.OK) {
            if (player.getBedSpawnLocation() == null || player.getBedSpawnLocation().distance(event.getBed().getLocation()) > 2) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                player.sendMessage(UtilUI.colorize("&8[&aMegachonker&8] &7Bed spawn location set. Use /home to teleport to this location."));
                UtilUI.sendActionBar(player, "Bed spawn location set.");
            }

            ChatColor nameColor = gp.getNameColor() == null ? ChatColor.GRAY : gp.getNameColor();

            Bukkit.broadcastMessage(nameColor + player.getName() + ChatColor.GRAY + " has entered a bed.");

            ArrayList<Player> otherPlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
            otherPlayers.remove(player);

            if (otherPlayers.size() == 0 || otherPlayers.stream().allMatch(Player::isSleeping))
                Bukkit.broadcastMessage(ChatColor.GRAY + "All players are sleeping, cycling to daylight.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = PlayerManager.getInstance().getGamePlayer(player);

        String color = gamePlayer.getNameColor() == null ? "&7" : gamePlayer.getNameColor().toString();
        event.setFormat(UtilUI.colorize(color + player.getName() + " &8: &f" + event.getMessage()));

        if (Arrays.stream(blacklistedWords).anyMatch(word -> event.getMessage().toLowerCase().contains(word))) {
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

        if (!event.isCancelled()) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (player != onlinePlayer && event.getMessage().contains(onlinePlayer.getName())) {
                    onlinePlayer.sendMessage(event.getFormat().replace(onlinePlayer.getName(), ChatColor.YELLOW + onlinePlayer.getName() + ChatColor.RESET));
                    onlinePlayer.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                } else {
                    onlinePlayer.sendMessage(event.getFormat());
                }
            }

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();

            if (event.getClickedInventory() instanceof AnvilInventory) {
                if (event.getSlot() == 2 && event.getCurrentItem() != null && event.getCurrentItem().getItemMeta().hasDisplayName()) {
                    if (event.getCurrentItem().getItemMeta().getDisplayName().contains("\u00A7")) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                        player.sendMessage(UtilUI.colorize("&8[&aMegachonker&8] &7Colorized item name."));
                    }
                }
            }
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
            //Clone to avoid modifying the from location.
            Location diff = event.getFrom().clone().subtract(event.getTo());
            if (Math.abs(diff.getBlockX()) == 1 || Math.abs(diff.getBlockY()) == 1 || Math.abs(diff.getBlockZ()) == 1) {
                GamePlayer gamePlayer = PlayerManager.getInstance().getGamePlayer(event.getPlayer());
                gamePlayer.interruptCooldowns("Movement detected.");
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (recentlyBadPlayers.contains(event.getEntity()) && event.getDeathMessage().contains("burned ")) {
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
