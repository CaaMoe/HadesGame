package moe.caa.fabric.hadesgame.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor extends EntityAccessor {

    @Invoker("drop")
    void invokeDrop(ServerWorld world, DamageSource damageSource);

    @Invoker("setLivingFlag")
    void invokeSetLivingFlag(int mask, boolean value);
}
