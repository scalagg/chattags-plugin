package gg.scala.peach.command

import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.peach.menu.ChatTagsMenu
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.Default
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
@AutoRegister
@CommandAlias("chattags|tags|prefixes")
object ChatTagsCommand : ScalaCommand()
{
    @Default
    fun onChatTags(player: Player)
    {
        ChatTagsMenu().openMenu(player)
    }
}
