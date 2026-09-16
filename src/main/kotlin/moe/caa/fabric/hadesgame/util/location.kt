package moe.caa.fabric.hadesgame.util

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.asDeferred
import moe.caa.fabric.hadesgame.game.GameCore
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.TicketType
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.chunk.status.ChunkStatus
import net.minecraft.world.level.levelgen.Heightmap
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

fun Entity.getLocation() = Location(
    level() as ServerLevel,
    x,
    y,
    z,
    yRot,
    xRot
)

fun Entity.teleport(location: Location) = teleportTo(
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
    var world: ServerLevel,
    var x: Double,
    var y: Double,
    var z: Double,
    var yaw: Float = 0.0f,
    var pitch: Float = 0.0f,
)

val randomLocationChunkTicketType: TicketType = Registry.register(
    BuiltInRegistries.TICKET_TYPE,
    "hadesgame:random_location",
    TicketType(300L, TicketType.FLAG_LOADING)
)

suspend fun ServerLevel.randomLobbySpawnLocation(): Location {
    while (true) {
        val posX = (-20000000 + Math.random() * 40000000).toInt()
        val posZ = (-20000000 + Math.random() * 40000000).toInt()

        val chunkPos = ChunkPos(posX shr 4, posZ shr 4)
        chunkSource.addTicketWithRadius(randomLocationChunkTicketType, chunkPos, 0)

        try {
            val worldChunk = GameCore.coroutineScope.async {
                repeat(10) {
                    val chunk = chunkSource
                        .getChunkFuture(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false)
                        .asDeferred()
                        .await()
                        .orElse(null)
                    if (chunk != null) return@async chunk
                    delay(50.milliseconds)
                }
                null
            }.await()

            if (worldChunk == null) {
                continue
            }

            val posY = getHeight(Heightmap.Types.WORLD_SURFACE, posX, posZ)
            val block = getBlockState(BlockPos(posX, posY - 1, posZ))

            if (block.block is LiquidBlock) {
                continue
            }
            if (block.block == Blocks.AIR) {
                continue
            }

            var platformMaxY = posY
            for (x in posX - 10..posX + 10) {
                for (z in posZ - 10..posZ + 10) {
                    platformMaxY = max(platformMaxY, getHeight(Heightmap.Types.WORLD_SURFACE, x, z))
                }
            }

            if (platformMaxY + 40 > height) {
                continue
            }

            return Location(this, posX + 0.5, posY + 20.0, posZ + 0.5)
        } finally {
            chunkSource.removeTicketWithRadius(randomLocationChunkTicketType, chunkPos, 0)
        }
    }
}
