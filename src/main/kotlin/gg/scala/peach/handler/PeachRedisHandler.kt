package gg.scala.peach.handler

import gg.scala.aware.annotation.Subscribe
import gg.scala.aware.message.AwareMessage
import gg.scala.peach.model.ChatTag
import gg.scala.peach.model.ChatTagPlayer
import gg.scala.peach.service.PeachPlayerService
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import org.bukkit.Bukkit
import java.util.*

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
object PeachRedisHandler
{
    @Subscribe("ctp-refresh")
    fun onCtpRefresh(message: AwareMessage)
    {
        val uniqueId = message
            .retrieve<UUID>("uniqueId")

        Bukkit.getPlayer(uniqueId)?.let {
            DataStoreObjectControllerCache
                .findNotNull<ChatTagPlayer>()
                .loadOptimalCopy(
                    uniqueId
                ) {
                    ChatTagPlayer(uniqueId)
                }
        }
    }

    @Subscribe("chat-tag-delete")
    fun onChatTagDelete(message: AwareMessage)
    {
        val rankUuid = message
            .retrieve<UUID>("uniqueId")

        ChatTagHandler.chatTags
            .remove(rankUuid)
    }

    @Subscribe("chat-tag-update")
    fun onChatTagUpdate(message: AwareMessage)
    {
        val controller = DataStoreObjectControllerCache
            .findNotNull<ChatTag>()

        controller.load(
            message.retrieve("uniqueId"),
            DataStoreStorageType.MONGO
        ).thenAccept {
            it ?: return@thenAccept
            ChatTagHandler.chatTags[it.uniqueId] = it
        }
    }
}
