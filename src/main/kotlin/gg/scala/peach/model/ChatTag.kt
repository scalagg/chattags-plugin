package gg.scala.peach.model

import gg.scala.common.Savable
import gg.scala.lemon.handler.RedisHandler
import gg.scala.peach.PeachSpigotPlugin
import gg.scala.peach.handler.MongoHandler
import net.evilblock.cubed.util.CC
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
data class ChatTag(
    val uniqueId: UUID,
    var displayName: String,
    var value: String = CC.GRAY,
) : Savable
{
    val miniMessage = MiniMessage.get().parse(value)

    override fun save(): CompletableFuture<Void> {
        return MongoHandler.chatTagLayer.saveEntry(
            uniqueId.toString(), this
        )
    }

    fun saveAndPushUpdatesGlobally(): CompletableFuture<Void> {
        return save().thenApply {
            RedisHandler.buildMessage(
                "chat-tag-update",
                hashMapOf<String, String>().also {
                    it["uniqueId"] = uniqueId.toString()
                }
            ).dispatch(
                PeachSpigotPlugin.instance.banana
            )

            return@thenApply null
        }
    }
}
