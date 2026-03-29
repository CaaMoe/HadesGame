package moe.caa.fabric.hadesgame.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.caa.fabric.hadesgame.event.OnEntityHeal;
import moe.caa.fabric.hadesgame.event.OnEntityLivingFlagChange;
import moe.caa.fabric.hadesgame.event.OnPreDeath;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @WrapOperation(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;die(Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private void onRedirectDamage(LivingEntity instance, DamageSource damageSource, Operation<Void> original) {
        if (OnPreDeath.Companion.shouldContinue(instance, damageSource)) {
            original.call(instance, damageSource);
        }
    }

    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void onHeal(float amount, CallbackInfo ci) {
        if (!OnEntityHeal.Companion.shouldContinue((LivingEntity) (Object) this, amount)) {
            ci.cancel();
        }
    }

    @Inject(method = "setLivingEntityFlag", at = @At("HEAD"), cancellable = true)
    private void onSetLivingEntityFlag(int mask, boolean value, CallbackInfo ci) {
        if (!OnEntityLivingFlagChange.Companion.shouldContinue((LivingEntity) (Object) this, mask, value)) {
            ci.cancel();
        }
    }
}
