package com.example.mod.client.mixin;

import com.example.mod.SimpleMagicMirrorItem;
import com.example.mod.SuperMagicMirrorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.RecoverWorldDataScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "setScreenAndShow", at = @At("HEAD"), cancellable = true)
    private void onSetScreenAndShow(Screen screen, CallbackInfo ci) {
        if (screen instanceof RecoverWorldDataScreen || screen instanceof LevelLoadingScreen) {
            if (SuperMagicMirrorItem.isMirrorTeleporting) {
                ci.cancel();
                SuperMagicMirrorItem.isMirrorTeleporting = false;
            }
            if (SimpleMagicMirrorItem.isMirrorTeleporting) {
                ci.cancel();
                SimpleMagicMirrorItem.isMirrorTeleporting = false;
            }
        }
    }
}
