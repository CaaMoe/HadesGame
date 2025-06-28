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
 * 随机书页事件
 * 掉落带有随机附魔的书籍和铁砧，同时给予大量经验
 * 让玩家可以立即使用附魔书改造装备
 */
data object RandomBookDropEvent : AbstractGameEvent() {
    override val eventName = "知识之雨"

    override suspend fun callEvent() {
        val enchantedBooks = listOf(
            Items.ENCHANTED_BOOK,
            Items.BOOK,
            Items.WRITABLE_BOOK
        )
        
        for (player in getPlayers()) {
            val playerPos = player.pos
            
            // 给予大量经验用于附魔
            player.addExperience(Random.nextInt(30, 60))
            
            // 在每个玩家周围掉落2-4本书
            repeat(Random.nextInt(2, 5)) {
                val x = playerPos.x + Random.nextDouble(-5.0, 5.0)
                val y = playerPos.y + Random.nextDouble(3.0, 8.0)
                val z = playerPos.z + Random.nextDouble(-5.0, 5.0)
                
                val book = enchantedBooks.random()
                val itemEntity = ItemEntity(player.world, x, y, z, book.defaultStack)
                player.world.spawnEntity(itemEntity)
            }
            
            // 在玩家附近掉落一个铁砧
            val anvilX = playerPos.x + Random.nextDouble(-3.0, 3.0)
            val anvilY = playerPos.y + Random.nextDouble(3.0, 6.0)
            val anvilZ = playerPos.z + Random.nextDouble(-3.0, 3.0)
            
            val anvilEntity = ItemEntity(player.world, anvilX, anvilY, anvilZ, Items.ANVIL.defaultStack)
            player.world.spawnEntity(anvilEntity)
            
            Text.literal("知识与工具从天而降！附魔书、铁砧和经验都已准备好")
                .withColor(Color.BLUE.rgb)
                .sendOverlay(player)
        }
        
        Text.literal("附魔书籍、铁砧和经验都掉落了！快来打造你的装备吧！")
            .withColor(Color.BLUE.rgb)
            .broadcast()
        
        SoundEvents.ITEM_BOOK_PAGE_TURN.broadcast(1.0F, 1.0F)
    }
}
