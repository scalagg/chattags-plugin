package gg.scala.peach.handler

import gg.scala.peach.model.ChatTag
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.serializers.Serializers
import java.util.*

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
object ChatTagHandler {

    var chatTags = mutableMapOf<UUID, ChatTag>()

    fun initialLoad() {
        val handle = DataStoreObjectControllerCache.create<ChatTag>()
        handle.provideCustomSerializer(Serializers.gson)

        handle.loadAll(DataStoreStorageType.MONGO)
            .thenAccept {
                chatTags = it.toMutableMap()
            }
    }

    fun findChatTag(name: String): ChatTag? {
        return chatTags.values.firstOrNull {
            it.displayName.equals(name, true)
        }
    }
}
