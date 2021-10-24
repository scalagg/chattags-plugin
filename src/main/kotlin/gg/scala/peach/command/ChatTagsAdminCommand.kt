package gg.scala.peach.command

import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.handler.RedisHandler
import gg.scala.peach.PeachSpigotPlugin
import gg.scala.peach.adventure.KyoriBridge
import gg.scala.peach.conversion.DeprecatedPrefixConverter
import gg.scala.peach.handler.ChatTagHandler
import gg.scala.peach.handler.MongoHandler
import gg.scala.peach.model.ChatTag
import gg.scala.peach.result.ChatTagPaginatedResult
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.CommandHelp
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.acf.annotation.Optional
import net.evilblock.cubed.acf.bukkit.contexts.OnlinePlayer
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.*


/**
 * @author GrowlyX
 * @since 9/11/2021
 */
@CommandAlias("peach|chattagadmin|cta")
@CommandPermission("peach.command.cta")
class ChatTagsAdminCommand : BaseCommand()
{

    @Default
    @HelpCommand
    @Syntax("[help]")
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @Subcommand("parse")
    fun onParse(player: Player, message: String)
    {
        val miniMessage = MiniMessage.get().parse(message)

        val component = net.kyori.adventure.text.Component.text(
            "Your parsed mini-message: ", TextColor.color(255, 255, 0)
        )

        KyoriBridge.adventure().player(player).sendMessage(
            component.append(miniMessage)
        )
    }

    @Subcommand("list")
    @Description("View all chat-tags.")
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
    @Syntax("<player> <chatTag>")
    @CommandCompletion("@all-players @chattags")
    @Description("Give a player a chat-tag.")
    fun onAdd(sender: CommandSender, target: OnlinePlayer, chatTag: ChatTag)
    {
        val chatTagPlayer = PeachSpigotPlugin.instance.players[target.player.uniqueId]
            ?: throw ConditionFailedException("That player does not exist.")

        if (chatTagPlayer.ownedTags.contains(chatTag.uniqueId))
        {
            throw ConditionFailedException("${CC.YELLOW}${target.player.name}${CC.RED} already owns the ${CC.YELLOW}${chatTag.displayName}${CC.RED} chat-tag.")
        }

        chatTagPlayer.ownedTags.add(chatTag.uniqueId)

        target.player.sendMessage("${CC.GREEN}You've been granted the ${CC.WHITE}${chatTag.displayName}${CC.GREEN} tag.")
        target.player.sendMessage("${CC.GRAY}Equip your new tag using /tags.")

        PlayerHandler.findPlayer(target.player.uniqueId).orElse(null)?.save()

        sender.sendMessage("${CC.SEC}You've granted ${CC.WHITE}${target.player.name}${CC.SEC} the ${CC.WHITE}${chatTag.displayName}${CC.SEC} chat-tag.")
    }

    @Subcommand("remove")
    @Syntax("<player> <chatTag>")
    @CommandCompletion("@all-players @chattags")
    @Description("Remove access to a chat-tag.")
    fun onRemove(sender: CommandSender, target: OnlinePlayer, chatTag: ChatTag)
    {
        val chatTagPlayer = PeachSpigotPlugin.instance.players[target.player.uniqueId]
            ?: throw ConditionFailedException("That player does not exist.")

        if (!chatTagPlayer.ownedTags.contains(chatTag.uniqueId))
        {
            throw ConditionFailedException("${CC.YELLOW}${target.player.name}${CC.RED} does not own the ${CC.YELLOW}${chatTag.displayName}${CC.RED} chat-tag.")
        }

        chatTagPlayer.ownedTags.remove(chatTag.uniqueId)
        PlayerHandler.findPlayer(target.player.uniqueId).orElse(null)?.save()

        sender.sendMessage("${CC.SEC}You've removed ${CC.WHITE}${target.player.name}'s${CC.SEC} access to the ${CC.WHITE}${chatTag.displayName}${CC.SEC} chat-tag.")
    }

    @Syntax("<name>")
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

    @Syntax("<chatTag>")
    @Subcommand("delete")
    @CommandCompletion("@chattags")
    @Description("Delete an existing chat-tag.")
    @CommandPermission("peach.command.cta.management")
    fun onDelete(sender: CommandSender, chatTag: ChatTag)
    {
        MongoHandler.chatTagLayer.deleteEntry(chatTag.uniqueId.toString()).thenAccept {
            RedisHandler.buildMessage(
                "chat-tag-delete",
                hashMapOf<String, String>().also {
                    it["uniqueId"] = chatTag.uniqueId.toString()
                }
            ).dispatch(
                PeachSpigotPlugin.instance.banana
            )

            sender.sendMessage("${CC.SEC}You've deleted the ${CC.WHITE}${chatTag.displayName}${CC.SEC} chat-tag.")
        }
    }

    @Subcommand("meta design")
    @Syntax("<chatTag> [design]")
    @CommandCompletion("@chattags")
    @Description("Edit a chat-tags design.")
    @CommandPermission("peach.command.cta.meta.edit")
    fun onMetaDesign(sender: CommandSender, chatTag: ChatTag, design: String)
    {
        chatTag.value = Color.translate(design)

        chatTag.saveAndPushUpdatesGlobally().thenAccept {
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

}
