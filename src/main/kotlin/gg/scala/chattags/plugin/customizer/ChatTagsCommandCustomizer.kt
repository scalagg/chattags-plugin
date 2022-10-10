package gg.scala.chattags.plugin.customizer

import gg.scala.commons.annotations.commands.customizer.CommandManagerCustomizer
import gg.scala.chattags.plugin.handler.ChatTagsService
import gg.scala.chattags.plugin.model.ChatTag
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.command.ScalaCommandManager
import net.evilblock.cubed.util.CC

/**
 * @author GrowlyX
 * @since 4/16/2022
 */
object ChatTagsCommandCustomizer
{
    @CommandManagerCustomizer
    fun customize(
        commandManager: ScalaCommandManager
    )
    {
        commandManager.commandCompletions
            .registerAsyncCompletion("chattags") {
                return@registerAsyncCompletion ChatTagsService
                    .chatTags.values.map { it.displayName }
            }

        commandManager.commandContexts
            .registerContext(ChatTag::class.java) {
                val firstArgument = it.popFirstArg()

                return@registerContext ChatTagsService.findChatTag(firstArgument)
                    ?: throw ConditionFailedException("No chat-tag matching ${CC.YELLOW}$firstArgument${CC.RED} could be found.")
            }
    }
}
