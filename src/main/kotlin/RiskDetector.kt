package org.laolittle.plugin

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.BuiltInCommands
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.error
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.warning
import java.util.*
import kotlin.concurrent.timerTask

object RiskDetector : KotlinPlugin(
    JvmPluginDescription(
        id = "org.laolittle.plugin.RiskDetector",
        name = "RiskDetector",
        version = "1.0",
    ) {
        author("LaoLittle")
    }
) {

    @OptIn(ExperimentalSerializationApi::class, ConsoleExperimentalApi::class)
    val task = timerTask {
        runBlocking(coroutineContext) {
            for (bot in Bot.instances) {
                if (!bot.isOnline) continue

                retryWhenFailed(bot.groups.size) {
                    val receipt =
                        (bot.getGroup(RiskDetectorConfig.groupId) ?: bot.groups.random()).sendMessage("风控检测")

                    runCatching { receipt.recall() }.onSuccess { logger.info { "$bot 通过检测" } }.onFailure {
                        logger.info { "$bot 疑似被风控" }
                        bot.close()
                        logger.info {
                            "删除$bot 缓存, 结果为: ${bot.configuration.cacheDir.deleteRecursively()}"
                        }

                        with(ConsoleCommandSender) {
                            runCatching {
                                with(BuiltInCommands.LoginCommand) {
                                    handle(id = bot.id)
                                }
                                logger.info { "$bot 已自动重新登录" }
                            }.onFailure {
                                logger.warning { "$bot 重新登陆失败" }
                            }
                        }
                    }
                } ?: logger.error { "检测失败, 无法找到可以发送消息的群" }
            }
        }
    }

    override fun onEnable() {
        RiskDetectorConfig.reload()
        RiskDetectCommand.register()

        if (RiskDetectorConfig.interval > 0) {
            Timer().schedule(task, Date(), RiskDetectorConfig.interval * 60 * 1000)
        }

        logger.info { "Plugin loaded" }
    }
}