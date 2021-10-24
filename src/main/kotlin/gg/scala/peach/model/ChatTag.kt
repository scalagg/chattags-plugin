package gg.scala.peach.model

import gg.scala.common.Savable
import gg.scala.lemon.handler.RedisHandler
import gg.scala.peach.PeachSpigotPlugin
import gg.scala.peach.handler.MongoHandler
import net.evilblock.cubed.util.CC
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

    override fun save(): CompletableFuture<Void>
    {
        return MongoHandler.chatTagLayer.saveEntry(
            uniqueId.toString(), this
        )
    }

    fun saveAndPushUpdatesGlobally(): CompletableFuture<Void>
    {
        return save().thenApply {
            try
            {
                RedisHandler.buildMessage(
                    "chat-tag-update",
                    hashMapOf<String, String>().also {
                        it["uniqueId"] = uniqueId.toString()
                    }
                ).dispatch(
                    PeachSpigotPlugin.instance.banana
                )
            } catch (e: Exception)
            {
                e.printStackTrace()
            }

            return@thenApply null
        }
    }
}
