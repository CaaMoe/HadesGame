package moe.caa.fabric.hadesgame.game.event

import moe.caa.fabric.hadesgame.util.Message
import moe.caa.fabric.hadesgame.util.Sound
import moe.caa.fabric.hadesgame.util.syncInventory
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.ItemStack
import java.awt.Color

data object SwapInventory : AbstractSwapTypeGameEvent<List<ItemStack>>() {
    override val name = "交换背包"

    override fun extract(source: ServerPlayer) = buildList(source.inventory.containerSize) {
        for (index in 0 until source.inventory.containerSize) {
            add(source.inventory.getItem(index).copy())
        }
    }

    override fun apply(target: ServerPlayer, data: List<ItemStack>) {
        for ((index, stack) in data.withIndex()) {
            target.inventory.setItem(index, stack)
        }

        target.syncInventory()
    }

    override fun buildSwapedMessage(
        receiver: ServerPlayer,
        swapedTarget: ServerPlayer
    ): Message {
        val message = Component.empty()
            .append("你与 ").withColor(Color.LIGHT_GRAY.rgb)
            .append(Component.literal(swapedTarget.nameAndId().name).withColor(0XFFD700))
            .append(" 互换了背包内容.").withColor(Color.LIGHT_GRAY.rgb)

        return Message(
            message,
            message,
            Sound(SoundEvents.ENDER_CHEST_CLOSE, 1F, 0.7F)
        )
    }

    override fun buildNoGoalSwapMessage(receiver: ServerPlayer): Message {
        val message = Component.empty()
            .append("你没有与任何玩家互换背包内容.").withColor(Color.LIGHT_GRAY.rgb)

        return Message(
            message, message,
            Sound(SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), 1F, 0.8F)
        )
    }

}