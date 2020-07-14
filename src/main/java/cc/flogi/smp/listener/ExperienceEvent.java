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
    final int MAX_XP_PER_BOTTLE = 50;

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
            int amount = Math.min(player.getTotalExperience(), MAX_XP_PER_BOTTLE);
            UtilEXP.addExperience(player, -amount, false);
            meta.setLore(Collections.singletonList(I18n.getMessage(player, "xp_amount",
                    "amount", amount + "xp")));
            meta.getPersistentDataContainer().set(new NamespacedKey(SMP.get(), "xp_amount"),
                    PersistentDataType.INTEGER, amount);
            bottle.setItemMeta(meta);
            player.getInventory().addItem(bottle);
        }
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null || event.getInventory().getResult() == null)
            return;

        Player player = (Player) event.getInventory().getViewers().get(0);

        applyXpLore(event.getInventory().getResult(), player);
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
        if (event.getEntity() instanceof Player)
            applyXpLore(event.getItem().getItemStack(), (Player) event.getEntity());
    }

    @EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        updateBottles(event.getPlayer(), event.getPlayer().getInventory());
    }

    private void updateBottles(Player player, Inventory inventory) {
        int amount = Math.min(UtilEXP.getTotalExperience(player), MAX_XP_PER_BOTTLE);
        ItemStack[] items = inventory.getContents();

        inventory.setContents(Arrays.stream(items).peek(item -> {
            if (item != null && item.getType() == Material.GLASS_BOTTLE) {
                ItemMeta meta = item.getItemMeta();
                if (amount == 0)
                    meta.setLore(null);
                else
                    meta.setLore(Collections.singletonList(I18n.getMessage(player, "add_xp_prompt",
                            "amount", amount + "xp")));
                item.setItemMeta(meta);
            }
        }).toArray(ItemStack[]::new));
    }

    private void applyXpLore(ItemStack item, Player player) {
        if (item.getType() == Material.GLASS_BOTTLE) {
            int amount = Math.min(UtilEXP.getTotalExperience(player), MAX_XP_PER_BOTTLE);
            ItemMeta meta = item.getItemMeta();
            if (player.getTotalExperience() > 0)
                meta.setLore(Collections.singletonList(I18n.getMessage(player, "add_xp_prompt",
                        "amount", amount + "xp")));
            else
                meta.setLore(null);
            item.setItemMeta(meta);
        }
    }
}
