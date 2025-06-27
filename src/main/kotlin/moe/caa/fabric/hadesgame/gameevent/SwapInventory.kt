package moe.caa.fabric.hadesgame.gameevent

import moe.caa.fabric.hadesgame.util.broadcast
import moe.caa.fabric.hadesgame.util.getPlayers
import moe.caa.fabric.hadesgame.util.sendOverlay
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.world.GameMode
import java.awt.Color

data object SwapInventory : AbstractGameEvent() {
    override val eventName = "交换背包"

    override suspend fun callEvent() {
        val players = getPlayers()
            .filter { it.interactionManager.gameMode != GameMode.SPECTATOR }
            .toMutableList()
            .apply { shuffle() }

        val list = players.map { it.name to it.getInventoryData() }.toMutableList().apply {
            add(removeFirst())
        }

        players.forEach {
            val (name, inventoryData) = list.removeFirst()
            inventoryData.applyTo(it)
            Text.literal("你已应用 ").withColor(Color.LIGHT_GRAY.rgb)
                .append(Text.literal(name.literalString).withColor(Color.WHITE.rgb)).append(" 的背包").sendOverlay(it)
        }
        SoundEvents.ENTITY_FOX_TELEPORT.broadcast(100F, 0F)
    }

    data class InventoryData(
        val itemStacks: List<ItemStack>
    )

    private fun ServerPlayerEntity.getInventoryData(): InventoryData {
        val itemStacks = ArrayList<ItemStack>(inventory.size())
        for (i in 0 until inventory.size()) {
            itemStacks.add(inventory.getStack(i).copy())
        }

        return InventoryData(itemStacks)
    }

    private fun InventoryData.applyTo(player: ServerPlayerEntity) {
        for (i in itemStacks.indices) {
            player.inventory.setStack(i, itemStacks[i])
        }
        player.inventory.updateItems()
    }
}