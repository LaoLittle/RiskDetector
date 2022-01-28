package org.laolittle.plugin

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.BuiltInCommands
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.error
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.warning

object RiskDetectCommand : CompositeCommand(
    RiskDetector, "risk",
    description = "风控检测"
) {
    private val logger by RiskDetector::logger

    @SubCommand("detect", "d")
    fun detect() {
        RiskDetector.task.run()
    }

    @OptIn(ConsoleExperimentalApi::class)
    @SubCommand("relogin", "re")
    suspend fun CommandSender.reLogin(bot: Bot? = this.bot) {
        if (bot == null) {
            logger.error { "请输入Bot号码" }
            return
        }

        bot.close()
        logger.info {
            "删除$bot 缓存, 结果为: ${bot.configuration.cacheDir.deleteRecursively()}"
        }
        try {
            with(BuiltInCommands.LoginCommand) {
                handle(id = bot.id)
            }
            logger.info { "$bot 已自动重新登录" }
        } catch (cause: Throwable) {
            logger.warning { "$bot 重新登陆失败" }
        }
    }
}