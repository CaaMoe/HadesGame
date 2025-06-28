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
 * 工具箱掉落事件
 * 在地图上随机生成工具箱，包含基础工具和材料
 * 所有玩家都有机会获得，增加地图控制的重要性
 */
data object ToolboxDropEvent : AbstractGameEvent() {
    override val eventName = "工具箱空投"

    override suspend fun callEvent() {
        val players = getPlayers()
        if (players.isEmpty()) return
        
        // 在地图上生成3-5个工具箱
        repeat(Random.nextInt(3, 6)) {
            val randomPlayer = players.random()
            val dropX = randomPlayer.x + Random.nextDouble(-20.0, 20.0)
            val dropY = randomPlayer.y + Random.nextDouble(10.0, 20.0)
            val dropZ = randomPlayer.z + Random.nextDouble(-20.0, 20.0)
            
            // 工具箱内容
            val toolboxItems = listOf(
                Items.WOODEN_SWORD,
                Items.WOODEN_PICKAXE,
                Items.WOODEN_AXE,
                Items.TORCH,
                Items.COBBLESTONE
            )
            
            toolboxItems.forEach { item ->
                val itemEntity = ItemEntity(randomPlayer.world, dropX, dropY, dropZ, item.defaultStack)
                randomPlayer.world.spawnEntity(itemEntity)
            }
        }
        
        for (player in getPlayers()) {
            Text.literal("工具箱从天而降！快去寻找它们！")
                .withColor(Color.GOLD.rgb)
                .sendOverlay(player)
        }
        
        Text.literal("工具箱已空投到战场上！")
            .withColor(Color.GOLD.rgb)
            .broadcast()
        
        SoundEvents.ENTITY_ITEM_PICKUP.broadcast(1.0F, 0.5F)
    }
}
