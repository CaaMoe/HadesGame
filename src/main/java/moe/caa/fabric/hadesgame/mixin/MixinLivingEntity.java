package moe.caa.fabric.hadesgame.mixin;

import moe.caa.fabric.hadesgame.access.LivingEntityAccess;
import moe.caa.fabric.hadesgame.event.OnPreDeath;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity implements LivingEntityAccess {
    @Shadow
    protected abstract void drop(ServerWorld world, DamageSource damageSource);

    @Override
    public void hadesGame$callDrop(@NotNull ServerWorld world, @NotNull DamageSource damageSource) {
        drop(world, damageSource);
    }

    @Redirect(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;onDeath(Lnet/minecraft/entity/damage/DamageSource;)V"))
    void onRedirectDamage(LivingEntity instance, DamageSource damageSource) {
        if (OnPreDeath.Companion.shouldCancel(instance, damageSource)) {
            return;
        }
        instance.onDeath(damageSource);
    }
}
