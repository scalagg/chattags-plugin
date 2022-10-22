package gg.scala.chattags.plugin.model

import gg.scala.aware.message.AwareMessage
import gg.scala.aware.thread.AwareThreadContext
import gg.scala.chattags.plugin.ScalaChatTagsPlugin
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
) : IDataStoreObject
{
    override val identifier: UUID
        get() = uniqueId

    val legacy: String
        get() = LegacyComponentSerializer
            .legacySection()
            .serialize(value)

    private fun save(): CompletableFuture<Void>
    {
        return DataStoreObjectControllerCache.findNotNull<ChatTag>()
            .save(this, DataStoreStorageType.MONGO)
    }

    fun saveAndPushUpdatesGlobally(plugin: ScalaChatTagsPlugin) =
        save()
            .thenRun {
                AwareMessage.of(
                    "chat-tag-update",
                    plugin.aware,
                    "uniqueId" to uniqueId
                ).publish(
                    AwareThreadContext.SYNC
                )
            }!!
}
