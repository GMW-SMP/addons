package cc.flogi.smp.command;

import cc.flogi.smp.i18n.I18n;
import cc.flogi.smp.player.GamePlayer;
import cc.flogi.smp.player.PlayerManager;
import cc.flogi.smp.player.data.Bookmark;
import cc.flogi.smp.util.BookGUI;
import cc.flogi.smp.util.UtilLocation;
import cc.flogi.smp.util.UtilUI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
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
    private final int MAX_NAME_LENGTH = 19;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            I18n.sendError(sender, "must_be_player", true);
            return true;
        }

        Player player = (Player) sender;
        GamePlayer gp = PlayerManager.getInstance().getGamePlayer(player);

        if (label.equalsIgnoreCase("bookmarks") || label.equalsIgnoreCase("marks") || args.length == 0)
            args = new String[]{"list"};

        if (args[0].equalsIgnoreCase("list")) {
            BaseComponent[] footer = new ComponentBuilder("EDIT")
                    .color(ChatColor.DARK_GREEN)
                    .bold(true)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder("Click to edit your bookmarks.")
                            .color(ChatColor.GREEN).create())))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bookmark edit")).create();
            ArrayList<BaseComponent[]> lines = new ArrayList<>();

            Bookmark[] validBookmarks = gp.getBookmarks().stream()
                    .filter(bm -> bm.getWorld().equals(player.getWorld().getName()))
                    .toArray(Bookmark[]::new);

            for (Bookmark mark : validBookmarks) {
                String cordsString = (int) mark.getX() + "," + (int) mark.getY() + "," + (int) mark.getZ();
                Location loc = mark.getLocation();

                String blockFace = UtilLocation.getRelativeDirection(player.getLocation(), loc).name().toLowerCase().replace('_', ' ');
                BaseComponent[] directions = new ComponentBuilder(UtilUI.colorize("&2~ " + (int) player.getLocation().distance(loc) + " blocks " + blockFace + ".")).create();

                lines.add(new ComponentBuilder(mark.getName())
                        .color(ChatColor.GOLD)
                        .append(UtilUI.colorize("&7&l|"))
                        .append(cordsString)
                        .color(ChatColor.GREEN)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(directions)))
                        .create());

            }

            new BookGUI(lines, footer).open(player);
            return true;
        } else if (args[0].equalsIgnoreCase("edit")) {
            BaseComponent[] footer = new ComponentBuilder("BACK")
                    .color(ChatColor.DARK_RED)
                    .bold(true)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder("Return to bookmarks list.")
                            .color(ChatColor.RED).create())))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bookmarks")).create();
            ArrayList<BaseComponent[]> lines = new ArrayList<>();

            Bookmark[] validBookmarks = gp.getBookmarks().stream()
                    .filter(bm -> bm.getWorld().equals(player.getWorld().getName()))
                    .toArray(Bookmark[]::new);

            for (Bookmark mark : validBookmarks) {
                String cordsString = (int) mark.getX() + "," + (int) mark.getY() + "," + (int) mark.getZ();

                lines.add(new ComponentBuilder(mark.getName())
                        .color(ChatColor.GOLD)
                        .append(UtilUI.colorize("&7&l|"))
                        .append(cordsString)
                        .color(ChatColor.GREEN)
                        .append(" [-]")
                        .color(ChatColor.RED)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder("Click to remove '" + mark.getName() + "'.")
                                .color(ChatColor.RED).create())))
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bookmark remove " + mark.getName()))
                        .create());

            }

            new BookGUI(lines, footer).open(player);
            return true;
        } else if (args[0].equalsIgnoreCase("remove")) {
            if (args.length > 1) {
                String name = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), ' ');
                if (gp.removeBookmark(name)) {
                    PlayerManager.getInstance().saveToFile(gp);
                    I18n.sendMessage(player, "bookmark_removed", true, true, "name", name);
                } else
                    I18n.sendError(player, "bookmark_not_found", true, true, "name", name);

                player.performCommand("bookmark edit");
            } else
                I18n.sendError(player, "enter_bookmark", true, true);

            return true;
        }

        String name = StringUtils.join(args, ' ');
        if (gp.getBookmarks().stream().map(Bookmark::getName).noneMatch(name::equalsIgnoreCase)) {
            if (name.length() <= MAX_NAME_LENGTH) {
                gp.addBookmark((player).getLocation(), name);
                PlayerManager.getInstance().saveToFile(gp);

                I18n.sendMessage(player, "bookmark_added", true, true, "name", name);
            } else {
                I18n.sendError(player, "bookmark_name_too_long", true, true,
                        "maxlength", MAX_NAME_LENGTH + "",
                        "difference", (name.length() - MAX_NAME_LENGTH) + "");
            }
        } else
            I18n.sendError(player, "bookmark_name_taken", true, true);

        return true;
    }
}
