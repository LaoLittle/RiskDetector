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
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.utils.error
import net.mamoe.mirai.utils.info
import java.io.File
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.system.measureTimeMillis

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
    val task by lazy {
        timerTask {
            runBlocking {
                bots.forEach { bot ->
                    retryWhenFailed(bot.groups.size) {
                        val receipt: MessageReceipt<Group>
                        val cost = measureTimeMillis {
                            receipt =
                                (bot.getGroup(RiskDetectorConfig.groupId) ?: bot.groups.random()).sendMessage("风控检测")
                        } - ping()
                        runCatching { receipt.recall() }
                        cost
                    }?.run {
                        if (this >= RiskDetectorConfig.duration) {
                            logger.info { "$bot 疑似被风控" }
                            bot.close()
                            logger.info {
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
                            logger.info { "$bot 已自动重新登录" }
                        } else logger.info { "$bot 通过检测" }
                    } ?: logger.error { "检测失败, 无法找到可以发送消息的群" }
                }
            }
        }
    }

    override fun onEnable() {
        AutoLoginData.reload()
        println(AutoLoginData.accounts)
        RiskDetectorConfig.reload()
        RiskDetectCommand.register()
        logger.info { "Plugin loaded" }
        globalEventChannel().subscribeAlways<BotOnlineEvent> {
            bots.add(bot)
        }
        globalEventChannel().subscribeAlways<BotOfflineEvent> {
            bots.remove(bot)
        }
        Timer().schedule(task, Date(), RiskDetectorConfig.interval * 60 * 1000)
    }

    init {
        PluginManager.pluginsConfigFolder.resolve("Console").resolve("AutoLogin.yml").readBytes().apply {
            dataFolder.resolve("AutoLogin.yml").writeBytes(this)
        }
    }
}

