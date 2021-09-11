package com.solexgames.peach.model

import com.solexgames.lemon.handler.RedisHandler
import com.solexgames.lemon.util.type.Savable
import com.solexgames.peach.PeachSpigotPlugin
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.HashMap

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
data class ChatTag(
   val uniqueId: UUID,
   var displayName: String,
   var value: String,
): Savable {

    override fun save(): CompletableFuture<Void> {
        return PeachSpigotPlugin.instance.mongoHandler.chatTagLayer.saveEntry(
            uniqueId.toString(), this
        )
    }

    fun saveAndPushUpdatesGlobally(): CompletableFuture<Void> {
        return save().thenApply {
            RedisHandler.buildMessage(
                "chat-tag-update",
                HashMap<String, String>().also {
                    it["uniqueId"] = uniqueId.toString()
                }
            ).publishAsync("peach")

            return@thenApply null
        }
    }

}
