package com.solexgames.peach

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.metadata.Metadata
import com.solexgames.lemon.player.rank.Rank
import com.solexgames.peach.command.ChatTagsAdminCommand
import com.solexgames.peach.command.ChatTagsCommand
import com.solexgames.peach.handler.ChatTagHandler
import com.solexgames.peach.handler.MongoHandler
import com.solexgames.peach.handler.PeachRedisHandler
import com.solexgames.peach.model.ChatTag
import com.solexgames.peach.model.ChatTagPlayer
import com.solexgames.redis.JedisBuilder
import me.lucko.helper.Events
import me.lucko.helper.plugin.ExtendedJavaPlugin
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.MessageType
import net.evilblock.cubed.command.manager.CubedCommandManager
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import org.bukkit.ChatColor
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
class PeachSpigotPlugin : ExtendedJavaPlugin() {

    companion object {
        @JvmStatic
        lateinit var instance: PeachSpigotPlugin
    }

    lateinit var mongoHandler: MongoHandler
    lateinit var chatTagHandler: ChatTagHandler

    val players = mutableMapOf<UUID, ChatTagPlayer>()

    override fun enable() {
        instance = this

        mongoHandler = MongoHandler
        chatTagHandler = ChatTagHandler

        loadCommands()
        loadRedisHandler()
        loadListeners()
    }

    private fun loadListeners() {
        Events.subscribe(PlayerJoinEvent::class.java).handler {
            val lemonPlayer = Lemon.instance.playerHandler
                .findPlayer(it.player.uniqueId).orElse(null)

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
            val lemonPlayer = Lemon.instance.playerHandler
                .findPlayer(it.player.uniqueId).orElse(null)

            if (lemonPlayer != null) {
                val serialized = Serializers.gson.toJson(players[it.player.uniqueId])

                lemonPlayer.updateOrAddMetadata(
                    "chat-tag-player",
                    Metadata(serialized)
                )
            }
        }
    }

    private fun loadRedisHandler() {
        JedisBuilder()
            .withChannel("peach")
            .withHandler(PeachRedisHandler)
            .withSettings(Lemon.instance.jedisSettings)
            .build()
    }

    private fun loadCommands() {
        val commandManager = CubedCommandManager(this)

        listOf<MessageType>(MessageType.HELP, MessageType.INFO, MessageType.SYNTAX).forEach {
            commandManager.getFormat(it).setColor(3, ChatColor.GRAY)
            commandManager.getFormat(it).setColor(2, ChatColor.valueOf(Lemon.instance.lemonWebData.secondary))
            commandManager.getFormat(it).setColor(1, ChatColor.valueOf(Lemon.instance.lemonWebData.primary))
        }

        commandManager.commandCompletions.registerAsyncCompletion("chattags") {
            return@registerAsyncCompletion mutableListOf<String>().also {
                chatTagHandler.chatTags.values.forEach { chatTag ->
                    it.add(chatTag.displayName)
                }
            }
        }

        commandManager.commandContexts.registerContext(ChatTag::class.java) {
            val firstArgument = it.popFirstArg()

            return@registerContext chatTagHandler.findChatTag(firstArgument)
                ?: throw ConditionFailedException("Could not find chat tag by the name: ${CC.YELLOW}$firstArgument${CC.RED}.")
        }

        commandManager.registerCommand(ChatTagsCommand())
        commandManager.registerCommand(ChatTagsAdminCommand())
    }
}
