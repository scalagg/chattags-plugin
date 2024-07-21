package gg.scala.chattags.plugin.menu

import com.cryptomorin.xseries.XMaterial
import gg.scala.chattags.plugin.handler.ChatTagsService
import gg.scala.chattags.plugin.model.ChatTag
import gg.scala.chattags.plugin.model.ChatTagPlayer
import gg.scala.chattags.plugin.service.ChatTagsPlayerService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
class ChatTagsMenu : PaginatedMenu()
{
    companion object
    {
        @JvmStatic
        val SLOTS = listOf(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
        )
    }

    init
    {
        placeholdBorders = true
        updateAfterClick = true
    }

    override fun size(buttons: Map<Int, Button>): Int = 45
    override fun getMaxItemsPerPage(player: Player) = 21

    override fun getAllPagesButtonSlots(): List<Int> = SLOTS

    override fun getPrePaginatedTitle(player: Player): String = "Chat Tags"

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        val chatTagPlayer = ChatTagsPlayerService.find(player)!!

        return mutableMapOf<Int, Button>().also {
            ChatTagsService.chatTags.forEach { chatTag ->
                it[it.size] = ChatTagButton(chatTag.value, chatTagPlayer)
            }
        }
    }

    inner class ChatTagButton(
        private val chatTag: ChatTag,
        private val chatTagPlayer: ChatTagPlayer
    ) : Button()
    {
        override fun getButtonItem(player: Player): ItemStack
        {
            val lore = mutableListOf<String>()

            val ownsPrefix = chatTagPlayer.ownedTags.contains(chatTag.uniqueId) || player.hasPermission("chattags.all-chattags")
            val equipped = chatTagPlayer.currentChatTag != null && chatTagPlayer.currentChatTag == chatTag.uniqueId

            lore.add("")
            lore += if (ownsPrefix)
            {
                if (equipped)
                {
                    "${CC.B_GREEN}This tag is equipped!"
                } else
                {
                    "${CC.B_YELLOW}Click to equip!"
                }
            } else
            {
                "${CC.RED}You don't own this tag!"
            }

            val itemBuilder = ItemBuilder
                .of(
                    if (equipped) XMaterial.LIME_DYE else (if (ownsPrefix) XMaterial.PAPER else XMaterial.NAME_TAG)
                )
                .name("${CC.B_PRI}${chatTag.legacy}")
                .setLore(lore)

            if (equipped)
            {
                itemBuilder.glow()
            }

            return itemBuilder.build()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView)
        {
            if (
                !chatTagPlayer.ownedTags.contains(chatTag.uniqueId) &&
                !player.hasPermission("chattags.all-chattags")
            )
            {
                player.sendMessage("${CC.RED}You don't own the ${CC.YELLOW}${chatTag.displayName}${CC.RED} tag.")
                return
            }
            if (chatTagPlayer.currentChatTag != null && chatTagPlayer.currentChatTag == chatTag.uniqueId)
            {
                player.sendMessage("${CC.RED}You've reset your chat-tag.")
                chatTagPlayer.currentChatTag = null
                return
            }

            chatTagPlayer.currentChatTag = chatTag.uniqueId
            chatTagPlayer.save()

            player.sendMessage("${CC.SEC}You've equipped the ${CC.PRI}${chatTag.displayName} ${CC.GRAY}(${chatTag.legacy}${CC.GRAY})${CC.SEC} chat tag.")
        }
    }

}
