package com.solexgames.peach.handler

import com.solexgames.peach.PeachSpigotPlugin
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

        PeachSpigotPlugin.instance.chatTagHandler.chatTags.remove(rankUuid)
    }

    @Subscription(action = "chat-tag-update")
    fun onChatTagUpdate(jsonAppender: JsonAppender) {
        val completableFuture =  PeachSpigotPlugin.instance.mongoHandler.chatTagLayer
            .fetchEntryByKey(jsonAppender.getParam("uniqueId"))

        completableFuture.thenAccept {
            PeachSpigotPlugin.instance.chatTagHandler.chatTags[it.uniqueId] = it
        }
    }
}
