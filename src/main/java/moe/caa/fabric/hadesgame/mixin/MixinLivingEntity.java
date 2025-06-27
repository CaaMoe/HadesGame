package moe.caa.fabric.hadesgame.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.caa.fabric.hadesgame.event.OnPreDeath;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
    @WrapOperation(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;onDeath(Lnet/minecraft/entity/damage/DamageSource;)V"))
    void onRedirectDamage(LivingEntity instance, DamageSource damageSource, Operation<Void> original) {
        if (OnPreDeath.Companion.shouldCancel(instance, damageSource)) {
            return;
        }
        instance.onDeath(damageSource);
    }
}
