package gg.scala.chattags.plugin.model

import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.storable.IDataStoreObject
import gg.scala.store.storage.type.DataStoreStorageType
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
data class ChatTagPlayer(
    private val uniqueId: UUID,
    override val identifier: UUID = uniqueId
) : IDataStoreObject
{
    var currentChatTag: UUID? = null

    val ownedTags = mutableListOf<UUID>()
    val fixedOwnedTags = mutableListOf<UUID>()

    var hasSpoofBeenFixed = false

    fun save(): CompletableFuture<Void>
    {
        return DataStoreObjectControllerCache
            .findNotNull<ChatTagPlayer>()
            .save(this, DataStoreStorageType.MONGO)
    }
}
