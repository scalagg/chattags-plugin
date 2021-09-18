package gg.scala.peach.channel

import com.solexgames.lemon.player.channel.impl.DefaultChannel
import gg.scala.peach.PeachSpigotPlugin
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
                val chatTag = PeachSpigotPlugin.instance.chatTagHandler.chatTags[it.currentChatTag]

                if (chatTag == null) {
                    ""
                } else {
                    " ${chatTag.value}"
                }
            }
        }
    }
}
