package gg.scala.peach.handler

import gg.scala.peach.PeachSpigotPlugin
import gg.scala.peach.model.ChatTag
import java.util.*

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
object ChatTagHandler {

    val chatTags = mutableMapOf<UUID, ChatTag>()

    fun initialLoad() {
        MongoHandler.chatTagLayer.fetchAllEntries().thenAccept { tags ->
            tags.forEach {
                chatTags[it.value.uniqueId] = it.value
            }
        }
    }

    fun findChatTag(name: String): ChatTag? {
        return chatTags.values.firstOrNull {
            it.displayName.equals(name, true)
        }
    }
}
