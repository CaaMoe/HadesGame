package moe.caa.fabric.hadesgame.util

import net.minecraft.entity.Entity
import net.minecraft.server.world.ServerWorld

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