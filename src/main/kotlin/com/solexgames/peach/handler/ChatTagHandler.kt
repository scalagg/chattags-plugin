package com.solexgames.peach.handler

import com.solexgames.peach.PeachSpigotPlugin
import com.solexgames.peach.model.ChatTag
import net.evilblock.cubed.util.CC
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

            if (chatTags.isEmpty()) {
                val uuid = UUID.randomUUID()
                chatTags[uuid] = ChatTag(uuid, "Testing", "${CC.AQUA}Testing")
            }
        }
    }
}
