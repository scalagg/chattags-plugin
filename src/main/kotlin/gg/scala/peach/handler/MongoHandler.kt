package gg.scala.peach.handler

import com.solexgames.datastore.commons.connection.impl.mongo.UriMongoConnection
import com.solexgames.datastore.commons.layer.impl.MongoStorageLayer
import com.solexgames.datastore.commons.storage.impl.MongoStorageBuilder
import gg.scala.lemon.Lemon
import gg.scala.peach.model.ChatTag
import net.evilblock.cubed.serializers.Serializers

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
object MongoHandler {

    var chatTagLayer: MongoStorageLayer<ChatTag>

    init {
        val mongoConnection = UriMongoConnection(Lemon.instance.mongoConfig.uri)
        val database = Lemon.instance.mongoConfig.database

        chatTagLayer = MongoStorageBuilder<ChatTag>()
            .setDatabase(database).setCollection("peach_chattags")
            .setConnection(mongoConnection).setType(ChatTag::class.java).build()

        chatTagLayer.supplyWithCustomGson(Serializers.gson)
    }
}
