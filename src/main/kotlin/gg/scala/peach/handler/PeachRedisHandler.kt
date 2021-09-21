package gg.scala.peach.handler

import gg.scala.banana.annotate.Subscribe
import gg.scala.banana.message.Message
import gg.scala.banana.subscribe.marker.BananaHandler
import java.util.*

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
object PeachRedisHandler : BananaHandler {

    @Subscribe("chat-tag-delete")
    fun onChatTagDelete(message: Message) {
        val rankUuid = UUID.fromString(
            message["uniqueId"]
        )

        ChatTagHandler.chatTags.remove(rankUuid)
    }

    @Subscribe("chat-tag-update")
    fun onChatTagUpdate(message: Message) {
        val completableFuture =  MongoHandler.chatTagLayer
            .fetchEntryByKey(message["uniqueId"])

        completableFuture.thenAccept {
            ChatTagHandler.chatTags[it.uniqueId] = it
        }
    }
}
