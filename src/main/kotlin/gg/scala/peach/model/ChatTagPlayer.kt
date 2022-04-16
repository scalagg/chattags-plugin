package gg.scala.peach.model

import gg.scala.common.Savable
import gg.scala.peach.service.PeachPlayerService
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
) : IDataStoreObject, Savable
{
    var currentChatTag: UUID? = null
    val ownedTags = mutableListOf<UUID>()

    override fun save(): CompletableFuture<Void>
    {
        return PeachPlayerService.controller
            .save(this, DataStoreStorageType.MONGO)
    }
}
