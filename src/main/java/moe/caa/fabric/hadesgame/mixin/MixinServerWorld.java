package moe.caa.fabric.hadesgame.mixin;

import moe.caa.fabric.hadesgame.event.OnSpawnEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public class MixinServerWorld {

    @Inject(method = "spawnEntity", at = @At(value = "HEAD"))
    private void onSpawnEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        OnSpawnEntity.Companion.callEvent(entity);
    }
}
