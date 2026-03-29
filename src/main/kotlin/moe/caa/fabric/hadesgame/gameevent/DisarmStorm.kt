package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.broadcastOverlay
import moe.caa.fabric.hadesgame.util.getActivePlayers
import moe.caa.fabric.hadesgame.util.syncInventory
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import java.awt.Color

data object DisarmStorm : AbstractGameEvent() {
    override val eventName = "缴械风暴"

    override suspend fun callEvent() {
        var affectedPlayers = 0

        for (player in getActivePlayers()) {
            val droppedItem = player.mainHandItem.copy()
            if (droppedItem.isEmpty) continue

            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY)
            player.drop(droppedItem, false, true)
            player.syncInventory()
            affectedPlayers++
        }

        if (affectedPlayers == 0) {
            Component.literal("缴械风暴掠过了战场, 但无人中招").withColor(Color.LIGHT_GRAY.rgb).broadcastOverlay()
            return
        }

        Component.literal("缴械风暴已生效").withColor(Color.GREEN.rgb).broadcast()
        Component.literal("所有人的主手物品都被吹飞了").withColor(Color.ORANGE.rgb).broadcastOverlay()
        SoundEvents.ITEM_PICKUP.broadcast(1.0F, 0.4F)
    }
}
