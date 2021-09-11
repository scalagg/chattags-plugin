package com.solexgames.peach.command

import com.solexgames.peach.menu.ChatTagsMenu
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
class ChatTagsCommand : BaseCommand() {

    @CommandAlias("chattags|tags")
    fun onChatTags(player: Player) {
        ChatTagsMenu().openMenu(player)
    }
}
