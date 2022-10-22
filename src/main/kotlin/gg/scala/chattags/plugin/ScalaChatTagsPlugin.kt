package gg.scala.chattags.plugin

import gg.scala.aware.AwareBuilder
import gg.scala.aware.codec.codecs.interpretation.AwareMessageCodec
import gg.scala.aware.message.AwareMessage
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.annotations.container.ContainerDisable
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.chattags.plugin.handler.ChatTagsRedisService
import gg.scala.commons.core.plugin.Plugin
import gg.scala.commons.core.plugin.PluginAuthor
import gg.scala.commons.core.plugin.PluginDependency
import gg.scala.commons.core.plugin.PluginDependencyComposite
import gg.scala.commons.core.plugin.PluginWebsite

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
@Plugin(
    name = "ScChatTags",
    version = "%remote%/%branch%/id%"
)
@PluginAuthor("Scala")
@PluginWebsite("https://scala.gg")
@PluginDependencyComposite(
    PluginDependency("scala-commons"),
    PluginDependency("cloudsync", soft = true),
    PluginDependency("Lemon", soft = true)
)
class ScalaChatTagsPlugin : ExtendedScalaPlugin()
{
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
        this.aware.listen(ChatTagsRedisService)
        this.aware.connect()
    }

    @ContainerDisable
    fun containerDisable()
    {
        this.aware.shutdown()
    }
}
