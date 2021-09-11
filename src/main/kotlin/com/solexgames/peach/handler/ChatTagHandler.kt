package com.solexgames.peach.handler

import com.solexgames.peach.PeachSpigotPlugin
import com.solexgames.peach.model.ChatTag
import java.util.*

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
object ChatTagHandler {

    val chatTags = mutableMapOf<UUID, ChatTag>()

    init {
        PeachSpigotPlugin.instance.mongoHandler.chatTagLayer.fetchAllEntries().thenAccept { tags ->
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
