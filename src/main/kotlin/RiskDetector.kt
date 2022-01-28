package org.laolittle.plugin

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.utils.error
import net.mamoe.mirai.utils.info
import java.io.File
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
    private val bots = mutableSetOf<Bot>()

    @OptIn(ExperimentalSerializationApi::class, ConsoleExperimentalApi::class)
    val task = timerTask {
        runBlocking {
            bots.forEach { bot ->
                retryWhenFailed(bot.groups.size) {
                    val receipt =
                        (bot.getGroup(RiskDetectorConfig.groupId) ?: bot.groups.random()).sendMessage("风控检测")

                    runCatching { receipt.recall() }.onSuccess { logger.info { "$bot 通过检测" } }.onFailure {
                        logger.info { "$bot 疑似被风控" }
                        mutableSetOf<Long>().apply {
                            AutoLoginData.accounts.forEach {
                                add(it.account)
                            }
                            if (bot.id !in this) {
                                logger.error { "自动登录配置中未找到$bot, 将不会重新登录" }
                                return@runBlocking
                            }
                        }
                        bot.close()
                        logger.info {
                            "删除$bot 缓存, 结果为: ${
                                File("bots/${bot.id}").resolve("cache").deleteRecursively()
                            }"
                        }
                        AutoLoginData.accounts.first { it.account == bot.id }.apply {
                            if (password.kind == PasswordKind.PLAIN)
                                MiraiConsole.addBot(account, password.value) botConfig@{
                                    protocol = configuration.protocol
                                    fileBasedDeviceInfo(configuration.device)
                                }.alsoLogin()
                            else logger.error { "暂不支持非PLAIN!" }
                        }
                        logger.info { "$bot 已自动重新登录" }

                    }
                } ?: logger.error { "检测失败, 无法找到可以发送消息的群" }
            }
        }
    }

    override fun onEnable() {
        AutoLoginData.reload()
        RiskDetectorConfig.reload()
        RiskDetectCommand.register()
        logger.info { "Plugin loaded" }
        globalEventChannel().subscribeAlways<BotOnlineEvent> {
            bots.add(bot)
        }
        globalEventChannel().subscribeAlways<BotOfflineEvent> {
            bots.remove(bot)
        }
        if (RiskDetectorConfig.interval > 0)
            Timer().schedule(task, Date(), RiskDetectorConfig.interval * 60 * 1000)
    }

    init {
        PluginManager.pluginsConfigFolder.resolve("Console").resolve("AutoLogin.yml").readBytes().apply {
            dataFolder.resolve("AutoLogin.yml").writeBytes(this)
        }
    }
}