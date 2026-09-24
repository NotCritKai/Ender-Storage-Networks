package com.notcritkai.enderstoragenetworks.mixin;

import com.notcritkai.enderstoragenetworks.Config;
import codechicken.enderstorage.api.AbstractEnderStorage;
import codechicken.enderstorage.storage.EnderLiquidStorage;
import codechicken.lib.colour.EnumColour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderLiquidStorage.class)
public abstract class EnderLiquidStorageMixin {
    @Inject(method = "getFluid", at = @At("HEAD"), cancellable = true)
    private void enderstorage_networks$getFluid(
            CallbackInfoReturnable<FluidStack> cir) {
        Fluid fluid = specialFluid();
        if (fluid != null) {
            cir.setReturnValue(new FluidStack(fluid, ((EnderLiquidStorage) (Object) this).getCapacity()));
        }
    }

    @Inject(method = "getFluidAmount", at = @At("HEAD"), cancellable = true)
    private void enderstorage_networks$getFluidAmount(
            CallbackInfoReturnable<Integer> cir) {
        if (specialFluid() != null) {
            cir.setReturnValue(((EnderLiquidStorage) (Object) this).getCapacity());
        }
    }

    @Inject(method = "fill", at = @At("HEAD"), cancellable = true)
    private void enderstorage_networks$fill(
            FluidStack resource,
            FluidAction action,
            CallbackInfoReturnable<Integer> cir) {
        Fluid fluid = specialFluid();
        if (fluid != null) {
            cir.setReturnValue(resource.is(fluid) ? resource.getAmount() : 0);
        }
    }

    @Inject(method = "drain(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/neoforged/neoforge/fluids/FluidStack;", at = @At("HEAD"), cancellable = true)
    private void enderstorage_networks$drainStack(
            FluidStack resource,
            FluidAction action,
            CallbackInfoReturnable<FluidStack> cir) {
        Fluid fluid = specialFluid();
        if (fluid != null) {
            if (resource.is(fluid)) {
                cir.setReturnValue(new FluidStack(fluid, resource.getAmount()));
            } else {
                cir.setReturnValue(FluidStack.EMPTY);
            }
        }
    }

    @Inject(method = "isFluidValid", at = @At("HEAD"), cancellable = true)
    private void enderstorage_networks$isFluidValid(
            FluidStack resource,
            CallbackInfoReturnable<Boolean> cir) {
        Fluid fluid = specialFluid();
        if (fluid != null) {
            cir.setReturnValue(resource.is(fluid));
        }
    }


    @Inject(method = "drain(ILnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/neoforged/neoforge/fluids/FluidStack;", at = @At("HEAD"), cancellable = true)
    private void enderstorage_networks$drainAmount(
            int maxDrain,
            FluidAction action,
            CallbackInfoReturnable<FluidStack> cir) {
        Fluid fluid = specialFluid();
        if (fluid != null) {
            cir.setReturnValue(new FluidStack(fluid, Math.max(0, maxDrain)));
        }
    }

    private codechicken.enderstorage.api.Frequency storageFrequency() {
        return ((AbstractEnderStorage) (Object) this).freq;
    }


    private Fluid specialFluid() {
        return Config.getInfiniteFluid(storageFrequency());
    }
}
