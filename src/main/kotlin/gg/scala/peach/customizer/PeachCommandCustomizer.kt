package gg.scala.peach.customizer

import gg.scala.commons.annotations.commands.customizer.CommandManagerCustomizer
import gg.scala.peach.handler.ChatTagHandler
import gg.scala.peach.model.ChatTag
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.command.manager.CubedCommandManager
import net.evilblock.cubed.util.CC

/**
 * @author GrowlyX
 * @since 4/16/2022
 */
object PeachCommandCustomizer
{
    @CommandManagerCustomizer
    fun customize(
        commandManager: CubedCommandManager
    )
    {
        commandManager.commandCompletions.registerAsyncCompletion("chattags") {
            return@registerAsyncCompletion ChatTagHandler.chatTags.values.map { it.displayName }
        }

        commandManager.commandContexts.registerContext(ChatTag::class.java) {
            val firstArgument = it.popFirstArg()

            return@registerContext ChatTagHandler.findChatTag(firstArgument)
                ?: throw ConditionFailedException("No chat-tag matching ${CC.YELLOW}$firstArgument${CC.RED} could be found.")
        }
    }
}
