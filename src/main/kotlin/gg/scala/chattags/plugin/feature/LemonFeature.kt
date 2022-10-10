package gg.scala.chattags.plugin.feature

import gg.scala.commons.annotations.plugin.SoftDependency
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import gg.scala.lemon.channel.channels.DefaultChatChannel
import gg.scala.chattags.plugin.handler.ChatTagsService
import gg.scala.chattags.plugin.service.ChatTagsPlayerService
import net.kyori.adventure.text.Component

/**
 * @author GrowlyX
 * @since 9/11/2022
 */
@Service
@IgnoreAutoScan
@SoftDependency("Lemon")
object LemonFeature
{
    @Configure
    fun configure()
    {
        val blank = Component.text("")

        DefaultChatChannel.provideChatTag { player ->
            val chatTagPlayer = ChatTagsPlayerService
                .find(player)
                ?: return@provideChatTag blank

            if (
                chatTagPlayer.currentChatTag == null
            )
            {
                return@provideChatTag blank
            }

            val chatTag =
                ChatTagsService.chatTags[chatTagPlayer.currentChatTag]
                    ?: return@provideChatTag blank

            return@provideChatTag Component
                .text(" ")
                .append {
                    chatTag.value
                }
        }
    }
}
