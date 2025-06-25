package moe.caa.fabric.hadesgame.access

import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.world.ServerWorld

interface LivingEntityAccess {
    fun `hadesGame$callDrop`(world: ServerWorld, damageSource: DamageSource)
}