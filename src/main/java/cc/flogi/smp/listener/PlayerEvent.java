package cc.flogi.smp.listener;

import cc.flogi.smp.SMP;
import cc.flogi.smp.i18n.I18n;
import cc.flogi.smp.player.GamePlayer;
import cc.flogi.smp.player.PlayerManager;
import cc.flogi.smp.util.UtilUI;
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

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Caden Kriese (flogic)
 * <p>
 * Created on 2019-05-07
 */
@SuppressWarnings("ALL")
public class PlayerEvent implements Listener {

    private final Set<UUID> recentlyBadPlayers = new HashSet<>();
    private final Pattern[] blacklistedPatterns = new Pattern[]{
            Pattern.compile("\\b(n+(\\W|\\d|_)*(i|1)+(\\W|\\d|_)*g+(\\W|\\d|_)*g+(\\W|\\d|_)*(a|4)+(\\W|\\d|_)*)"),
            Pattern.compile("\\b(n+(\\W|\\d|_)*(i|1)+(\\W|\\d|_)*g+(\\W|\\d|_)*g+(\\W|\\d|_)*(e|3|a|4)+(\\W|\\d|_)*r+(\\W|\\d|_)*)"),
            Pattern.compile("\\b(n+(\\W|\\d|_)*(i|1)+(\\W|\\d|_)*g+(\\W|\\d|_)*(e|3|a|4)+(\\W|\\d|_)*r+(\\W|\\d|_)*)"),
            Pattern.compile("\\b(n+(\\W|\\d|_)*(i|1)+(\\W|\\d|_)*g+(\\W|\\d|_)*(i|4)+(\\W|\\d|_)*)"),
            Pattern.compile("\\b(n+(\\W|\\d|_)*(i|1)+(\\W|\\d|_)*g+(\\W|\\d|_)*l+(\\W|\\d|_)*(e|3|a|4)+(\\W|\\d|_)*t+(\\W|\\d|_)*)"),
            Pattern.compile("\\b(n+(\\W|\\d|_)*(i|1)+(\\W|\\d|_)*g+(\\W|\\d|_)*)"),
            Pattern.compile("\\b(b+(\\W|\\d|_)*(e|3)+(\\W|\\d|_)*(a|4)+(\\W|\\d|_)*n+(\\W|\\d|_)*(e|3)+(\\W|\\d|_)*r+(\\W|\\d|_)*)"),
            Pattern.compile("\\b(k+(\\W|\\d|_)*(i|1)+(\\W|\\d|_)*k+(\\W|\\d|_)*(e|3)+(\\W|\\d|_)*)"),
            Pattern.compile("\\b(c+(\\W|\\d|_)*h+(\\W|\\d|_)*(i|1)+(\\W|\\d|_)*n+(\\W|\\d|_)*k+(\\W|\\d|_)*)")
    };

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
        String strippedMessage = ChatColor.stripColor(UtilUI.colorize(event.getMessage())).toLowerCase();

        if (Arrays.stream(blacklistedPatterns).anyMatch(pat -> pat.matcher(strippedMessage).find())) {
            event.setCancelled(true);
            new BukkitRunnable() {
                @Override
                public void run() {
                    recentlyBadPlayers.add(player.getUniqueId());
                    UtilUI.sendTitle(player, ChatColor.DARK_RED + ChatColor.BOLD.toString() + "Racism Gay", "", 5, 70, 20);
                    SMP.get().getLogger().info(player.getName() + " tried to say a blacklisted phrase.");

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
            String deaths = String.valueOf(player.getStatistic(Statistic.DEATHS));
            String mobKills = String.valueOf(player.getStatistic(Statistic.MOB_KILLS));
            String distanceTraveled = String.format("%.2f", (double) Arrays.stream(Statistic.values())
                                              .filter(stat -> stat.name().contains("ONE_CM"))
                                              .map(stat -> player.getStatistic(stat))
                                              .mapToInt(Integer::intValue)
                                              .sum()/100000d)+"km";

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (player != onlinePlayer && event.getMessage().contains(onlinePlayer.getName())) {
                    I18n.sendMessage(onlinePlayer, "chat_format", false,
                            "name", player.getName(),
                            "color", gamePlayer.getNameColor().toString(),
                            "message", event.getMessage().replace(onlinePlayer.getName(), ChatColor.YELLOW + onlinePlayer.getName() + ChatColor.GRAY),
                            "deaths", deaths,
                            "mobs_killed", mobKills,
                            "distance_traveled", distanceTraveled);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                        }
                    }.runTask(SMP.get());
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
        if (Math.abs(diff.getBlockX()) == 1 || Math.abs(diff.getBlockZ()) == 1 || Math.abs(diff.getBlockY()) == 1) {
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
