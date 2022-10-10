package gg.scala.chattags.plugin.conversion

import com.google.gson.reflect.TypeToken
import gg.scala.chattags.plugin.ScalaChatTagsPlugin
import gg.scala.chattags.plugin.handler.ChatTagsService
import gg.scala.chattags.plugin.model.ChatTag
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.command.CommandSender
import java.lang.reflect.Type
import java.util.*

/**
 * @author GrowlyX
 * @since 10/24/2021
 */
object ScalaLegacyPrefixConverter
{
    @JvmStatic
    val DEPRECATED_PREFIX_LIST_TYPE: Type = object : TypeToken<List<ScalaLegacyPrefix>>()
    {}.type

    fun convert(json: String, plugin: ScalaChatTagsPlugin, sender: CommandSender)
    {
        val deprecated = Serializers.gson
            .fromJson<List<ScalaLegacyPrefix>>(
                json, DEPRECATED_PREFIX_LIST_TYPE
            )

        deprecated.forEach {
            val existing = ChatTagsService.findChatTag(it.name)

            if (existing != null)
            {
                sender.sendMessage("${CC.RED}Skipping ${it.name} as a chat-tag with that name already exists.")
                return@forEach
            }

            val newModel = ChatTag(
                UUID.randomUUID(),
                it.displayName,
                LegacyComponentSerializer
                    .legacyAmpersand()
                    .deserialize(
                        Color.translate(it.prefix)
                    )
            )
            newModel.saveAndPushUpdatesGlobally(plugin)

            sender.sendMessage("${CC.GREEN}Successfully converted ${CC.YELLOW}${newModel.displayName}${CC.GREEN}.")
        }
    }
}
