package com.notcritkai.enderstoragenetworks.mixin;

import codechicken.enderstorage.network.TankSynchroniser;
import codechicken.enderstorage.api.Frequency;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.notcritkai.enderstoragenetworks.Config;

@Mixin(TankSynchroniser.TankState.class)
public abstract class TankStateMixin {
    @Inject(method = "setFrequency", at = @At("TAIL"))
    private void enderstorage_networks$setFrequency(
            Frequency frequency,
            CallbackInfo ci) {
        enderstorage_networks$applyInfiniteVisual(frequency);
    }

    @Inject(method = "update", at = @At("HEAD"))
    private void enderstorage_networks$update(
            boolean clientSide,
            CallbackInfo ci) {
        if (clientSide) {
            enderstorage_networks$applyInfiniteVisual(
                    ((TankSynchroniser.TankState) (Object) this).frequency);
        }
    }

    @Unique
    private void enderstorage_networks$applyInfiniteVisual(Frequency frequency) {
        if (!Minecraft.getInstance().isSameThread()) {
            return;
        }

        Fluid fluid = Config.getInfiniteFluid(frequency);
        if (fluid == null) {
            return;
        }

        TankSynchroniser.TankState state =
                (TankSynchroniser.TankState) (Object) this;
        FluidStack synthetic = new FluidStack(fluid, enderstorage_networks$getCapacity());

        state.c_liquid = synthetic.copy();
        state.s_liquid = synthetic.copy();
    }

    @Unique
    private int enderstorage_networks$getCapacity() {
        return 16_000;
    }

    @Inject(method = "sync", at = @At("TAIL"))
    private void enderstorage_networks$syncVisual(
            FluidStack synchronizedFluid,
            CallbackInfo ci) {
        TankSynchroniser.TankState state =
                (TankSynchroniser.TankState) (Object) this;

        if (Config.getInfiniteFluid(state.frequency) != null) {
            return;
        }

        state.c_liquid = state.s_liquid.copy();
    }
}
