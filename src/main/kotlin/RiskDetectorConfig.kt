package org.laolittle.plugin

import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object RiskDetectorConfig : ReadOnlyPluginConfig("Config") {
    @ValueDescription("风控测试群号")
    val groupId by value(123456L)

    @ValueDescription("检测间隔 (单位: 分)")
    val interval by value(20L)

    @ValueDescription("风控消息时间阈值 (单位: 毫秒)")
    val duration by value(2000L)
}