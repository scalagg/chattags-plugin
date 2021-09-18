package gg.scala.peach.model

import java.util.*

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
class ChatTagPlayer {

    var currentChatTag: UUID? = null

    val ownedTags: MutableList<UUID> = mutableListOf()

}
