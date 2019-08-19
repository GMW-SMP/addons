package cc.flogi.smp.listener;

import cc.flogi.smp.SMP;
import cc.flogi.smp.player.GamePlayer;
import cc.flogi.smp.player.PlayerManager;
import cc.flogi.smp.util.UtilUI;
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

import java.util.*;

/**
 * @author Caden Kriese (flogic)
 * <p>
 * Created on 2019-05-07
 */
@SuppressWarnings("ALL")
public class PlayerEvent implements Listener {

    private final String[] blacklistedWords = new String[]{"nigga", "nigger", "neegar", "kneegar"};
    private final Set<UUID> recentlyBadPlayers = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        GamePlayer gp = PlayerManager.getInstance().getGamePlayer(player);

        if (!event.isCancelled() && event.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.OK) {
            if (player.getBedSpawnLocation() == null || player.getBedSpawnLocation().distance(event.getBed().getLocation()) > 2) {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                player.sendMessage(UtilUI.colorize("&8[&aSMP&8] &7Bed spawn location set."));
                UtilUI.sendActionBar(player, "Bed spawn location set.");
            }

            Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1));
            Bukkit.broadcastMessage(gp.getNameColor() + player.getName() + ChatColor.GRAY + " has entered a bed.");

            ArrayList<Player> otherPlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
            otherPlayers.remove(player);

            if (otherPlayers.size() == 0 || otherPlayers.stream().allMatch(Player::isSleeping))
                Bukkit.broadcastMessage(ChatColor.GRAY + "All players are sleeping, cycling to daylight.");

            //Send action bar to players who are sleeping.
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isSleeping()) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                int sleepingPlayers = (int) Bukkit.getOnlinePlayers()
                                                                    .stream()
                                                                    .filter(p -> ((Player) p).isSleeping())
                                                                    .count();
                                int onlinePlayers = Bukkit.getOnlinePlayers().size();

                                UtilUI.sendActionBar(player, ChatColor.GRAY.toString() +
                                                                     sleepingPlayers + "/" + onlinePlayers + " players in bed.");
                            }
                        }.runTask(SMP.get());
                    } else
                        this.cancel();
                }
            }.runTaskTimerAsynchronously(SMP.get(), 20L, 35L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = PlayerManager.getInstance().getGamePlayer(player);

        String color = gamePlayer.getNameColor() == null ? "&7" : gamePlayer.getNameColor().toString();
        event.setFormat(UtilUI.colorize(color + player.getName() + "&8: &7" + event.getMessage()));

        if (Arrays.stream(blacklistedWords).anyMatch(word -> event.getMessage().toLowerCase().contains(word))) {
            event.setCancelled(true);
            new BukkitRunnable() {
                @Override
                public void run() {
                    recentlyBadPlayers.add(player.getUniqueId());
                    UtilUI.sendTitle(player, ChatColor.DARK_RED + ChatColor.BOLD.toString() + "Racism Gay", "", 5, 70, 20);

                    for (int i = 0; i < 6; i++) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                player.getWorld().strikeLightning(player.getLocation());
                            }
                        }.runTaskLater(SMP.get(), i * 3);
                    }

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            recentlyBadPlayers.remove(player);
                        }
                    }.runTaskLater(SMP.get(), 20 * 20L);
                }
            }.runTask(SMP.get());
        }

        if (!event.isCancelled()) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (player != onlinePlayer && event.getMessage().contains(onlinePlayer.getName())) {
                    onlinePlayer.sendMessage(event.getFormat().replace(onlinePlayer.getName(), ChatColor.YELLOW + onlinePlayer.getName() + ChatColor.GRAY));

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                        }
                    }.runTask(SMP.get());
                } else {
                    onlinePlayer.sendMessage(event.getFormat());
                }
            }

            //Better than doing setCancelled as it allows other plugins to handle the event.
            event.getRecipients().clear();
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
                        player.sendMessage(UtilUI.colorize("&8[&aSMP&8] &7Colorized item name."));
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
        //Clone to avoid modifying the from location.
        Location diff = event.getFrom().clone().subtract(event.getTo());
        if (Math.abs(diff.getBlockX()) == 1 || Math.abs(diff.getBlockY()) == 1 || Math.abs(diff.getBlockZ()) == 1) {
            if (PlayerManager.getInstance().getGamePlayer(event.getPlayer()).getActiveCountdowns().size() > 0) {
                GamePlayer gamePlayer = PlayerManager.getInstance().getGamePlayer(event.getPlayer());
                gamePlayer.interruptCooldowns("Movement detected.");
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        GamePlayer gp = PlayerManager.getInstance().getGamePlayer(player);

        if (recentlyBadPlayers.contains(player.getUniqueId()) && (event.getDeathMessage().contains("burned ") || event.getDeathMessage().contains("struck by lightning"))) {
            event.setDeathMessage(gp.getNameColor() + player.getName() + ChatColor.RESET + " died of racism");
            recentlyBadPlayers.remove(player);
        } else {
            event.setDeathMessage(event.getDeathMessage().replace(player.getName(), gp.getNameColor() + player.getName() + ChatColor.GRAY));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PlayerManager.getInstance().addPlayers(event.getPlayer());
        GamePlayer gamePlayer = PlayerManager.getInstance().getGamePlayer(event.getPlayer());

        event.setJoinMessage("");

        String color = gamePlayer.getNameColor() == null ? "&7" : gamePlayer.getNameColor().toString();
        Bukkit.broadcastMessage(UtilUI.colorize("&8[&a+&8] " + color + event.getPlayer().getName()));

        String mcVer = Bukkit.getVersion();
        mcVer = mcVer.substring(mcVer.indexOf(":") + 2, mcVer.indexOf(")"));
        String smpVer = SMP.get().getDescription().getVersion();

        event.getPlayer().setPlayerListHeader(UtilUI.colorize("&a&lSMP\n&7You're playing on smp.flogi.cc."));
        event.getPlayer().setPlayerListFooter(UtilUI.colorize("&8MC " + mcVer + " | SMP " + smpVer));
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
