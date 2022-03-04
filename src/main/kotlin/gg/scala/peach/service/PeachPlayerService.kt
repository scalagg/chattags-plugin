package gg.scala.peach.service

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.peach.model.ChatTagPlayer
import gg.scala.store.controller.DataStoreObjectController
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.impl.CachedDataStoreStorageLayer
import gg.scala.store.storage.type.DataStoreStorageType
import me.lucko.helper.Events
import net.evilblock.cubed.serializers.Serializers
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.Delegates

/**
 * @author GrowlyX
 * @since 1/5/2022
 */
@Service
object PeachPlayerService
{
    var controller by Delegates.notNull<DataStoreObjectController<ChatTagPlayer>>()

    private val players: ConcurrentHashMap<UUID, ChatTagPlayer>
        get() = controller.useLayerWithReturn<CachedDataStoreStorageLayer<ChatTagPlayer>, ConcurrentHashMap<UUID, ChatTagPlayer>>(
            DataStoreStorageType.CACHE
        ) {
            this.connection.getConnection()
        }

    @Configure
    fun configure()
    {
        controller = DataStoreObjectControllerCache.create()

        Events.subscribe(AsyncPlayerPreLoginEvent::class.java)
            .handler {
                controller.loadAndCache(
                    it.uniqueId,
                    {
                        ChatTagPlayer(it.uniqueId)
                    },
                    DataStoreStorageType.MONGO
                )
            }

        Events.subscribe(PlayerQuitEvent::class.java)
            .handler {
                players.remove(it.player.uniqueId)?.save()
            }
    }
}
