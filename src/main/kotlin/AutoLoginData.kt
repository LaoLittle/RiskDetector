package org.laolittle.plugin

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.ReadOnlyPluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.utils.BotConfiguration as MiraiBotConfig

object AutoLoginData : ReadOnlyPluginData("AutoLogin") {
    val accounts by value(mutableListOf<BotConfiguration>())
}

enum class PasswordKind {
    PLAIN,
    MD5,
}

@Serializable
data class BotConfiguration(
    val account: Long,
    val password: Password,
    val configuration: Configuration
)

@Serializable
data class Password(
    val kind: PasswordKind,
    val value: String
)

@Serializable
data class Configuration(
    val protocol: MiraiBotConfig.MiraiProtocol,
    val device: String,
    val enable: Boolean
)