package org.laolittle.plugin

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.utils.error
import net.mamoe.mirai.utils.info
import java.io.File

object RiskDetectCommand : CompositeCommand(
    RiskDetector, "risk",
    description = "风控检测"
) {
    private val logger by RiskDetector::logger

    @SubCommand("detect", "d")
    fun detect() {
        RiskDetector.task.run()
    }

    @SubCommand("relogin", "re")
    suspend fun CommandSender.reLogin(bot: Bot? = this.bot) {
        if (bot != null) {
            logger.info { "删除$bot 缓存, 结果为: ${File("bots/${bot.id}").resolve("cache").takeIf { it.isDirectory }?.deleteRecursively() ?: false}" }
            bot.login()
            logger.info { "$bot 已自动重新登录" }
        } else logger.error { "请输入Bot号码" }

    }
}