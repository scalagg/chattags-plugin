package gg.scala.peach.channel

import gg.scala.lemon.player.channel.impl.DefaultChannel
import gg.scala.peach.PeachSpigotPlugin
import gg.scala.peach.handler.ChatTagHandler
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
class ChatTagDefaultChannel : DefaultChannel() {

    override fun getChatTag(player: Player): String? {
        val chatTagPlayer = PeachSpigotPlugin.instance.players[player.uniqueId]

        return chatTagPlayer?.let {
            if (it.currentChatTag == null) {
                ""
            } else {
                val chatTag = ChatTagHandler.chatTags[it.currentChatTag]

                if (chatTag == null) {
                    ""
                } else {
                    " ${chatTag.value}"
                }
            }
        }
    }
}
