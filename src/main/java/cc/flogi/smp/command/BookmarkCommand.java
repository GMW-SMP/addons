package cc.flogi.smp.command;

import cc.flogi.smp.player.GamePlayer;
import cc.flogi.smp.player.PlayerManager;
import cc.flogi.smp.player.data.Bookmark;
import cc.flogi.smp.util.BookGUI;
import cc.flogi.smp.util.UtilLocation;
import cc.flogi.smp.util.UtilUI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-08-07
 */
public class BookmarkCommand implements CommandExecutor {
    @Override public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(UtilUI.colorize("&8[&cSMP&8] &7You must be a player to do this command."));
            return true;
        }

        Player player = (Player) sender;
        GamePlayer gp = PlayerManager.getInstance().getGamePlayer(player);

        if (label.equalsIgnoreCase("bookmarks") || label.equalsIgnoreCase("marks") || args.length == 0)
            args = new String[]{"list"};

        ArrayList<BaseComponent[]> pages = new ArrayList<>();
        ComponentBuilder builder = new ComponentBuilder(new TextComponent());

        if (args[0].equalsIgnoreCase("list")) {
            int bookmarkIndex = 0;
            for (int page = 0; page <= gp.getBookmarks().size() / 13; page++) {
                for (int line = 0; line < 14; line++) {
                    if (line == 13) {
                        builder.append("EDIT")
                                .color(ChatColor.DARK_GREEN)
                                .bold(true)
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to edit your bookmarks.")
                                                                                           .color(ChatColor.GREEN).create()))
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bookmark edit"));

                        continue;
                    }

                    if (bookmarkIndex < gp.getBookmarks().size()) {
                        Bookmark mark = gp.getBookmarks().get(bookmarkIndex);
                        String cordsString = (int) mark.getX() + "," + (int) mark.getY() + "," + (int) mark.getZ();
                        Location loc = mark.getLocation();

                        if (!mark.getWorld().equalsIgnoreCase(player.getWorld().getName())) {
                            bookmarkIndex++;
                            line--;
                            continue;
                        }

                        String blockFace = UtilLocation.getRelativeDirection(player.getLocation(), loc).name().toLowerCase().replace('_', ' ');
                        BaseComponent[] directions = new ComponentBuilder(UtilUI.colorize("&2~ " + (int) player.getLocation().distance(loc) + " blocks " + blockFace + ".")).create();

                        builder.color(ChatColor.GOLD)
                                .append(mark.getName())
                                .append(UtilUI.colorize("&7&l|"))
                                .append(cordsString)
                                .color(ChatColor.GREEN)
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, directions));

                        //If the characters wrap to next line.
                        if (cordsString.length() + mark.getName().length() > 22)
                            line++;

                        bookmarkIndex++;
                    }

                    builder.append("\n");
                }

                pages.add(builder.create());
                builder = new ComponentBuilder(new TextComponent());
            }

            new BookGUI(pages).open(player);
            return true;
        } else if (args[0].equalsIgnoreCase("edit")) {
            int bookmarkIndex = 0;
            for (int page = 0; page <= gp.getBookmarks().size() / 13; page++) {
                for (int line = 0; line < 14; line++) {
                    if (line == 13) {
                        builder.append("BACK")
                                .color(ChatColor.DARK_RED)
                                .bold(true)
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Return to bookmarks list.")
                                                                                           .color(ChatColor.RED).create()))
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bookmarks"));

                        continue;
                    }

                    if (bookmarkIndex < gp.getBookmarks().size()) {
                        Bookmark mark = gp.getBookmarks().get(bookmarkIndex);
                        String cordsString = (int) mark.getX() + "," + (int) mark.getY() + "," + (int) mark.getZ();

                        if (!mark.getWorld().equalsIgnoreCase(player.getWorld().getName())) {
                            bookmarkIndex++;
                            line--;
                            continue;
                        }

                        builder.color(ChatColor.GOLD)
                                .append(mark.getName())
                                .append(UtilUI.colorize("&7&l|"))
                                .append(cordsString)
                                .color(ChatColor.GREEN)
                                .append(" [-]")
                                .color(ChatColor.RED)
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to remove '" + mark.getName() + "'.")
                                                                                           .color(ChatColor.RED).create()))
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bookmark remove " + mark.getName()));

                        //If the characters wrap to next line.
                        if (cordsString.length() + mark.getName().length() + 2 > 19)
                            line++;

                        bookmarkIndex++;
                    }

                    builder.append("\n");
                }

                pages.add(builder.create());
                builder = new ComponentBuilder(new TextComponent());
            }

            new BookGUI(pages).open(player);
            return true;
        } else if (args[0].equalsIgnoreCase("remove")) {
            if (args.length > 1) {
                String name = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), ' ');
                if (gp.removeBookmark(name)) {
                    PlayerManager.getInstance().saveToFile(gp);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                    player.sendMessage(UtilUI.colorize("&8[&aSMP&8] &7Successfully removed bookmark &f'" + name + "'&7."));
                } else {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    player.sendMessage(UtilUI.colorize("&8[&cSMP&8] &7Could not find bookmark with name &f'" + name + "'&7."));
                }

                player.performCommand("bookmark edit");
            } else
                player.sendMessage(UtilUI.colorize("&8[&cSMP&8] &7Please enter a bookmark to remove."));
            return true;
        }

        String name = StringUtils.join(args, ' ');
        gp.addBookmark((player).getLocation(), name);
        PlayerManager.getInstance().saveToFile(gp);

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
        player.sendMessage(UtilUI.colorize("&8[&aSMP&8] &7Added location to your bookmarks."));

        return true;
    }
}
