package gg.scala.chattags.plugin.handler

import gg.scala.aware.annotation.Subscribe
import gg.scala.aware.message.AwareMessage
import gg.scala.chattags.plugin.model.ChatTag
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import java.util.*

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
object ChatTagsRedisService
{
    @Subscribe("chat-tag-delete")
    fun onChatTagDelete(message: AwareMessage)
    {
        val rankUuid = message
            .retrieve<UUID>("uniqueId")

        ChatTagsService.chatTags
            .remove(rankUuid)
    }

    @Subscribe("chat-tag-update")
    fun onChatTagUpdate(message: AwareMessage)
    {
        DataStoreObjectControllerCache
            .findNotNull<ChatTag>()
            .load(
                message.retrieve("uniqueId"),
                DataStoreStorageType.MONGO
            )
            .thenAccept {
                it ?: return@thenAccept
                ChatTagsService.chatTags[it.uniqueId] = it
            }
    }
}
