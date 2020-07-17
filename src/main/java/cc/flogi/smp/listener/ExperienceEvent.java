package cc.flogi.smp.listener;

import cc.flogi.smp.SMP;
import cc.flogi.smp.i18n.I18n;
import cc.flogi.smp.util.UtilEXP;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
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
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author Caden Kriese
 *
 * Manages events in relation to the custom experience storage system.
 *
 * Created on 02/11/2020.
 */
public class ExperienceEvent implements Listener {
    final int MAX_XP_PER_BOTTLE = 50;

    //XP Bottling handler

    @EventHandler
    public void onInventoryInteract(InventoryClickEvent event) {
        if (event.getClickedInventory() == null
                || !(event.getWhoClicked() instanceof Player)
                || !(event.getClickedInventory() instanceof PlayerInventory))
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
            HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(bottle);
            if (overflow.size() > 0) {
                for (ItemStack stack : overflow.values())
                    player.getWorld().dropItem(player.getLocation(), stack);
            }
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 0.33f);
        } else if (item == null
                && event.getCursor() == null
                && event.getClickedInventory().contains(Material.GLASS_BOTTLE)) {
            Bukkit.broadcastMessage("Updating bottles from inv click.");
            updateBottles(player, event.getClickedInventory());
        }
    }

    //Applying informative lore to bottles

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

    @EventHandler
    public void onPlayerOpenInventory(InventoryOpenEvent event) {
        // TODO maybe remove lores if the bottles are in an inventory like a brewing stand.
        // TODO also handle drinking a water bottle and it turning into a glass bottle.
        if (event.getPlayer() instanceof Player)
            updateBottles((Player) event.getPlayer(), event.getInventory());
    }

    private void updateBottles(Player player, Inventory inventory) {
        int amount = Math.min(UtilEXP.getTotalExperience(player), MAX_XP_PER_BOTTLE);
        ItemStack[] items = inventory.getContents();

        List<String> lore = null;

        if (amount > 0)
            lore = ImmutableList.of(I18n.getMessage(player, "add_xp_prompt", "amount", amount + "xp"));

        final List<String> finalLore = lore;
        Arrays.stream(items)
                .filter(item -> item != null && item.getType() == Material.GLASS_BOTTLE)
                .forEach(item -> {
                    ItemMeta meta = item.getItemMeta();
                    meta.setLore(finalLore);
                    item.setItemMeta(meta);
                });

        player.updateInventory();
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
