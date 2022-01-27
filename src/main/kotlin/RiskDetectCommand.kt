package org.laolittle.plugin

import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
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

    @OptIn(ConsoleExperimentalApi::class)
    @SubCommand("relogin", "re")
    suspend fun CommandSender.reLogin(bot: Bot? = this.bot) {
        if (bot != null) {
            bot.close()
            RiskDetector.logger.info {
                "删除$bot 缓存, 结果为: ${
                    File("bots/${bot.id}").resolve("cache").takeIf { it.isDirectory }
                        ?.deleteRecursively() ?: false
                }"
            }
            AutoLoginData.accounts.first { it.account == bot.id }.apply {
                if (password.kind == PasswordKind.MD5)
                    MiraiConsole.addBot(account, password.value) botConfig@{
                        protocol = configuration.protocol
                        fileBasedDeviceInfo(configuration.device)
                    }.alsoLogin()
            }
            RiskDetector.logger.info { "$bot 已自动重新登录" }
        } else logger.error { "请输入Bot号码" }

    }
}