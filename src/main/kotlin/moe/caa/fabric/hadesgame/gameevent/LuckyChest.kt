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
 * 幸运宝箱事件
 * 随机掉落包含稀有物品的宝箱，但有陷阱风险
 * 增加风险收益的决策制定
 */
data object LuckyChestEvent : AbstractGameEvent() {
    override val eventName = "幸运宝箱"

    override suspend fun callEvent() {
        val players = getPlayers()
        if (players.isEmpty()) return
        
        val luckyItems = listOf(
            Items.DIAMOND_SWORD,
            Items.DIAMOND_PICKAXE,
            Items.ENCHANTED_GOLDEN_APPLE,
            Items.TOTEM_OF_UNDYING,
            Items.ELYTRA,
            Items.BOW,
            Items.CROSSBOW
        )
        
        val trapItems = listOf(
            Items.TNT,
            Items.POISONOUS_POTATO,
            Items.SPIDER_EYE
        )
        
        // 在地图上生成2-4个宝箱
        repeat(Random.nextInt(2, 5)) {
            val randomPlayer = players.random()
            val chestX = randomPlayer.x + Random.nextDouble(-25.0, 25.0)
            val chestY = randomPlayer.y + Random.nextDouble(5.0, 10.0)
            val chestZ = randomPlayer.z + Random.nextDouble(-25.0, 25.0)
            
            // 70%概率是好物品，30%是陷阱
            val items = if (Random.nextDouble() < 0.7) {
                listOf(luckyItems.random())
            } else {
                listOf(trapItems.random())
            }
            
            items.forEach { item ->
                val itemEntity = ItemEntity(randomPlayer.world, chestX, chestY, chestZ, item.defaultStack)
                randomPlayer.world.spawnEntity(itemEntity)
            }
            
            // 生成宝箱外观
            val chestEntity = ItemEntity(randomPlayer.world, chestX, chestY, chestZ, Items.CHEST.defaultStack)
            randomPlayer.world.spawnEntity(chestEntity)
        }
        
        for (player in players) {
            Text.literal("幸运宝箱出现了！但要小心陷阱...")
                .withColor(Color.GOLD.rgb)
                .sendOverlay(player)
        }
        
        Text.literal("幸运宝箱已投放！")
            .withColor(Color.GOLD.rgb)
            .broadcast()
        
        SoundEvents.BLOCK_CHEST_OPEN.broadcast(1.0F, 0.8F)
    }
}
