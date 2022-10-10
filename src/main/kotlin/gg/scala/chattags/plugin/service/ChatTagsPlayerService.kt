package gg.scala.chattags.plugin.service

import gg.scala.commons.persist.ProfileOrchestrator
import gg.scala.flavor.service.Service
import gg.scala.chattags.plugin.model.ChatTagPlayer
import java.util.*

/**
 * @author GrowlyX
 * @since 1/5/2022
 */
@Service
object ChatTagsPlayerService : ProfileOrchestrator<ChatTagPlayer>()
{
    override fun new(uniqueId: UUID) = ChatTagPlayer(uniqueId)
    override fun type() = ChatTagPlayer::class
}
