package gg.scala.chattags.plugin.conversion

/**
 * A model of an old Scandium `prefix`.
 *
 * @author GrowlyX
 * @since 10/24/2021
 */
data class ScalaLegacyPrefix(
    val _id: String,
    val name: String,
    val displayName: String,
    val prefix: String,
    val purchasable: Boolean
)
