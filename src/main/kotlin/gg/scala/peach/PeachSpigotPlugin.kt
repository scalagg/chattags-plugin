package gg.scala.peach

import gg.scala.banana.Banana
import gg.scala.banana.BananaBuilder
import gg.scala.banana.options.BananaOptions
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.flavor.Flavor
import gg.scala.flavor.FlavorOptions
import gg.scala.lemon.Lemon
import gg.scala.peach.handler.ChatTagHandler
import gg.scala.peach.handler.PeachRedisHandler
import gg.scala.peach.model.ChatTag
import me.lucko.helper.plugin.ap.Plugin
import me.lucko.helper.plugin.ap.PluginDependency
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.command.manager.CubedCommandManager
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import org.bukkit.ChatColor

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
@Plugin(
    name = "Peach",
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
        lateinit var instance: PeachSpigotPlugin
    }

    private val flavor by lazy {
        Flavor.create<PeachSpigotPlugin>(
            options = FlavorOptions(logger)
        )
    }

    lateinit var banana: Banana

    override fun enable()
    {
        instance = this

        loadCommands()
        loadRedisHandler()

        flavor.startup()
    }

    private fun loadRedisHandler()
    {
        banana = BananaBuilder()
            .options(
                BananaOptions(
                    channel = "peach",
                    async = true,
                    gson = Serializers.gson
                )
            )
            .credentials(
                Lemon.instance.credentials
            )
            .build()

        banana.registerClass(PeachRedisHandler)
        banana.subscribe()
    }

    private fun loadCommands()
    {
        val commandManager = CubedCommandManager(
            plugin = this,
            primary = ChatColor.valueOf(Lemon.instance.lemonWebData.primary),
            secondary = ChatColor.valueOf(Lemon.instance.lemonWebData.secondary)
        )

        commandManager.commandCompletions.registerAsyncCompletion("chattags") {
            return@registerAsyncCompletion ChatTagHandler.chatTags.values.map { it.displayName }
        }

        commandManager.commandContexts.registerContext(ChatTag::class.java) {
            val firstArgument = it.popFirstArg()

            return@registerContext ChatTagHandler.findChatTag(firstArgument)
                ?: throw ConditionFailedException("No chat-tag matching ${CC.YELLOW}$firstArgument${CC.RED} could be found.")
        }

        Lemon.instance.registerCommandsInPackage(commandManager, "gg.scala.peach.command")
    }

    override fun disable()
    {
        flavor.close()
    }
}
