package org.laolittle.plugin

import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object RiskDetectorConfig : ReadOnlyPluginConfig("Config") {
    @ValueDescription("风控测试群号")
    val groupId by value(123456L)

    @ValueDescription("检测间隔 (单位: 分)")
    val interval by value(0L)
}