package com.solexgames.peach.menu

import com.cryptomorin.xseries.XMaterial
import com.solexgames.peach.PeachSpigotPlugin
import com.solexgames.peach.model.ChatTag
import com.solexgames.peach.model.ChatTagPlayer
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
class ChatTagsMenu : PaginatedMenu() {

    override fun getPrePaginatedTitle(player: Player): String {
        return "Chat Tags"
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        return hashMapOf<Int, Button>().also {
            val chatTagPlayer = PeachSpigotPlugin.instance.players[player.uniqueId]!!

            PeachSpigotPlugin.instance.chatTagHandler.chatTags.values.forEach { chatTag ->
                it[it.size] = ChatTagButton(chatTag, chatTagPlayer)
            }
        }
    }

    class ChatTagButton(
        private val chatTag: ChatTag,
        private val chatTagPlayer: ChatTagPlayer
    ) : Button() {

        override fun getButtonItem(player: Player): ItemStack {
            val lore = mutableListOf<String>()

            lore.add("${CC.GRAY}Shows up in chat as:")
            lore.add(chatTag.value)
            lore.add("")
            lore.add(
                if (chatTagPlayer.ownedTags.contains(chatTag.displayName)) {
                    "${CC.GREEN}Click to equip this tag."
                } else {
                    "${CC.RED}You can't equip this tag."
                }
            )

            return ItemBuilder(XMaterial.NAME_TAG)
                .name("${CC.PRI}${chatTag.displayName}")
                .setLore(lore).build()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            if (!chatTagPlayer.ownedTags.contains(chatTag.displayName)) {
                player.sendMessage("${CC.RED}You don't own the ${CC.YELLOW}${chatTag.displayName}${CC.RED} tag.")
                return
            }

            if (chatTagPlayer.currentChatTag != null && chatTagPlayer.currentChatTag == chatTag.uniqueId) {
                player.sendMessage("${CC.RED}You already have the ${CC.YELLOW}${chatTag.displayName}${CC.RED} tag equipped.")
                return
            }

            chatTagPlayer.currentChatTag = chatTag.uniqueId

            player.sendMessage("${CC.SEC}You've equipped the ${CC.PRI}${chatTag.displayName} ${CC.GRAY}(${chatTag.value}${CC.GRAY})${CC.SEC} chat tag.")
        }
    }

}
