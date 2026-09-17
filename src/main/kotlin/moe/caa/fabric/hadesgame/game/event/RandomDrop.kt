package moe.caa.fabric.hadesgame.game.event

import moe.caa.fabric.hadesgame.event.addEntityEvent
import moe.caa.fabric.hadesgame.util.Message
import moe.caa.fabric.hadesgame.util.Sound
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import java.awt.Color
import kotlin.jvm.optionals.getOrNull

data object RandomDrop : AbstractSustainGameEvent() {
    override val name = "随机掉落"


    init {
        addEntityEvent.register {
            if (!eventRunning) return@register

            if (it is ItemEntity) {
                val randomItem = BuiltInRegistries.ITEM.getRandom(it.random).getOrNull() ?: return@register
                it.item = ItemStack(randomItem)
            }
        }
    }

    override val startMessage = Message(
        Component.literal("随机掉落效果已生效").withColor(Color.GREEN.rgb),
        Component.literal("随机掉落效果已生效").withColor(Color.GREEN.rgb),
        Sound(SoundEvents.ITEM_PICKUP, 1F, 1F)
    )

    override val endMessage = Message(
        Component.literal("随机掉落效果已失效").withColor(Color.RED.rgb),
        Component.literal("随机掉落效果已失效").withColor(Color.RED.rgb),
        Sound(SoundEvents.ITEM_PICKUP, 1F, 0F)
    )
}