package cc.flogi.smp.util;

import cc.flogi.smp.SMP;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-08-11
 */
@SuppressWarnings("unused")
public class BookGUI {
    private ItemStack book;

    /**
     * Creates a BookGUI from the given params.
     *
     * @param pages The pages to be set in the book.
     */
    public BookGUI(List<BaseComponent[]> pages) {
        book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.spigot().setPages(pages);

        book.setItemMeta(meta);
    }

    /***
     * Creates a BookGUI from the given params.
     *
     * @param pages The pages to be set in the book.
     * @param colorize Should the pages have their ChatColors replaced.
     */
    public BookGUI(List<String> pages, boolean colorize) {
        book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        if (colorize)
            meta.setPages(pages.stream().map(UtilUI::colorize).collect(Collectors.toList()));
        else
            meta.setPages(pages);

        book.setItemMeta(meta);
    }

    /**
     * Creates a BookGUI from the given params.
     *
     * @param lines      The lines to be included. (Pages split up automatically)
     * @param footerLine The line to be set at the bottom of each page.
     */
    //Needs suppression bc stupid JDK bug wont allow BaseComponent::toPlainText.
    @SuppressWarnings("Convert2MethodRef")
    public BookGUI(List<BaseComponent[]> lines, BaseComponent[] footerLine) {
        ArrayList<BaseComponent[]> pages = new ArrayList<>();
        ComponentBuilder builder = new ComponentBuilder(new TextComponent());

        int stringIndex = 0;
        int trueLineCount = lines.stream()
                                    .map(c -> UtilUI.countLines(StringUtils.join(Arrays.stream(c)
                                                                       .map(cmp -> cmp.toPlainText())
                                                                       .toArray(String[]::new))))
                                    .mapToInt(Integer::intValue)
                                    .sum();

        for (int page = 0; page <= trueLineCount / 13; page++) {
            for (int lineNum = 0; lineNum < 14; lineNum++) {
                if (lineNum == 13) {
                    builder.append(footerLine);
                    continue;
                }

                if (stringIndex < lines.size()) {
                    BaseComponent[] lineText = lines.get(stringIndex);
                    int lineCount = UtilUI.countLines(StringUtils.join(Arrays.stream(lineText)
                                                                               .map(comp -> comp.toPlainText())
                                                                               .toArray(String[]::new)));

                    if (lineCount + lineNum < 13) {
                        builder.append(lineText);
                        lineNum += lineCount - 1;
                        stringIndex++;
                    } else {
                        builder.append(StringUtils.repeat("\n", 13 - lineNum)).reset();
                        lineNum = 12;
                        continue;
                    }
                }

                builder.append("\n").reset();
            }

            pages.add(builder.create());
            builder = new ComponentBuilder(new TextComponent()).retain(ComponentBuilder.FormatRetention.NONE);
        }

        book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.spigot().setPages(pages);

        book.setItemMeta(meta);
    }

    /**
     * Opens the GUI to the player.
     *
     * @param player The player to send the book packet to.
     */
    public void open(Player player) {
        ItemStack previousItem = player.getInventory().getItemInMainHand();
        player.getInventory().setItemInMainHand(book);

        ProtocolManager protocol = SMP.get().getProtocolManager();
        PacketContainer bookPacket = protocol.createPacket(PacketType.Play.Server.OPEN_BOOK);
        bookPacket.getHands().write(0, EnumWrappers.Hand.MAIN_HAND);

        try {
            protocol.sendServerPacket(player, bookPacket);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } finally {
            player.getInventory().setItemInMainHand(previousItem);
        }
    }
}
