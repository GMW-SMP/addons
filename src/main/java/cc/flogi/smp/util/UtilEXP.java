package cc.flogi.smp.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerExpChangeEvent;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 06/26/2020
 */
public class UtilEXP {
    /**
     * Adds experience to a player.
     *
     * @param player The player whos xp should be added to.
     * @param exp    The amount to be added to their xp (positive or negative)
     */
    public static void addExperience(final Player player, final int exp) {
        setTotalExperience(player, getTotalExperience(player) + exp);
    }

    /**
     * Sets the total experience of a player.
     * Accounts for crappy bukkit xp logic.
     *
     * @param player The player to set the xp of.
     * @param exp    The amount of xp to set.
     */
    //FIXME add param for mending when using paper.
    public static void setTotalExperience(final Player player, final int exp) {
        if (exp < 0) {
            throw new IllegalArgumentException("Experience is negative!");
        }
        player.setExp(0);
        player.setLevel(0);
        player.setTotalExperience(0);

        //This following code is technically redundant now, as bukkit now calulcates levels more or less correctly
        //At larger numbers however... player.getExp(3000), only seems to give 2999, putting the below calculations off.
        int amount = exp;
        while (amount > 0) {
            final int expToLevel = getExpAtLevel(player);
            amount -= expToLevel;
            if (amount >= 0) {
                // give until next level
                player.giveExp(expToLevel);
            } else {
                // give the rest
                amount += expToLevel;
                player.giveExp(amount);
                amount = 0;
            }
        }
        Bukkit.getPluginManager().callEvent(new PlayerExpChangeEvent(player, exp));
    }

    /**
     * Performs the calculation for the xp required to reach a level.
     *
     * @param player The player whos level to use.
     * @return The xp for the players level.
     */
    private static int getExpAtLevel(final Player player) {
        return getExpAtLevel(player.getLevel());
    }

    /**
     * Performs the calculation for the xp required to reach a level.
     *
     * @param level The level to calculate the xp of.
     * @return The xp for a certain level.
     */
    public static int getExpAtLevel(final int level) {
        if (level <= 15)
            return (2 * level) + 7;
        else if (level <= 30)
            return (5 * level) - 38;
        else
            return (9 * level) - 158;
    }

    /**
     * Gets the total experience amount for a player.
     * Accounts for crappy bukkit xp logic.
     *
     * @param player The player to get the experience of.
     * @return The total experience of the player.
     */
    public static int getTotalExperience(final Player player) {
        int exp = Math.round(getExpAtLevel(player) * player.getExp());
        int currentLevel = player.getLevel();

        while (currentLevel > 0) {
            currentLevel--;
            exp += getExpAtLevel(currentLevel);
        }
        if (exp < 0) {
            exp = Integer.MAX_VALUE;
        }
        return exp;
    }
}
