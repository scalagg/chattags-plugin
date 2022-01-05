package gg.scala.peach.adventure

import gg.scala.cookie.Cookie
import gg.scala.flavor.service.Close
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import net.kyori.adventure.platform.bukkit.BukkitAudiences

/**
 * @author GrowlyX
 * @since 10/17/2021
 */
@Service
object KyoriBridge
{
    private lateinit var adventure: BukkitAudiences

    fun adventure(): BukkitAudiences = adventure

    @Configure
    fun configure()
    {
        adventure = BukkitAudiences.create(
            Cookie.instance
        )
    }

    @Close
    fun close()
    {
        adventure.close()
    }
}
