package moe.caa.fabric.hadesgame.stage

import moe.caa.fabric.hadesgame.access.LivingEntityAccess
import moe.caa.fabric.hadesgame.event.preDeathEvent
import moe.caa.fabric.hadesgame.handler.ScoreboardHandler
import moe.caa.fabric.hadesgame.util.*
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.world.GameMode
import java.awt.Color
import java.time.LocalDateTime

data object GamingStage : AbstractStage() {
    override val stageName: String = "游戏中"
    override val nextStage: AbstractStage = EndStage

    private var tick = 0
    private var invincibleCountdown = 0

    override fun init() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register { livingEntity, _, _ ->
            if (isCurrentRunStage()) {
                if (livingEntity is ServerPlayerEntity) {
                    return@register invincibleCountdown <= 0
                }
            }
            return@register true
        }

        preDeathEvent.register { livingEntity: LivingEntity, damageSource: DamageSource ->
            if (isCurrentRunStage() && livingEntity is ServerPlayerEntity) {
                (livingEntity as LivingEntityAccess).`hadesGame$callDrop`(livingEntity.world, damageSource)
                livingEntity.resetState()
                livingEntity.changeGameMode(GameMode.SPECTATOR)
                livingEntity.damageTracker.deathMessage.broadcast()

                return@register true
            }
            return@register false
        }
    }

    override suspend fun startStage() {
        tick = 0
        invincibleCountdown = 20

        for (player in getPlayers()) {
            player.resetState()
            player.changeGameMode(GameMode.SURVIVAL)
        }
    }

    override suspend fun tickStage() {
        tick++

        if (tick % 20 == 0) {
            invincibleCountdown--


            if (invincibleCountdown > 0) {
                ScoreboardHandler.updateContents(contents = buildList {
                    add(Text.literal(DATE_FORMAT.format(LocalDateTime.now())).withColor(Color.LIGHT_GRAY.rgb))
                    add(Text.literal(" "))
                    add(Text.literal(" 请做好准备, 无敌  "))
                    add(
                        Text.literal(" 时间还剩余")
                            .append(Text.literal(invincibleCountdown.toString()).withColor(Color.RED.rgb)).append("秒.")
                    )
                    add(Text.literal(" "))
                    add(Text.literal("(╯°□°)╯").withColor(Color.YELLOW.rgb))
                })
                Text.literal("无敌时间还剩余 ").withColor(Color.LIGHT_GRAY.rgb)
                    .append(Text.literal(invincibleCountdown.toString()).withColor(Color.RED.rgb))
                    .append(Text.literal(" 秒").withColor(Color.LIGHT_GRAY.rgb)).broadcastOverlay()
            } else if (invincibleCountdown == 0) {
                SoundEvents.GOAT_HORN_SOUNDS[2].value().broadcast(1000F, 1F)
                Text.literal("全军出击!").withColor(Color.RED.rgb).broadcastOverlay()

            } else {
                ScoreboardHandler.updateContents(contents = buildList {
                    add(Text.literal(DATE_FORMAT.format(LocalDateTime.now())).withColor(Color.LIGHT_GRAY.rgb))
                    add(Text.literal(" "))
                    add(Text.literal(" 剩下的还没做完  "))
                    add(Text.literal(" "))
                    add(Text.literal("(╯°□°)╯").withColor(Color.YELLOW.rgb))
                })
            }
        }
    }

    override suspend fun shouldEndStage(): Boolean {
        val playerEntities = getPlayers().filter { it.gameMode != GameMode.SPECTATOR }
        return playerEntities.size <= 1
    }
}