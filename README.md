# HadesGame(阴间游戏)

一个基于 Fabric 的 Minecraft 多人大逃杀游戏模组(Server Only)。

## 项目概述  

HadesGame 是一个 Minecraft Fabric Server 模组, 旨在提供一个多人在线大逃杀的游戏体验.
玩家将被传送到一个随机生成的地图上, 必须在有限的时间内收集资源, 利用各种事件机制击败其他玩家并成为最后的幸存者.

## 注册一个游戏事件
要注册一个游戏事件，你需要在 `moe.caa.fabric.hadesgame.gameevent` 包下编写 `AbstractGameEvent` 的 数据单例子类, 并处理你的游戏事件逻辑.

```kotlin
data object ExampleEvent : AbstractGameEvent() {
    override val eventName = "你的事件名称"

    override fun initEvent() { /** 在这里编写事件初始化逻辑 */ }
    override suspend fun callEvent() { /** 在这里编写触发事件时的逻辑 */ }
    override suspend fun endEvent() { /** 在这里编写事件结束时的逻辑 */ }
}
```