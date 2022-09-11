package gg.scala.peach.model

import gg.scala.aware.message.AwareMessage
import gg.scala.aware.thread.AwareThreadContext
import gg.scala.common.Savable
import gg.scala.peach.PeachSpigotPlugin
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.storable.IDataStoreObject
import gg.scala.store.storage.type.DataStoreStorageType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
data class ChatTag(
    val uniqueId: UUID,
    var displayName: String,
    var value: Component =
        Component.empty(),
) : Savable, IDataStoreObject
{
    override val identifier: UUID
        get() = uniqueId

    val legacy: String
        get() = LegacyComponentSerializer
            .legacySection()
            .serialize(value)

    override fun save(): CompletableFuture<Void>
    {
        return DataStoreObjectControllerCache.findNotNull<ChatTag>()
            .save(this, DataStoreStorageType.MONGO)
    }

    fun saveAndPushUpdatesGlobally(): CompletableFuture<Void>
    {
        return save().thenApply {
            AwareMessage.of(
                "chat-tag-update",
                PeachSpigotPlugin.instance.aware,
                "uniqueId" to uniqueId
            ).publish(
                AwareThreadContext.SYNC
            )

            return@thenApply null
        }
    }
}
