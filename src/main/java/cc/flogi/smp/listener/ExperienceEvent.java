package cc.flogi.smp.listener;

import cc.flogi.smp.SMP;
import cc.flogi.smp.i18n.I18n;
import cc.flogi.smp.util.UtilEXP;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Caden Kriese
 *
 * Manages events in relation to the custom experience storage system.
 *
 * Created on 02/11/2020.
 */
public class ExperienceEvent implements Listener {
    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null)
            return;

        ItemStack product = event.getRecipe().getResult();

        if (product.getType() == Material.GLASS_BOTTLE) {
            Player player = (Player) event.getInventory().getViewers().get(0);
            ItemMeta meta = product.getItemMeta();
            int amount = Math.min(player.getTotalExperience(), 50);
            meta.setLore(Collections.singletonList(I18n.getMessage(player, "add_xp_prompt",
                    "amount", amount + "xp")));
            product.setItemMeta(meta);
            event.getInventory().setResult(product);
        }
    }

    @EventHandler
    public void onInventoryInteract(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || !(event.getWhoClicked() instanceof Player))
            return;

        ItemStack item = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();

        if (item != null &&
                item.getType() == Material.GLASS_BOTTLE &&
                event.getClick() == ClickType.SHIFT_RIGHT &&
                UtilEXP.getTotalExperience(player) > 0) {
            event.setCancelled(true);
            if (item.getAmount() > 1)
                item.setAmount(item.getAmount() - 1);
            else
                player.getInventory().remove(item);

            ItemStack bottle = new ItemStack(Material.EXPERIENCE_BOTTLE);
            ItemMeta meta = bottle.getItemMeta();
            int amount = Math.min(player.getTotalExperience(), 50);
            UtilEXP.addExperience(player, -amount);
            meta.setLore(Collections.singletonList(I18n.getMessage(player, "xp_amount",
                    "amount", amount + "xp")));
            meta.getPersistentDataContainer().set(new NamespacedKey(SMP.get(), "xp_amount"),
                    PersistentDataType.INTEGER, amount);
            bottle.setItemMeta(meta);
            player.getInventory().addItem(bottle);
        }
    }

    @SuppressWarnings("ConstantConditions") @EventHandler
    public void onExpBottle(ExpBottleEvent event) {
        NamespacedKey key = new NamespacedKey(SMP.get(), "xp_amount");
        ItemMeta meta = event.getEntity().getItem().getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER))
            return;

        event.setExperience(meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER));
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            int amount = Math.min(player.getTotalExperience(), 50);
            ItemStack item = event.getItem().getItemStack();
            ItemMeta meta = item.getItemMeta();
            meta.setLore(Collections.singletonList(I18n.getMessage(player, "add_xp_prompt",
                    "amount", amount + "xp")));
            item.setItemMeta(meta);
            event.getItem().remove();
            event.setCancelled(true);
            ((Player) event.getEntity()).getInventory().addItem(item);
        }
    }

    //FIXME may create bugs in inventories with multiple viewers.
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        updateBottles((Player) event.getPlayer(), event.getInventory());
    }

    @EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        updateBottles(event.getPlayer(), event.getPlayer().getInventory());
    }

    private void updateBottles(Player player, Inventory inventory) {
        int amount = Math.min(player.getTotalExperience(), 50);
        ItemStack[] items = inventory.getContents();

        inventory.setContents(Arrays.stream(items).peek(item -> {
            if (item != null && item.getType() == Material.GLASS_BOTTLE) {
                ItemMeta meta = item.getItemMeta();
                meta.setLore(Collections.singletonList(I18n.getMessage(player, "add_xp_prompt",
                        "amount", amount + "xp")));
                item.setItemMeta(meta);
            }
        }).toArray(ItemStack[]::new));
    }
}
