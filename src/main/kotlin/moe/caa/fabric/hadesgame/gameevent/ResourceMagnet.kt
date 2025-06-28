package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.getPlayers
import moe.caa.fabric.hadesgame.util.sendOverlay
import net.minecraft.entity.ItemEntity
import net.minecraft.item.Items
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import java.awt.Color
import kotlin.random.Random

/**
 * 资源磁场事件
 * 在地图中心生成一个资源点，吸引所有玩家前往
 * 创造集中冲突区域，增加直接对抗的机会
 */
data object ResourceMagnetEvent : AbstractGameEvent() {
    override val eventName = "资源磁场"

    override suspend fun callEvent() {
        val players = getPlayers()
        if (players.isEmpty()) return
        
        // 计算地图中心（所有玩家的平均位置）
        var centerX = 0.0
        var centerZ = 0.0
        
        for (player in players) {
            centerX += player.x
            centerZ += player.z
        }
        
        centerX /= players.size
        centerZ /= players.size
        
        val centerY = players.first().world.getTopY(centerX.toInt(), centerZ.toInt()).toDouble()
        
        // 在中心投放大量资源
        val valuableItems = listOf(
            Items.IRON_INGOT,
            Items.GOLD_INGOT,
            Items.DIAMOND,
            Items.EMERALD,
            Items.IRON_SWORD,
            Items.IRON_PICKAXE,
            Items.COOKED_BEEF,
            Items.GOLDEN_APPLE
        )
        
        repeat(Random.nextInt(8, 15)) {
            val item = valuableItems.random()
            val offsetX = Random.nextDouble(-3.0, 3.0)
            val offsetZ = Random.nextDouble(-3.0, 3.0)
            
            val itemEntity = ItemEntity(
                players.first().world,
                centerX + offsetX,
                centerY + 5.0,
                centerZ + offsetZ,
                item.defaultStack
            )
            players.first().world.spawnEntity(itemEntity)
        }
        
        for (player in players) {
            Text.literal("资源磁场出现在 (${centerX.toInt()}, ${centerZ.toInt()})！")
                .withColor(Color.GOLD.rgb)
                .sendOverlay(player)
        }
        
        Text.literal("珍贵资源聚集在地图中心！")
            .withColor(Color.GOLD.rgb)
            .broadcast()
        
        SoundEvents.BLOCK_BEACON_ACTIVATE.broadcast(1.0F, 0.8F)
    }
}
