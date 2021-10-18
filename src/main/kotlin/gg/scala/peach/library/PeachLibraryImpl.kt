package gg.scala.peach.library

import gg.scala.commons.library.model.Library

/**
 * @author GrowlyX
 * @since 10/14/2021
 */
object PeachLibraryImpl
{

    @JvmStatic
    val KYORI_ADVENTURE = Library(
        "net.kyori", "adventure-api", "4.9.2"
    ).mavenCentral()

    @JvmStatic
    val KYORI_ADVENTURE_BUKKIT = Library(
        "net.kyori", "adventure-platform-bukkit", "4.0.0"
    ).mavenCentral()

    @JvmStatic
    val KYORI_ADVENTURE_MINI_MESSAGE = Library(
        "net.kyori", "adventure-text-minimessage", "4.1.0-SNAPSHOT"
    ).repository(
        "https://oss.sonatype.org/content/repositories/snapshots/"
    )

}
