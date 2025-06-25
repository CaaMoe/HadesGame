package moe.caa.fabric.hadesgame.util

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.asDeferred
import moe.caa.fabric.hadesgame.GameCore
import net.minecraft.block.Blocks
import net.minecraft.block.FluidBlock
import net.minecraft.entity.Entity
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.server.world.ChunkTicketType
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.Heightmap
import kotlin.math.max

fun Entity.getLocation() = Location(
    this.world as ServerWorld,
    this.x, this.y, this.z,
    this.yaw, this.pitch
)

fun Entity.teleport(location: Location) = teleport(
    location.world,
    location.x,
    location.y,
    location.z,
    emptySet(),
    location.yaw,
    location.pitch,
    true
)


data class Location(
    var world: ServerWorld,
    var x: Double,
    var y: Double,
    var z: Double,
    var yaw: Float = 0.0.toFloat(),
    var pitch: Float = 0.0.toFloat(),
)

val randomLocationChunkTicketType: ChunkTicketType = Registry.register(
    Registries.TICKET_TYPE,
    "hadesgame:random_location",
    ChunkTicketType(300, false, ChunkTicketType.Use.LOADING)
)

tailrec suspend fun ServerWorld.randomLobbySpawnLocation(): Location {
    val posX = (-20000000 + Math.random() * 40000000).toInt()
    val posZ = (-20000000 + Math.random() * 40000000).toInt()

    val chunkPos = ChunkPos(posX shr 4, posZ shr 4)
    chunkManager.addTicket(randomLocationChunkTicketType, chunkPos, 0)

    val worldChunk = GameCore.coroutineScope.async {
        repeat(10) {
            val chunk = chunkManager.chunkLoadingManager
                .getCurrentChunkHolder(chunkPos.toLong())
                ?.accessibleFuture?.asDeferred()?.await()?.orElse(null)
            if (chunk != null) return@async chunk
            delay(50)
        }
        return@async null
    }.await()
    if (worldChunk == null) {
        chunkManager.removeTicket(randomLocationChunkTicketType, chunkPos, 0)
        return randomLobbySpawnLocation()
    }

    val posY = getTopY(Heightmap.Type.MOTION_BLOCKING, posX, posZ)
    val block = getBlockState(BlockPos(posX, posY - 1, posZ))

    if (block.block is FluidBlock) {
        return randomLobbySpawnLocation()
    }
    if (block.block == Blocks.AIR) {
        return randomLobbySpawnLocation()
    }

    var platformMaxY = 0
    for (x in -10..10) {
        for (z in -10..10) {
            platformMaxY = max(platformMaxY, getTopY(Heightmap.Type.MOTION_BLOCKING, x, z))
        }
    }

    if (platformMaxY + 20 > height) {
        return randomLobbySpawnLocation()
    }

    return Location(this, posX + 0.5, posY + 10.0, posZ + 0.5)
}