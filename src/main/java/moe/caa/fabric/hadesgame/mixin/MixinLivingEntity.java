package moe.caa.fabric.hadesgame.mixin;

import moe.caa.fabric.hadesgame.event.OnEntityHeal;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void onHeal(float heal, CallbackInfo ci) {
        if (!OnEntityHeal.Companion.shouldContinue((LivingEntity) (Object) this, heal)) {
            ci.cancel();
        }
    }
}