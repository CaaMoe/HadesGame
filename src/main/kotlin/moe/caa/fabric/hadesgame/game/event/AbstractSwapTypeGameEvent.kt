package moe.caa.fabric.hadesgame.game.event

import moe.caa.fabric.hadesgame.game.GameCore
import moe.caa.fabric.hadesgame.util.EMPTY_MESSAGE
import moe.caa.fabric.hadesgame.util.Message
import moe.caa.fabric.hadesgame.util.getActivePlayers
import moe.caa.fabric.hadesgame.util.sendTo
import net.minecraft.server.level.ServerPlayer

/**
 * 交换型事件(一次性事件)的抽象基类
 */
sealed class AbstractSwapTypeGameEvent<T> : AbstractGameEvent() {


    override fun call() {
        val sources = getActivePlayers().shuffled()

        if (sources.isEmpty()) {
            return
        }

        GameCore.logger.info("事件 $name 已触发.")

        for (index in sources.indices step 2) {
            val p0 = sources[index]
            if (sources.size > index + 1) {
                val p1 = sources[index + 1]

                val t0 = extract(p0)
                val t1 = extract(p1)

                apply(p1, t0)
                apply(p0, t1)

                buildSwapedMessage(p0, p1).also { it.sendTo(p0) }
                buildSwapedMessage(p1, p0).also { it.sendTo(p1) }

                GameCore.logger.info("事件 $name 已将 ${p0.nameAndId().name} 与 ${p1.nameAndId().name} 的数据对调.")
            } else {
                buildNoGoalSwapMessage(p0).also { it.sendTo(p0) }
                GameCore.logger.info("由于存活玩家为奇数, 事件 $name 将忽略 ${p0.nameAndId().name}")
            }
        }
    }

    /**
     * 从玩家身上提取要交换的数据.
     */
    abstract fun extract(source: ServerPlayer): T

    /**
     * 将提取到的数据应用到目标玩家上.
     */
    abstract fun apply(target: ServerPlayer, data: T)

    /**
     * 在成功互换后，为接收者构建的消息提示.
     */
    open fun buildSwapedMessage(receiver: ServerPlayer, swapedTarget: ServerPlayer): Message = EMPTY_MESSAGE

    /**
     * 当玩家因总人数为奇数没有配对对象时为接收者构建的消息提示.
     */
    open fun buildNoGoalSwapMessage(receiver: ServerPlayer): Message = EMPTY_MESSAGE
}