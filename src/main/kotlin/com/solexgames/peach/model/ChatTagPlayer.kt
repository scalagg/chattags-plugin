package com.solexgames.peach.model

import java.util.*

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
class ChatTagPlayer {

    var currentChatTag: UUID? = null

    val ownedTags: MutableList<String> = mutableListOf()

}
