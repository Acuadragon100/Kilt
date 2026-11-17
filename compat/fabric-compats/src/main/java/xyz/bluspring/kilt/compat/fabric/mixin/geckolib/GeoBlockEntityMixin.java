package xyz.bluspring.kilt.compat.fabric.mixin.geckolib;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import software.bernie.geckolib.animatable.GeoBlockEntity;

@Mixin(GeoBlockEntity.class)
public interface GeoBlockEntityMixin {

    @Shadow void stopTriggeredAnim(@Nullable String controllerName, @Nullable String animName);

    // For some reason they use a different name for this method on Fabric......
    @Unique
    default void stopTriggeredAnimation(@Nullable String controllerName, @Nullable String animName) {
        stopTriggeredAnim(controllerName, animName);
    }

}
