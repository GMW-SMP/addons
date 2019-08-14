package cc.flogi.smp.util;

import cc.flogi.smp.SMP;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 2019-08-11
 */
public class BookGUI {
    private ItemStack book;

    public BookGUI(List<BaseComponent[]> pages) {
        book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.spigot().setPages(pages);

        book.setItemMeta(meta);
    }

    public BookGUI(List<String> pages, boolean colorize) {
        book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        if (colorize)
            meta.setPages(pages.stream().map(UtilUI::colorize).collect(Collectors.toList()));
        else
            meta.setPages(pages);

        book.setItemMeta(meta);
    }

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
