package moe.caa.fabric.hadesgame.util

import kotlinx.coroutines.delay
import net.minecraft.block.Blocks
import net.minecraft.block.FluidBlock
import net.minecraft.entity.Entity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap

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

tailrec suspend fun ServerWorld.randomSafeLocation(tryPeriod: Long): Location {
    val posX = (-20000000 + Math.random() * 40000000).toInt()
    val posZ = (-20000000 + Math.random() * 40000000).toInt()

    getChunk(posX shr 4, posZ shr 4)

    val posY = getTopY(Heightmap.Type.MOTION_BLOCKING, posX, posZ)
    val block = getBlockState(BlockPos(posX, posY - 1, posZ))


    if (block.block is FluidBlock) {
        delay(tryPeriod)
        return randomSafeLocation(tryPeriod)
    }
    if (block.block == Blocks.AIR) {
        delay(tryPeriod)
        return randomSafeLocation(tryPeriod)
    }
    return Location(this, posX + 0.5, posY + 0.0, posZ + 0.5)
}