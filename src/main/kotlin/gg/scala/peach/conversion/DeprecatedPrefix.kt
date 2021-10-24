package gg.scala.peach.conversion

/**
 * A model of an old Scandium `prefix`.
 *
 * @author GrowlyX
 * @since 10/24/2021
 */
data class DeprecatedPrefix(
    val _id: String,
    val name: String,
    val displayName: String,
    val prefix: String,
    val purchasable: Boolean
)
