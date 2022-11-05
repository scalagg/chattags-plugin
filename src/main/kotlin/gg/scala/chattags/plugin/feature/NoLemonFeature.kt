package gg.scala.chattags.plugin.feature

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.chattags.plugin.handler.ChatTagsService
import gg.scala.chattags.plugin.service.ChatTagsPlayerService
import me.lucko.helper.Events
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.player.AsyncPlayerChatEvent

/**
 * @author GrowlyX
 * @since 10/9/2022
 */
@Service
object NoLemonFeature
{
    @Configure
    fun configure()
    {
        if (Bukkit.getPluginManager().getPlugin("Lemon") == null)
        {
            Events.subscribe(AsyncPlayerChatEvent::class.java)
                .handler {
                    val chatTagPlayer = ChatTagsPlayerService
                        .find(it.player)
                        ?: return@handler

                    if (
                        chatTagPlayer.currentChatTag != null
                    )
                    {
                        val chatTag =
                            ChatTagsService.chatTags[chatTagPlayer.currentChatTag]
                                ?: return@handler

                        it.format = "${chatTag.legacy} ${ChatColor.RESET}${it.player.displayName}: ${it.message}"
                        return@handler
                    }

                    it.format = "${it.player.name}: ${it.message}"
                }
        }
    }
}
