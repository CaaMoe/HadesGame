package moe.caa.fabric.hadesgame.game.event

import moe.caa.fabric.hadesgame.util.Message
import moe.caa.fabric.hadesgame.util.Sound
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import java.awt.Color

data object SuperMiners : AbstractSustainGameEvent() {
    override val name = "超级矿工"

    private val breakingPositions = mutableSetOf<Long>()

    init {
        PlayerBlockBreakEvents.AFTER.register { world, player, pos, _, _ ->
            if (!eventRunning) return@register


            val rootPosKey = pos.asLong()
            if (!breakingPositions.add(rootPosKey)) return@register

            val pendingKeys = mutableListOf<Long>()
            try {
                val radius = 2
                for (y in pos.y - radius..pos.y + radius) {
                    for (x in pos.x - radius..pos.x + radius) {
                        for (z in pos.z - radius..pos.z + radius) {
                            if (x == pos.x && y == pos.y && z == pos.z) continue

                            val targetPos = BlockPos(x, y, z)
                            val targetKey = targetPos.asLong()
                            if (breakingPositions.add(targetKey)) {
                                pendingKeys += targetKey
                            }
                            world.destroyBlock(targetPos, true, player)
                        }
                    }
                }
            } finally {
                breakingPositions.remove(rootPosKey)
                pendingKeys.forEach(breakingPositions::remove)
            }
        }
    }

    override val startMessage = Message(
        Component.literal("超级矿工效果已生效").withColor(Color.GREEN.rgb),
        Component.literal("超级矿工效果已生效").withColor(Color.GREEN.rgb),
        Sound(SoundEvents.VILLAGER_YES, 1F, 0.7F)
    )

    override val endMessage = Message(
        Component.literal("超级矿工效果已失效").withColor(Color.RED.rgb),
        Component.literal("超级矿工效果已失效").withColor(Color.RED.rgb),
        Sound(SoundEvents.VILLAGER_NO, 1F, 0.7F)
    )

    override fun eventStart() {

    }

    override fun eventEnd() {

    }
}