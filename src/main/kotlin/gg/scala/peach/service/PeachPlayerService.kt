package gg.scala.peach.service

import gg.scala.commons.persist.ProfileOrchestrator
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.peach.model.ChatTagPlayer
import java.util.*

/**
 * @author GrowlyX
 * @since 1/5/2022
 */
@Service
object PeachPlayerService : ProfileOrchestrator<ChatTagPlayer>()
{
    @Configure
    fun configure()
    {
        this.subscribe()
    }

    override fun new(uniqueId: UUID) = ChatTagPlayer(uniqueId)
    override fun type() = ChatTagPlayer::class
}
