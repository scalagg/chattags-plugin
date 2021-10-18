package gg.scala.peach.adventure

import gg.scala.cookie.Cookie
import net.kyori.adventure.platform.bukkit.BukkitAudiences

/**
 * @author GrowlyX
 * @since 10/17/2021
 */
object KyoriBridge
{
    private lateinit var adventure: BukkitAudiences

    fun adventure(): BukkitAudiences = adventure

    fun initialLoad()
    {
        adventure = BukkitAudiences.create(
            Cookie.instance
        )
    }

    fun finalSave()
    {
        adventure.close()
    }
}
