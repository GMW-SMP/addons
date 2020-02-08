package cc.flogi.smp.listener;

import cc.flogi.smp.SMP;
import cc.flogi.smp.i18n.I18n;
import cc.flogi.smp.player.GamePlayer;
import cc.flogi.smp.player.PlayerManager;
import cc.flogi.smp.util.UtilThreading;
import cc.flogi.smp.util.UtilUI;
import com.google.gson.Gson;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.Statistic;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-05-07
 */
@SuppressWarnings("ALL")
public class PlayerEvent implements Listener {

    private static final Set<String> recentlyBadPlayers = new HashSet<>();
    private static final Pattern[] blacklistedPatterns;

    static {
        Pattern[] blacklist;
        Gson gson = new Gson();
        try {
            String patterns = new String(Files.readAllBytes(
                    Paths.get(PlayerEvent.class.getResource("/i18n/chat_blacklist.json").getPath())));

            blacklist = Arrays.stream(gson.fromJson(patterns, String[].class))
                    .map(str -> Pattern.compile(str))
                    .toArray(Pattern[]::new);
        } catch (IOException ex) {
            ex.printStackTrace();
            blacklist = new Pattern[0];
        }
        blacklistedPatterns = blacklist;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        GamePlayer gp = PlayerManager.getInstance().getGamePlayer(player);

        if (!event.isCancelled() && event.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.OK) {
            if (player.getBedSpawnLocation() == null || player.getBedSpawnLocation().distance(event.getBed().getLocation()) > 2) {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                I18n.sendMessage(player, "spawn_location_set", true);
                I18n.sendActionBar(player, "spawn_location_set", false);
            }

            ArrayList<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
            I18n.broadcastMessage(onlinePlayers, "player_enter_bed", false, true,
                    "player", gp.getNameColor() + player.getName() + ChatColor.GRAY);
            if (onlinePlayers.stream().filter(Player::isSleeping).count() + 1 == Bukkit.getOnlinePlayers().size())
                I18n.broadcastMessage(onlinePlayers, "daylight_cycle", true, true);

            //Send action bar to players who are sleeping.
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isSleeping()) {
                        UtilThreading.sync(() -> {
                            Long sleepingPlayers = onlinePlayers.stream().filter(Player::isSleeping).count();
                            Integer playersCount = onlinePlayers.size();
                            I18n.sendActionBar(player, "players_sleeping", false,
                                    "current", sleepingPlayers.toString(),
                                    "max", playersCount.toString());
                        });
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
        String strippedMessage = ChatColor.stripColor(UtilUI.colorize(event.getMessage())).toLowerCase();

        if (Arrays.stream(blacklistedPatterns).anyMatch(pat -> pat.matcher(strippedMessage).find())) {
            event.setCancelled(true);
            UtilThreading.sync(() -> {
                recentlyBadPlayers.add(player.getUniqueId().toString());
                UtilUI.sendTitle(player, I18n.getMessage(player, "player_swear"), "", 5, 70, 20);
                I18n.logMessage("player_swear_log", Level.INFO,
                        "player", player.getName() + "(" + player.getUniqueId().toString() + ")");
                for (int i = 0; i < 6; i++) {
                    UtilThreading.syncDelayed(() -> player.getWorld().strikeLightning(player.getLocation()), i * 3);
                }

                UtilThreading.syncDelayed(() -> recentlyBadPlayers.remove(player.getUniqueId().toString()), 400);
            });
        }

        if (!event.isCancelled()) {
            String deaths = String.valueOf(player.getStatistic(Statistic.DEATHS));
            String mobKills = String.valueOf(player.getStatistic(Statistic.MOB_KILLS));
            String distanceTraveled = String.format("%.2f", (double) Arrays.stream(Statistic.values())
                    .filter(stat -> stat.name().contains("ONE_CM"))
                    .map(stat -> player.getStatistic(stat))
                    .mapToInt(Integer::intValue)
                    .sum() / 100000d) + "km";

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (player != onlinePlayer && event.getMessage().contains(onlinePlayer.getName())) {
                    I18n.sendMessage(onlinePlayer, "chat_format", false,
                            "name", player.getName(),
                            "color", gamePlayer.getNameColor().toString(),
                            "message", event.getMessage().replace(onlinePlayer.getName(), ChatColor.YELLOW + onlinePlayer.getName() + ChatColor.GRAY),
                            "deaths", deaths,
                            "mobs_killed", mobKills,
                            "distance_traveled", distanceTraveled);

                    UtilThreading.sync(() -> onlinePlayer.playSound(onlinePlayer.getLocation(),
                            Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1));
                } else {
                    I18n.sendMessage(onlinePlayer, "chat_format", false,
                            "name", player.getName(),
                            "color", gamePlayer.getNameColor().toString(),
                            "message", event.getMessage(),
                            "deaths", deaths,
                            "mobs_killed", mobKills,
                            "distance_traveled", distanceTraveled);
                }
            }

            //Format for console.
            event.setFormat(UtilUI.colorize(gamePlayer.getNameColor() + player.getName() + "&8: &7" + event.getMessage()));
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
                        I18n.sendMessage(player, "item_colorized", true, true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            GamePlayer gamePlayer = PlayerManager.getInstance().getGamePlayer((Player) event.getEntity());
            gamePlayer.interruptCooldowns(I18n.getMessage(gamePlayer.getPlayer(), "damage_taken"));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        //Clone to avoid modifying the from location.
        Location diff = event.getFrom().clone().subtract(event.getTo());
        if (Math.abs(diff.getBlockX()) == 1 || Math.abs(diff.getBlockZ()) == 1 || Math.abs(diff.getBlockY()) == 1) {
            if (PlayerManager.getInstance().getGamePlayer(event.getPlayer()).getActiveCountdowns().size() > 0) {
                GamePlayer gamePlayer = PlayerManager.getInstance().getGamePlayer(event.getPlayer());
                gamePlayer.interruptCooldowns(I18n.getMessage(gamePlayer.getPlayer(), "movement_detected"));
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        GamePlayer gp = PlayerManager.getInstance().getGamePlayer(player);

        if (recentlyBadPlayers.contains(player.getUniqueId().toString()) && (event.getDeathMessage().contains("burned ") || event.getDeathMessage().contains("struck by lightning"))) {
            event.setDeathMessage(I18n.getMessage(player, "swearing_death",
                    "player", gp.getNameColor() + player.getName() + ChatColor.GRAY));
            recentlyBadPlayers.remove(player.getUniqueId().toString());
        } else {
            event.setDeathMessage(event.getDeathMessage().replace(player.getName(), gp.getNameColor() + player.getName() + ChatColor.GRAY));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage("");

        PlayerManager.getInstance().addPlayers(event.getPlayer());

        GamePlayer gamePlayer = PlayerManager.getInstance().getGamePlayer(event.getPlayer());
        ChatColor color = gamePlayer.getNameColor();
        Bukkit.broadcastMessage(UtilUI.colorize("&8[&a+&8] " + color + event.getPlayer().getName()));

        String mcVer = Bukkit.getVersion();
        mcVer = mcVer.substring(mcVer.indexOf(":") + 2, mcVer.indexOf(")"));
        String smpVer = SMP.get().getDescription().getVersion();

        event.getPlayer().setPlayerListHeader(UtilUI.colorize("&a&lSMP\n&7You're playing on smp.flogi.cc."));
        event.getPlayer().setPlayerListFooter(UtilUI.colorize("&8MC " + mcVer + " | SMP " + smpVer));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage("");

        PlayerManager.getInstance().playerLogout(event.getPlayer());

        GamePlayer gamePlayer = PlayerManager.getInstance().getGamePlayer(event.getPlayer());
        ChatColor color = gamePlayer.getNameColor();
        Bukkit.broadcastMessage(UtilUI.colorize("&8[&c-&8] &3" + color + event.getPlayer().getName()));
    }
}
