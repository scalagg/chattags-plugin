package gg.scala.peach.handler

import gg.scala.aware.annotation.Subscribe
import gg.scala.aware.message.AwareMessage
import gg.scala.peach.model.ChatTag
import gg.scala.peach.model.ChatTagPlayer
import gg.scala.peach.service.PeachPlayerService
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import org.bukkit.Bukkit
import java.util.*

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
object PeachRedisHandler
{
    @Subscribe("ctp-refresh")
    fun onCtpRefresh(message: AwareMessage)
    {
        val uniqueId = message
            .retrieve<UUID>("uniqueId")

        Bukkit.getPlayer(uniqueId)?.let {
            val original = PeachPlayerService.find(it)
                ?: return@let null

            val chatTags = original.ownedTags

            DataStoreObjectControllerCache
                .findNotNull<ChatTagPlayer>()
                .loadOptimalCopy(uniqueId) {
                    ChatTagPlayer(uniqueId)
                }
                .thenAccept { player ->
                    val newTags = player.ownedTags
                        .filter { chatTag -> chatTag !in chatTags }

                    for (newTag in newTags)
                    {
                        val chatTag = ChatTagHandler.chatTags[newTag]
                            ?: continue

                        val fancyMessage = FancyMessage()
                        fancyMessage.withMessage(
                            "${CC.GREEN}You've been granted the ${CC.WHITE}${chatTag.displayName}${CC.GREEN} chat-tag.",
                            "${CC.GRAY}Equip your new tag by executing: /tags."
                        )

                        fancyMessage.sendToPlayer(it)
                    }
                }
        }
    }

    @Subscribe("chat-tag-delete")
    fun onChatTagDelete(message: AwareMessage)
    {
        val rankUuid = message
            .retrieve<UUID>("uniqueId")

        ChatTagHandler.chatTags
            .remove(rankUuid)
    }

    @Subscribe("chat-tag-update")
    fun onChatTagUpdate(message: AwareMessage)
    {
        val controller = DataStoreObjectControllerCache
            .findNotNull<ChatTag>()

        controller.load(
            message.retrieve("uniqueId"),
            DataStoreStorageType.MONGO
        ).thenAccept {
            it ?: return@thenAccept
            ChatTagHandler.chatTags[it.uniqueId] = it
        }
    }
}
