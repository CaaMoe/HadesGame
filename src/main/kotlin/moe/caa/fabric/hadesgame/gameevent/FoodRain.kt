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
 * 食物雨事件
 * 天空中会随机掉落各种食物，为所有玩家提供补给机会
 * 增加区域控制和资源竞争的策略性
 */
data object FoodRainEvent : AbstractGameEvent() {
    override val eventName = "食物从天而降"

    override suspend fun callEvent() {
        val foods = listOf(
            Items.BREAD, Items.APPLE, Items.COOKED_BEEF, Items.COOKED_PORKCHOP,
            Items.COOKED_CHICKEN, Items.CARROT, Items.POTATO, Items.BEETROOT
        )
        
        for (player in getPlayers()) {
            val playerPos = player.pos
            
            // 在玩家周围掉落食物
            repeat(Random.nextInt(3, 7)) {
                val x = playerPos.x + Random.nextDouble(-10.0, 10.0)
                val y = playerPos.y + Random.nextDouble(5.0, 15.0)
                val z = playerPos.z + Random.nextDouble(-10.0, 10.0)
                
                val food = foods.random()
                val itemEntity = ItemEntity(player.world, x, y, z, food.defaultStack)
                player.world.spawnEntity(itemEntity)
            }
            
            Text.literal("天空中掉落了食物！快去收集吧！")
                .withColor(Color.ORANGE.rgb)
                .sendOverlay(player)
        }
        
        Text.literal("食物从天而降！")
            .withColor(Color.ORANGE.rgb)
            .broadcast()
        
        SoundEvents.ENTITY_CHICKEN_EGG.broadcast(1.0F, 0.5F)
    }
}
