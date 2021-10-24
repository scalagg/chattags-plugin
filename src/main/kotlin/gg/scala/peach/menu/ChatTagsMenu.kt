package gg.scala.peach.menu

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.peach.PeachSpigotPlugin
import gg.scala.peach.handler.ChatTagHandler
import gg.scala.peach.model.ChatTag
import gg.scala.peach.model.ChatTagPlayer
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material
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

    override fun getPrePaginatedTitle(player: Player): String
    {
        return "Chat Tags"
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        return hashMapOf<Int, Button>().also {
            val chatTagPlayer = PeachSpigotPlugin.instance.players[player.uniqueId]!!

            ChatTagHandler.chatTags.values.forEach { chatTag ->
                it[it.size] = ChatTagButton(chatTag, chatTagPlayer)
            }
        }
    }

    override fun getGlobalButtons(player: Player): Map<Int, Button>
    {
        return hashMapOf<Int, Button>().also {
            it[4] = ItemBuilder(Material.BED)
                .name("${CC.RED}Reset Tag")
                .addToLore(
                    "${CC.GRAY}Resets your current",
                    "${CC.GRAY}chat tag.",
                    "",
                    "${CC.YELLOW}Click to reset."
                )
                .toButton { clicker, _ ->
                    if (clicker != null)
                    {
                        val chatTagPlayer = PeachSpigotPlugin.instance.players[player.uniqueId]!!

                        if (chatTagPlayer.currentChatTag == null)
                        {
                            clicker.sendMessage("${CC.RED}You don't have a tag equipped.")
                            return@toButton
                        }

                        chatTagPlayer.currentChatTag = null

                        clicker.sendMessage("${CC.GREEN}You've reset your tag.")
                    }
                }
        }
    }

    class ChatTagButton(
        private val chatTag: ChatTag,
        private val chatTagPlayer: ChatTagPlayer
    ) : Button()
    {

        override fun getButtonItem(player: Player): ItemStack
        {
            val lore = mutableListOf<String>()
            val ownsPrefix = chatTagPlayer.ownedTags.contains(chatTag.uniqueId)

            if (ownsPrefix)
            {
                lore.add("${CC.GRAY}You own this prefix and it")
                lore.add("${CC.GRAY}can be enabled at any time.")
            } else
            {
                lore.add("${CC.GRAY}This prefix is currently")
                lore.add("${CC.GRAY}locked as you don't own it.")
            }

            lore.add("")
            lore.add("${CC.GRAY}Shows up in chat as:")
            lore.add(chatTag.value)
            lore.add("")
            lore.add(
                if (!player.hasPermission("peach.all-chattags") || !ownsPrefix)
                {
                    "${CC.GREEN}Click to equip this tag."
                } else
                {
                    "${CC.RED}You can't equip this tag."
                }
            )

            return ItemBuilder(XMaterial.NAME_TAG)
                .name("${CC.PRI}${chatTag.displayName}")
                .setLore(lore).build()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView)
        {
            if (!player.hasPermission("peach.all-chattags") || !player.isOp)
            {
                if (!chatTagPlayer.ownedTags.contains(chatTag.uniqueId))
                {
                    player.sendMessage("${CC.RED}You don't own the ${CC.YELLOW}${chatTag.displayName}${CC.RED} tag.")
                    return
                }
            }

            if (chatTagPlayer.currentChatTag != null && chatTagPlayer.currentChatTag == chatTag.uniqueId)
            {
                player.sendMessage("${CC.RED}You already have the ${CC.YELLOW}${chatTag.displayName}${CC.RED} tag equipped.")
                return
            }

            chatTagPlayer.currentChatTag = chatTag.uniqueId

            PeachSpigotPlugin.instance.finalizePlayer(player)
            PlayerHandler.findPlayer(player.uniqueId).orElse(null)?.save()

            player.sendMessage("${CC.SEC}You've equipped the ${CC.PRI}${chatTag.displayName} ${CC.GRAY}(${chatTag.value}${CC.GRAY})${CC.SEC} chat tag.")
        }
    }

}
