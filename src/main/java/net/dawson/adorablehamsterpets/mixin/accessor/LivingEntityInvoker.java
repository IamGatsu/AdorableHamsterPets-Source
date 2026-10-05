package net.dawson.adorablehamsterpets.mixin.accessor;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityInvoker {

    @Invoker("getHurtSound")
    SoundEvent adorablehamsterpets$callGetHurtSound(DamageSource source);

    @Invoker("getDeathSound")
    SoundEvent adorablehamsterpets$callGetDeathSound();
}