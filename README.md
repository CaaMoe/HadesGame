# HadesGame（阴间游戏）

一个基于 Minecraft Fabric 服务器端的随机事件生存竞技模组, 玩家会被随机传送到地图中, 在不断缩圈与持续刷新的随机事件里互相战斗最后只留下
1 名幸存者.

---

## 编写事件

这个项目的事件注册方式非常简单, 只要在 `moe.caa.fabric.hadesgame.game.event` 包下定义一个单例事件对象 (需要间接或者直接继承
`AbstractGameEvent`), 系统就会自动发现并注册它.

### 1. 注册一个交换类型的事件

如果你想做一个"交换类型"的事件, 可以继承 `AbstractSwapTypeGameEvent<T>`:

```kotlin
package moe.caa.fabric.hadesgame.game.event

import moe.caa.fabric.hadesgame.util.Location
import moe.caa.fabric.hadesgame.util.getLocation
import moe.caa.fabric.hadesgame.util.teleport
import net.minecraft.server.level.ServerPlayer

data object ExampleSwapEvent : AbstractSwapTypeGameEvent<Location>() {
    override val name = "示例交换事件"

    override fun extract(source: ServerPlayer): Location = source.getLocation()

    override fun apply(target: ServerPlayer, data: Location) {
        target.teleport(data)
    }
}
```

### 2. 注册一个持续型事件

如果你的事件需要持续生效一段时间, 可以继承 `AbstractSustainGameEvent`:

```kotlin
package moe.caa.fabric.hadesgame.game.event

import moe.caa.fabric.hadesgame.game.GameCore

data object ExampleSlowEvent : AbstractSustainGameEvent() {
    override val name = "示例减速事件"
    override val keepTickNumber = 30 * 20

    override fun eventStart() {
        GameCore.server.tickRateManager().setTickRate(10F)
    }

    override fun eventEnd() {
        GameCore.server.tickRateManager().setTickRate(20F)
    }
}
```

### 3. 注册其他事件

如果你的事件既不是持续型的也不是交换型的, 也可以直接继承所有游戏事件的统一抽象基类 (`AbstractGameEvent`)来执行你的任何逻辑

### 4. 事件命名与推荐规范

开发事件时，建议遵循下面的规范：

- 事件名唯一且短而清晰
- 放置于 `moe.caa.fabric.hadesgame.game.event` 包
- 使用 `data object` 作为单例对象

