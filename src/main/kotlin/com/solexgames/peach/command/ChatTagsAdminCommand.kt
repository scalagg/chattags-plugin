package com.solexgames.peach.command

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.handler.RedisHandler
import com.solexgames.peach.PeachSpigotPlugin
import com.solexgames.peach.model.ChatTag
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.CommandHelp
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.acf.bukkit.contexts.OnlinePlayer
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
import org.apache.commons.lang3.StringUtils
import org.bukkit.command.CommandSender
import java.util.*

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
@CommandAlias("peach|chattagadmin|cta")
@CommandPermission("peach.command.cta")
class ChatTagsAdminCommand : BaseCommand() {

    @Default
    @HelpCommand
    @Syntax("[help]")
    fun onHelp(help: CommandHelp) {
        help.showHelp()
    }

    @Subcommand("list")
    @Description("View all chat-tags.")
    fun onList(sender: CommandSender) {
        val chatTagList = PeachSpigotPlugin.instance.chatTagHandler.chatTags

        if (chatTagList.isEmpty()) {
            throw ConditionFailedException("There are no chat-tags.")
        }

        sender.sendMessage(arrayOf(
            "${CC.B_PRI}Available Chat Tags:",
            "${CC.SEC}${chatTagList.size}${CC.GRAY} chat-tags found.",
            ""
        ))

        chatTagList.forEach {
            sender.sendMessage("${CC.GRAY} - ${CC.WHITE}${it.value.displayName} ${CC.GRAY}(${it.value.value}${CC.GRAY})")
        }
    }

    @Subcommand("add")
    @Syntax("<player> <chatTag>")
    @CommandCompletion("@players @chattags")
    @Description("Give a player a chat-tag.")
    fun onAdd(sender: CommandSender, target: OnlinePlayer, chatTag: ChatTag) {
        val chatTagPlayer = PeachSpigotPlugin.instance.players[target.player.uniqueId]
            ?: throw ConditionFailedException("That player does not exist.")

        if (chatTagPlayer.ownedTags.contains(chatTag.uniqueId)) {
            throw ConditionFailedException("${CC.YELLOW}${target.player.name}${CC.RED} already owns the ${CC.YELLOW}${chatTag.displayName}${CC.RED} chat-tag.")
        }

        chatTagPlayer.ownedTags.add(chatTag.uniqueId)

        target.player.sendMessage("${CC.GREEN}You've been granted the ${CC.WHITE}${chatTag.displayName}${CC.GREEN} tag.")
        target.player.sendMessage("${CC.GRAY}Equip your new tag using /tags.")

        Lemon.instance.playerHandler.findPlayer(target.player.uniqueId).orElse(null)?.save()

        sender.sendMessage("${CC.SEC}You've granted ${CC.WHITE}${target.player.name}${CC.SEC} the ${CC.WHITE}${chatTag.displayName}${CC.SEC} chat-tag.")
    }

    @Subcommand("remove")
    @Syntax("<player> <chatTag>")
    @CommandCompletion("@players @chattags")
    @Description("Remove access to a chat-tag.")
    fun onRemove(sender: CommandSender, target: OnlinePlayer, chatTag: ChatTag) {
        val chatTagPlayer = PeachSpigotPlugin.instance.players[target.player.uniqueId]
            ?: throw ConditionFailedException("That player does not exist.")

        if (!chatTagPlayer.ownedTags.contains(chatTag.uniqueId)) {
            throw ConditionFailedException("${CC.YELLOW}${target.player.name}${CC.RED} does not own the ${CC.YELLOW}${chatTag.displayName}${CC.RED} chat-tag.")
        }

        chatTagPlayer.ownedTags.remove(chatTag.uniqueId)
        Lemon.instance.playerHandler.findPlayer(target.player.uniqueId).orElse(null)?.save()

        sender.sendMessage("${CC.SEC}You've removed ${CC.WHITE}${target.player.name}'s${CC.SEC} access to the ${CC.WHITE}${chatTag.displayName}${CC.SEC} chat-tag.")
    }

    @Syntax("<name>")
    @Subcommand("create")
    @Description("Create a new chat-tag.")
    @CommandPermission("peach.command.cta.management")
    fun onCreate(sender: CommandSender, @Single name: String) {
        val existing = PeachSpigotPlugin.instance.chatTagHandler.findChatTag(name)

        if (existing != null) {
            throw ConditionFailedException("A chat tag with the name matching ${CC.YELLOW}$name${CC.RED} already exists.")
        }

        if (name.length < 3) {
            throw ConditionFailedException("${CC.YELLOW}$name${CC.RED} must be at least 3 characters long.")
        }

        if (name.length > 16) {
            throw ConditionFailedException("${CC.YELLOW}$name${CC.RED} must be at most 16 characters long.")
        }

        if (!StringUtils.isAlphanumeric(name)) {
            throw ConditionFailedException("${CC.YELLOW}$name${CC.RED} must only contain alphanumeric characters.")
        }

        val chatTag = ChatTag(UUID.randomUUID(), name)

        chatTag.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've created the ${CC.WHITE}${chatTag.displayName}${CC.SEC} chat tag.")
            sender.sendMessage("${CC.GRAY}Use /peach meta design ${chatTag.value} <design> to set the tag design.")
        }
    }

    @Syntax("<chatTag>")
    @Subcommand("delete")
    @CommandCompletion("@chattags")
    @Description("Delete an existing chat-tag.")
    @CommandPermission("peach.command.cta.management")
    fun onDelete(sender: CommandSender, chatTag: ChatTag) {
        PeachSpigotPlugin.instance.mongoHandler.chatTagLayer.deleteEntry(chatTag.uniqueId.toString()).thenAccept {
            RedisHandler.buildMessage(
                "chat-tag-delete",
                hashMapOf<String, String>().also {
                    it["uniqueId"] = chatTag.uniqueId.toString()
                }
            ).publishAsync("peach")

            sender.sendMessage("${CC.SEC}You've deleted the ${CC.WHITE}${chatTag.displayName}${CC.SEC} rank.")
        }
    }

    @Subcommand("meta design")
    @Syntax("<chatTag> [design]")
    @CommandCompletion("@chattags")
    @Description("Edit a chat-tags design.")
    @CommandPermission("peach.command.cta.meta.edit")
    fun onMetaDesign(sender: CommandSender, chatTag: ChatTag, design: String) {
        chatTag.value = Color.translate(design)

        chatTag.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've updated ${CC.WHITE}${chatTag.displayName}'s${CC.SEC} design to ${chatTag.value}${CC.SEC}.")
        }
    }

}
