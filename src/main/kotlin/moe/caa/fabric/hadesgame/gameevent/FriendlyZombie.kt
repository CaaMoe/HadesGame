package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.getPlayers
import moe.caa.fabric.hadesgame.util.sendOverlay
import net.minecraft.entity.Entity
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import java.awt.Color
import kotlin.random.Random

/**
 * 友善僵尸事件
 * 在每个玩家附近生成一个友善的僵尸作为临时保镖
 * 增加了环境变化但不直接给予玩家优势物品
 */
data object FriendlyZombieEvent : AbstractGameEvent() {
    override val eventName = "僵尸保镖"
    private val spawnedZombies = mutableListOf<Entity>()

    override suspend fun callEvent() {
        for (player in getPlayers()) {
            val playerPos = player.pos
            val zombie = ZombieEntity(player.world)
            
            // 设置僵尸位置
            zombie.setPosition(
                playerPos.x + Random.nextDouble(-3.0, 3.0),
                playerPos.y,
                playerPos.z + Random.nextDouble(-3.0, 3.0)
            )
            
            // 设置僵尸为友善
            zombie.setCustomName(Text.literal("${player.name.literalString}的保镖"))
            zombie.isCustomNameVisible = true
            zombie.isPersistent = true
            
            player.world.spawnEntity(zombie)
            spawnedZombies.add(zombie)
            
            Text.literal("一个友善的僵尸成为了你的临时保镖！")
                .withColor(Color.DARK_GREEN.rgb)
                .sendOverlay(player)
        }
        
        Text.literal("每个人都有了僵尸保镖！")
            .withColor(Color.DARK_GREEN.rgb)
            .broadcast()
        
        SoundEvents.ENTITY_ZOMBIE_AMBIENT.broadcast(1.0F, 1.2F)
    }

    override suspend fun endEvent() {
        // 清理生成的僵尸
        for (zombie in spawnedZombies) {
            if (!zombie.isRemoved) {
                zombie.remove(Entity.RemovalReason.DISCARDED)
            }
        }
        spawnedZombies.clear()
    }
}
