package gg.scala.peach.handler

import gg.scala.peach.PeachSpigotPlugin
import com.solexgames.redis.annotation.Subscription
import com.solexgames.redis.handler.JedisHandler
import com.solexgames.redis.json.JsonAppender
import java.util.*

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
object PeachRedisHandler : JedisHandler {

    @Subscription(action = "chat-tag-delete")
    fun onChatTagDelete(jsonAppender: JsonAppender) {
        val rankUuid = UUID.fromString(
            jsonAppender.getParam("uniqueId")
        )

        ChatTagHandler.chatTags.remove(rankUuid)
    }

    @Subscription(action = "chat-tag-update")
    fun onChatTagUpdate(jsonAppender: JsonAppender) {
        val completableFuture =  MongoHandler.chatTagLayer
            .fetchEntryByKey(jsonAppender.getParam("uniqueId"))

        completableFuture.thenAccept {
            ChatTagHandler.chatTags[it.uniqueId] = it
        }
    }
}
