package gg.scala.peach.conversion

import com.google.gson.reflect.TypeToken
import gg.scala.peach.handler.ChatTagHandler
import gg.scala.peach.model.ChatTag
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender
import java.lang.reflect.Type
import java.util.*

/**
 * @author GrowlyX
 * @since 10/24/2021
 */
object DeprecatedPrefixConverter
{
    @JvmStatic
    val DEPRECATED_PREFIX_LIST_TYPE: Type = object : TypeToken<List<DeprecatedPrefix>>() {}.type

    fun convert(json: String, sender: CommandSender)
    {
        val deprecated = Serializers.gson.fromJson<List<DeprecatedPrefix>>(
            json, DEPRECATED_PREFIX_LIST_TYPE
        )

        deprecated.forEach {
            val existing = ChatTagHandler.findChatTag(it.name)

            if (existing != null)
            {
                sender.sendMessage("${CC.RED}Skipping ${it.name} as a chat-tag with that name already exists.")
                return@forEach
            }

            val newModel = ChatTag(UUID.randomUUID(), it.displayName, it.prefix)
            newModel.saveAndPushUpdatesGlobally()

            sender.sendMessage("${CC.GREEN}Successfully converted ${CC.YELLOW}${newModel.displayName}${CC.GREEN}.")
        }
    }
}
