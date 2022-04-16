package gg.scala.peach

import gg.scala.aware.AwareBuilder
import gg.scala.aware.codec.codecs.interpretation.AwareMessageCodec
import gg.scala.aware.message.AwareMessage
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.annotations.container.ContainerDisable
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.peach.handler.PeachRedisHandler
import me.lucko.helper.plugin.ap.Plugin
import me.lucko.helper.plugin.ap.PluginDependency
import kotlin.properties.Delegates

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
@Plugin(
    name = "Peach",
    apiVersion = "1.18",
    depends = [
        PluginDependency("Cubed"),
        PluginDependency("helper"),
        PluginDependency("Lemon"),
        PluginDependency("store-spigot")
    ]
)
class PeachSpigotPlugin : ExtendedScalaPlugin()
{
    companion object
    {
        @JvmStatic
        var instance by Delegates.notNull<PeachSpigotPlugin>()
    }

    val aware by lazy {
        AwareBuilder
            .of<AwareMessage>("peach")
            .codec(AwareMessageCodec)
            .logger(this.logger)
            .build()
    }

    @ContainerEnable
    fun containerEnable()
    {
        instance = this

        this.aware.listen(PeachRedisHandler)
        this.aware.connect().toCompletableFuture().join()
    }

    @ContainerDisable
    fun containerDisable()
    {
        this.aware.shutdown()
    }
}
