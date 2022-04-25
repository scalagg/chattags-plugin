package gg.scala.peach.handler

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.channel.channels.DefaultChatChannel
import gg.scala.peach.model.ChatTag
import gg.scala.peach.service.PeachPlayerService
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import java.util.*

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
@Service
object ChatTagHandler
{
    var chatTags = mutableMapOf<UUID, ChatTag>()

    @Configure
    fun configure()
    {
        val handle = DataStoreObjectControllerCache.create<ChatTag>()

        handle.loadAll(DataStoreStorageType.MONGO)
            .thenAccept {
                chatTags = it.toMutableMap()
            }
            .join()

        val blank = Component.text("")

        DefaultChatChannel.provideChatTag { player ->
            val chatTagPlayer = PeachPlayerService
                .find(player)
                ?: return@provideChatTag blank

            if (
                chatTagPlayer.currentChatTag == null
            )
            {
                return@provideChatTag blank
            }

            val chatTag =
                chatTags[chatTagPlayer.currentChatTag]
                    ?: return@provideChatTag blank

            return@provideChatTag Component
                .text(" ")
                .append {
                    chatTag.value
                }
        }
    }

    fun findChatTag(name: String): ChatTag?
    {
        return chatTags.values.firstOrNull {
            it.displayName.equals(name, true)
        }
    }
}
