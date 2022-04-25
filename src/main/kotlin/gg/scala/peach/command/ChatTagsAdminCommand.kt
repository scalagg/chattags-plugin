package gg.scala.peach.command

import gg.scala.aware.message.AwareMessage
import gg.scala.aware.thread.AwareThreadContext
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.handler.RedisHandler.buildMessage
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.QuickAccess.sendGlobalPlayerFancyMessage
import gg.scala.peach.PeachSpigotPlugin
import gg.scala.peach.adventure.KyoriBridge
import gg.scala.peach.conversion.DeprecatedPrefixConverter
import gg.scala.peach.handler.ChatTagHandler
import gg.scala.peach.model.ChatTag
import gg.scala.peach.model.ChatTagPlayer
import gg.scala.peach.result.ChatTagPaginatedResult
import gg.scala.peach.service.PeachPlayerService
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.CommandHelp
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.acf.annotation.Optional
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
@AutoRegister
@CommandPermission("peach.command.cta")
@CommandAlias("peach|chattagadmin|cta|tag")
class ChatTagsAdminCommand : ScalaCommand()
{
    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @Subcommand("list")
    @Description("View all loaded chat-tags.")
    fun onList(sender: CommandSender, @Optional page: Int?)
    {
        val chatTagList = ChatTagHandler.chatTags

        if (chatTagList.isEmpty())
        {
            throw ConditionFailedException("There are no chat-tags.")
        }

        ChatTagPaginatedResult.display(
            sender, chatTagList.values.toList(),
            page ?: 1, "cta list %s"
        )
    }

    @Subcommand("add")
    @CommandCompletion("@all-players @chattags")
    @Description("Give a player a chat-tag.")
    fun onAdd(sender: CommandSender, target: UUID, chatTag: ChatTag): CompletableFuture<Void>
    {
        return DataStoreObjectControllerCache
            .findNotNull<ChatTagPlayer>()
            .load(target, DataStoreStorageType.MONGO)
            .thenApply {
                it ?: throw ConditionFailedException("This player has never logged on.")

                if (it.ownedTags.contains(chatTag.uniqueId))
                {
                    throw ConditionFailedException("${CC.YELLOW}${CubedCacheUtil.fetchName(target)}${CC.RED} already owns the ${CC.YELLOW}${chatTag.displayName}${CC.RED} chat-tag.")
                }

                it.ownedTags.add(chatTag.uniqueId)

                it.save().thenRun {
                    AwareMessage.of(
                        "ctp-refresh",
                        PeachSpigotPlugin.instance.aware,
                        "uniqueId" to chatTag.uniqueId
                    ).publish(
                        AwareThreadContext.SYNC
                    )
                }

                val fancyMessage = FancyMessage()
                fancyMessage.withMessage(
                    "${CC.GREEN}You've been granted the ${CC.WHITE}${chatTag.displayName}${CC.GREEN} chat-tag.",
                    "${CC.GRAY}Equip your new tag by executing: /tags."
                )

                sendGlobalPlayerFancyMessage(
                    fancyMessage = fancyMessage, uuid = target
                )

                sender.sendMessage("${CC.SEC}You've granted ${CC.PRI}${CubedCacheUtil.fetchName(target)}${CC.SEC} the ${CC.WHITE}${chatTag.displayName}${CC.SEC} chat-tag.")

                return@thenApply null
            }
    }

    @Subcommand("remove")
    @CommandCompletion("@all-players @chattags")
    @Description("Remove access to a chat-tag.")
    fun onRemove(sender: CommandSender, target: UUID, chatTag: ChatTag): CompletableFuture<Void>
    {
        return DataStoreObjectControllerCache
            .findNotNull<ChatTagPlayer>()
            .load(target, DataStoreStorageType.MONGO)
            .thenApply {
                it ?: throw ConditionFailedException("This player has never logged on.")

                if (!it.ownedTags.contains(chatTag.uniqueId))
                {
                    throw ConditionFailedException("${CC.YELLOW}${CubedCacheUtil.fetchName(target)}${CC.RED} does not own the ${CC.YELLOW}${chatTag.displayName}${CC.RED} chat-tag.")
                }

                it.ownedTags.remove(chatTag.uniqueId)

                it.save().thenRun {
                    AwareMessage.of(
                        "ctp-refresh",
                        PeachSpigotPlugin.instance.aware,
                        "uniqueId" to chatTag.uniqueId
                    ).publish(
                        AwareThreadContext.SYNC
                    )
                }

                sender.sendMessage("${CC.SEC}You've removed ${CC.PRI}${CubedCacheUtil.fetchName(target)}'s${CC.SEC} access to the ${CC.WHITE}${chatTag.displayName}${CC.SEC} chat-tag.")

                return@thenApply null
            }
    }

    @Subcommand("create")
    @Description("Create a new chat-tag.")
    @CommandPermission("peach.command.cta.management")
    fun onCreate(sender: CommandSender, @Single name: String)
    {
        val existing = ChatTagHandler.findChatTag(name)

        if (existing != null)
        {
            throw ConditionFailedException("A chat tag with the name matching ${CC.YELLOW}$name${CC.RED} already exists.")
        }

        if (name.isBlank())
        {
            throw ConditionFailedException("${CC.YELLOW}$name${CC.RED} must be at least 1 character long.")
        }

        if (name.length > 16)
        {
            throw ConditionFailedException("${CC.YELLOW}$name${CC.RED} must be at most 16 characters long.")
        }

        val chatTag = ChatTag(UUID.randomUUID(), name)

        chatTag.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've created the ${CC.WHITE}${chatTag.displayName}${CC.SEC} chat tag.")
            sender.sendMessage("${CC.GRAY}Use /peach meta design ${chatTag.displayName} <design> to set the tag design.")
        }
    }

    @Subcommand("delete")
    @CommandCompletion("@chattags")
    @Description("Delete an existing chat-tag.")
    @CommandPermission("peach.command.cta.management")
    fun onDelete(sender: CommandSender, chatTag: ChatTag)
    {
        DataStoreObjectControllerCache.findNotNull<ChatTag>()
            .delete(chatTag.identifier, DataStoreStorageType.MONGO)
            .thenRun {
                AwareMessage.of(
                    "chat-tag-delete",
                    PeachSpigotPlugin.instance.aware,
                    "uniqueId" to chatTag.uniqueId
                ).publish(
                    AwareThreadContext.SYNC
                )

                sender.sendMessage("${CC.SEC}You've deleted the ${CC.WHITE}${chatTag.displayName}${CC.SEC} chat-tag.")
            }
    }

    @Subcommand("meta design")
    @CommandCompletion("@chattags")
    @Description("Edit a chat-tags design.")
    @CommandPermission("peach.command.cta.meta.edit")
    fun onMetaDesign(sender: CommandSender, chatTag: ChatTag, design: String)
    {
        chatTag.value = MiniMessage
            .miniMessage()
            .deserialize(design)

        chatTag.saveAndPushUpdatesGlobally().thenRun {
            sender.sendMessage("${CC.RED} Versions of Peach 1.1.3+ now parse chat-tag designs through MiniMessage.")
            sender.sendMessage("")
            sender.sendMessage("${CC.RED} Please refer to Kyori's website to view its documentation:")
            sender.sendMessage("${CC.RED} ${CC.BOLD}https://docs.adventure.kyori.net/minimessage#format")
            sender.sendMessage("")
            sender.sendMessage("${CC.RED} To view your own MiniMessage design live, use:")
            sender.sendMessage("${CC.RED} ${CC.BOLD}/peach tools parse <mini-message>")
            sender.sendMessage("")
            sender.sendMessage("${CC.SEC}You've updated ${CC.WHITE}${chatTag.displayName}'s${CC.SEC} design to ${chatTag.value}${CC.SEC}.")
        }
    }

    @Subcommand("tools convert")
    @Description("Convert from an existing list of prefixes.")
    fun onConvertTool(sender: CommandSender, url: String)
    {
        val handle = URL(url)
        val content = StringBuilder()
        val urlConnection = handle.openConnection()

        val bufferedReader = BufferedReader(InputStreamReader(urlConnection.getInputStream()))
        var line: String?

        while (bufferedReader.readLine().also { line = it } != null)
        {
            content.append(line)
        }

        bufferedReader.close()

        DeprecatedPrefixConverter.convert(content.toString(), sender)
    }

    @Subcommand("tools parse")
    @Description("Preview a MiniMessage parsed message")
    fun onParse(player: Player, message: String)
    {
        val miniMessage = MiniMessage.miniMessage()
            .deserialize(message)

        val component = Component.text(
            "Your parsed mini-message: ", NamedTextColor.YELLOW
        )

        KyoriBridge.adventure().player(player).sendMessage(
            component.append(miniMessage)
        )
    }

}
