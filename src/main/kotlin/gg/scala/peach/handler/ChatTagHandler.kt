package gg.scala.peach.handler

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.handler.ChatHandler
import gg.scala.peach.channel.ChatTagDefaultChannel
import gg.scala.peach.model.ChatTag
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.serializers.Serializers
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

        ChatHandler.channels["default"] = ChatTagDefaultChannel
    }

    fun findChatTag(name: String): ChatTag?
    {
        return chatTags.values.firstOrNull {
            it.displayName.equals(name, true)
        }
    }
}
