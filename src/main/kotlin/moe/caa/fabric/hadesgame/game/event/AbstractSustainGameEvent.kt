package moe.caa.fabric.hadesgame.game.event

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import moe.caa.fabric.hadesgame.game.GameCore
import moe.caa.fabric.hadesgame.util.EMPTY_MESSAGE
import moe.caa.fabric.hadesgame.util.broadcast
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.bossevents.CustomBossEvent
import net.minecraft.util.RandomSource
import net.minecraft.world.BossEvent
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

/**
 * 持续型事件的抽象基类
 */
sealed class AbstractSustainGameEvent : AbstractGameEvent() {
    /**
     * 当前事件还剩多少 tick.
     */
    var remainTicks = 0
        private set

    /**
     * 当前事件是否正在生效中.
     */
    var eventRunning = false
        private set

    private val bossBarIdentifier = Identifier.fromNamespaceAndPath("hadesgame", javaClass.simpleName.lowercase())
    private var bossBar: CustomBossEvent? = null

    open val mutualExclusions = emptyList<AbstractSustainGameEvent>()

    /**
     * BossBar 的颜色.
     */
    open val bossBarColor = BossEvent.BossBarColor.entries.random()

    /**
     * 事件总持续时长.
     */
    open val keepTickNumber = 30 * 20

    /**
     * 事件开始时广播的消息.
     */
    open val startMessage = EMPTY_MESSAGE

    /**
     * 事件结束时广播的消息.
     */
    open val endMessage = EMPTY_MESSAGE

    /**
     * 事件 tick
     */
    private var tickJob: Job? = null

    final override fun call() {
        shouldStart()
    }

    private fun tick() {
        bossBar?.apply {
            this.progress = remainTicks / keepTickNumber.toFloat()
        }

        if (--remainTicks == 0) {
            shouldEnd()
        }
    }

    override fun gameStart() {
        tickJob = GameCore.coroutineScope.launch {
            while (isActive) {
                val startTimeMills = System.currentTimeMillis()
                tick()
                val endTimeMills = System.currentTimeMillis()
                val keepTimeMills = endTimeMills - startTimeMills
                delay(max(0, 50 - keepTimeMills).milliseconds)
            }
        }
    }

    override fun gameStop() {
        shouldEnd()
        tickJob?.cancel()
    }

    private fun shouldEnd() {
        remainTicks = -1
        if (!eventRunning) return
        eventRunning = false

        bossBar?.apply {
            GameCore.server.customBossEvents.remove(this)
            this.removeAllPlayers()
            bossBar = null
        }
        eventEnd()
        endMessage.broadcast()
        GameCore.logger.info("事件 $name 已结束.")
    }

    private fun shouldStart() {
        remainTicks = keepTickNumber
        if (eventRunning) return
        eventRunning = true

        bossBar = GameCore.server.customBossEvents.create(
            RandomSource.create(),
            bossBarIdentifier,
            Component.literal(name)
        ).apply {
            this.color = bossBarColor

            GameCore.server.playerList.players.forEach {
                this.addPlayer(it)
            }
        }

        mutualExclusions.forEach { it.shouldEnd() }

        eventStart()
        startMessage.broadcast()
        GameCore.logger.info("事件 $name 已激活, 持续时间 $remainTicks ticks.")
    }

    /**
     * 持续型事件生效逻辑
     */
    open fun eventStart() {}

    /**
     * 持续型事件失效逻辑
     */
    open fun eventEnd() {}
}