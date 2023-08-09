package gg.scala.chattags.plugin.command

import gg.scala.aware.message.AwareMessage
import gg.scala.aware.thread.AwareThreadContext
import gg.scala.cache.uuid.ScalaStoreUuidCache
import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandCompletion
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Default
import gg.scala.commons.acf.annotation.Description
import gg.scala.commons.acf.annotation.HelpCommand
import gg.scala.commons.acf.annotation.Optional
import gg.scala.commons.acf.annotation.Single
import gg.scala.commons.acf.annotation.Subcommand
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.flavor.inject.Inject
import gg.scala.chattags.plugin.ScalaChatTagsPlugin
import gg.scala.chattags.plugin.conversion.ScalaLegacyPrefixConverter
import gg.scala.chattags.plugin.handler.ChatTagsService
import gg.scala.chattags.plugin.model.ChatTag
import gg.scala.chattags.plugin.model.ChatTagPlayer
import gg.scala.chattags.plugin.result.ChatTagPaginatedResult
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.util.CC
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
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
@CommandPermission("chattags.command.scchattags")
@CommandAlias("scchattags|sct|chattagsadmin|cta")
object ChatTagsAdminCommand : ScalaCommand()
{
    @Inject
    lateinit var plugin: ScalaChatTagsPlugin

    @Inject
    lateinit var audiences: BukkitAudiences

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
        val chatTagList = ChatTagsService.chatTags

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
                    throw ConditionFailedException("${CC.YELLOW}${ScalaStoreUuidCache.username(target)}${CC.RED} already owns the ${CC.YELLOW}${chatTag.displayName}${CC.RED} chat-tag.")
                }

                it.ownedTags.add(chatTag.uniqueId)

                it.save().thenRun {
                    AwareMessage.of(
                        "ctp-refresh",
                        plugin.aware,
                        "uniqueId" to chatTag.uniqueId
                    ).publish(
                        AwareThreadContext.SYNC
                    )
                }

                sender.sendMessage("${CC.SEC}You've granted ${CC.PRI}${ScalaStoreUuidCache.username(target)}${CC.SEC} the ${CC.WHITE}${chatTag.displayName}${CC.SEC} chat-tag.")

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
                    throw ConditionFailedException("${CC.YELLOW}${ScalaStoreUuidCache.username(target)}${CC.RED} does not own the ${CC.YELLOW}${chatTag.displayName}${CC.RED} chat-tag.")
                }

                it.ownedTags.remove(chatTag.uniqueId)

                it.save().thenRun {
                    AwareMessage.of(
                        "ctp-refresh",
                        plugin.aware,
                        "uniqueId" to chatTag.uniqueId
                    ).publish(
                        AwareThreadContext.SYNC
                    )
                }

                sender.sendMessage("${CC.SEC}You've removed ${CC.PRI}${ScalaStoreUuidCache.username(target)}'s${CC.SEC} access to the ${CC.WHITE}${chatTag.displayName}${CC.SEC} chat-tag.")

                return@thenApply null
            }
    }

    @Subcommand("create")
    @Description("Create a new chat-tag.")
    @CommandPermission("chattags.command.scchattags.management")
    fun onCreate(sender: CommandSender, @Single name: String)
    {
        val existing = ChatTagsService.findChatTag(name)

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

        chatTag.saveAndPushUpdatesGlobally(plugin).thenAccept {
            sender.sendMessage("${CC.SEC}You've created the ${CC.WHITE}${chatTag.displayName}${CC.SEC} chat tag.")
            sender.sendMessage("${CC.GRAY}Use /scchattags meta design ${chatTag.displayName} <design> to set the tag design.")
        }
    }

    @Subcommand("delete")
    @CommandCompletion("@chattags")
    @Description("Delete an existing chat-tag.")
    @CommandPermission("chattags.command.scchattags.management")
    fun onDelete(sender: CommandSender, chatTag: ChatTag)
    {
        DataStoreObjectControllerCache.findNotNull<ChatTag>()
            .delete(chatTag.identifier, DataStoreStorageType.MONGO)
            .thenRun {
                AwareMessage.of(
                    "chat-tag-delete",
                    plugin.aware,
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
    @CommandPermission("chattags.command.scchattags.meta.edit")
    fun onMetaDesign(sender: CommandSender, chatTag: ChatTag, design: String)
    {
        chatTag.value = MiniMessage
            .miniMessage()
            .deserialize(design)

        chatTag.saveAndPushUpdatesGlobally(plugin).thenRun {
            sender.sendMessage("${CC.SEC}You've updated ${CC.WHITE}${chatTag.displayName}'s${CC.SEC} design to ${chatTag.legacy}${CC.SEC}.")
        }
    }

    @Subcommand("tools import")
    @CommandPermission("chattags.command.scchattags.tools.import")
    @Description("Import from an existing prefix source.")
    fun onConvertTool(sender: CommandSender, url: String)
    {
        ScalaLegacyPrefixConverter
            .convert(
                URL(url).readText(), plugin, sender
            )
    }

    @Subcommand("tools parse")
    @Description("Preview a MiniMessage parsed message")
    fun onParse(player: Player, message: String)
    {
        val miniMessage = MiniMessage.miniMessage()
            .deserialize(message)

        val component = Component.text(
            "Your parsed mini-message: ",
            NamedTextColor.YELLOW
        )

        audiences.player(player).sendMessage(
            component.append(miniMessage)
        )
    }

}
