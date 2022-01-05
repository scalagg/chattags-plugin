package gg.scala.peach.model

import gg.scala.common.Savable
import gg.scala.lemon.handler.RedisHandler
import gg.scala.peach.PeachSpigotPlugin
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.storable.IDataStoreObject
import gg.scala.store.storage.type.DataStoreStorageType
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
) : Savable, IDataStoreObject
{
    override val identifier: UUID
        get() = uniqueId

    override fun save(): CompletableFuture<Void>
    {
        return DataStoreObjectControllerCache.findNotNull<ChatTag>()
            .save(this, DataStoreStorageType.MONGO)
    }

    fun saveAndPushUpdatesGlobally(): CompletableFuture<Void>
    {
        return save().thenApply {
            RedisHandler.buildMessage(
                "chat-tag-update",
                "uniqueId" to uniqueId.toString()
            ).dispatch(
                PeachSpigotPlugin.instance.banana
            )

            return@thenApply null
        }
    }
}
