package gg.scala.peach.channel

import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.channel.impl.DefaultChannel
import gg.scala.lemon.player.rank.Rank
import gg.scala.peach.PeachSpigotPlugin
import gg.scala.peach.handler.ChatTagHandler
import gg.scala.peach.service.PeachPlayerService
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
object ChatTagDefaultChannel : DefaultChannel() {

    override fun getId(): String {
        return "default"
    }

    override fun getFormatted(message: String, sender: String, rank: Rank, receiver: Player): String {
        val bukkitPlayer = Bukkit.getPlayer(sender)
        val lemonPlayer = PlayerHandler.findPlayer(bukkitPlayer).orElse(null)

        return "${rank.prefix}${lemonPlayer.getColoredName(rank)}${rank.suffix}${getChatTag(bukkitPlayer)}${CC.WHITE}: ${colorIfHasPermission(bukkitPlayer, message).replace(receiver.name, "${CC.YELLOW}${receiver.name}${CC.RESET}")}"
    }

    override fun getPrefix(): String? {
        return null
    }

    override fun getPermission(): String? {
        return null
    }

    override fun shouldCheckForPrefix(): Boolean {
        return false
    }

    override fun isGlobal(): Boolean {
        return false
    }

    override fun getChatTag(player: Player): String
    {
        val chatTagPlayer = PeachPlayerService
            .players[player.uniqueId]!!

        return chatTagPlayer.let {
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
