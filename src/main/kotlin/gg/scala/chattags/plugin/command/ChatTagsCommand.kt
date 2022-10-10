package gg.scala.chattags.plugin.command

import gg.scala.chattags.plugin.menu.ChatTagsMenu
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
@AutoRegister
object ChatTagsCommand : ScalaCommand()
{
    @CommandAlias("chattags|tags|prefixes")
    @CommandPermission("chattags.command.chattags")
    fun onChatTags(player: Player)
    {
        ChatTagsMenu().openMenu(player)
    }
}
