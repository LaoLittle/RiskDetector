# RiskDetector 风控检测

这是一个为mirai-console定制的风控检测插件

他会每隔一段时间检测风控，并在疑似遇到风控时自动尝试解决

---

### 本插件已适配多Bot

---

配置文件:

```yaml
# 风控测试群号
# 机器人会将风控测试消息发至此群 (未找到则会另外随机选择)
groupId: 123456
# 检测间隔 (单位: 分)
interval: 20
```
本插件的原理: 发送一条消息并撤回，若被风控则消息未发出，撤回失败进行一次清理缓存并重新登录的操作

注意: 本插件并不能解决如封号等的实际性问题, 也不保证一定会解决风控
