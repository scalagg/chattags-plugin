package gg.scala.peach

import gg.scala.banana.Banana
import gg.scala.banana.BananaBuilder
import gg.scala.banana.options.BananaOptions
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.ChatHandler
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.metadata.Metadata
import gg.scala.peach.adventure.KyoriBridge
import gg.scala.peach.channel.ChatTagDefaultChannel
import gg.scala.peach.handler.ChatTagHandler
import gg.scala.peach.handler.PeachRedisHandler
import gg.scala.peach.model.ChatTag
import gg.scala.peach.model.ChatTagPlayer
import me.lucko.helper.Events
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.command.manager.CubedCommandManager
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
class PeachSpigotPlugin : ExtendedScalaPlugin() {

    companion object {
        @JvmStatic
        lateinit var instance: PeachSpigotPlugin
    }

    val players = mutableMapOf<UUID, ChatTagPlayer>()

    lateinit var banana: Banana

    override fun enable() {
        instance = this

        loadCommands()
        loadRedisHandler()
        loadListeners()

        ChatHandler.channels["default"] = ChatTagDefaultChannel

        KyoriBridge.initialLoad()
    }

    private fun loadListeners() {
        Events.subscribe(PlayerJoinEvent::class.java).handler {
            val lemonPlayer = PlayerHandler.findPlayer(it.player.uniqueId).orElse(null)

            if (lemonPlayer != null) {
                val chatTagPlayerData = lemonPlayer.getMetadata("chat-tag-player")

                if (chatTagPlayerData != null) {
                    val chatTagPlayer = Serializers.gson.fromJson(
                        chatTagPlayerData.asString(), ChatTagPlayer::class.java
                    )

                    if (chatTagPlayer != null) {
                        players[it.player.uniqueId] = chatTagPlayer
                    } else {
                        val newChatTagPlayer = ChatTagPlayer()

                        players[it.player.uniqueId] = newChatTagPlayer
                    }
                } else {
                    val chatTagPlayer = ChatTagPlayer()

                    players[it.player.uniqueId] = chatTagPlayer
                }
            }
        }

        Events.subscribe(PlayerQuitEvent::class.java).handler {
            finalizePlayer(it.player)
        }
    }

    fun finalizePlayer(player: Player)
    {
        val lemonPlayer = PlayerHandler.findPlayer(player.uniqueId).orElse(null)

        if (lemonPlayer != null) {
            val serialized = Serializers.gson.toJson(players[player.uniqueId])

            lemonPlayer.updateOrAddMetadata(
                "chat-tag-player",
                Metadata(serialized)
            )
        }
    }

    private fun loadRedisHandler() {
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

    private fun loadCommands() {
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
        KyoriBridge.finalSave()
    }
}
