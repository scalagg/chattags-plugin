package gg.scala.peach.adventure

import gg.scala.commons.annotations.inject.AutoBind
import gg.scala.flavor.service.Close
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.peach.PeachSpigotPlugin
import net.kyori.adventure.platform.bukkit.BukkitAudiences

/**
 * @author GrowlyX
 * @since 10/17/2021
 */
@Service
@AutoBind
object KyoriBridge
{
    private lateinit var adventure: BukkitAudiences

    fun adventure() =
        adventure

    @Configure
    fun configure()
    {
        adventure = BukkitAudiences.create(
            PeachSpigotPlugin.instance
        )
    }

    @Close
    fun close()
    {
        adventure.close()
    }
}
