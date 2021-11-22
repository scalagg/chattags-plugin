package gg.scala.peach.result

import gg.scala.peach.model.ChatTag
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.PaginatedResult

/**
 * @author GrowlyX
 * @since 10/17/2021
 */
object ChatTagPaginatedResult : PaginatedResult<ChatTag>()
{
    override fun getHeader(page: Int, maxPages: Int): String
    {
        return "${CC.PRI}=== ${CC.SEC}Chat Tags ${CC.GRAY}($page/$maxPages)${CC.PRI} ==="
    }

    override fun format(result: ChatTag, resultIndex: Int): String
    {
        return "${CC.GRAY} - ${CC.WHITE}${result.displayName} ${CC.GRAY}(${result.value}${CC.GRAY})"
    }
}
