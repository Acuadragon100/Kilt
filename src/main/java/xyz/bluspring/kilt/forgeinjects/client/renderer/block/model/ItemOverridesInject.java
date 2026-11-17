// TRACKED HASH: ee2d4b36c902294bee93d8bff904182c9ba84f31
package xyz.bluspring.kilt.forgeinjects.client.renderer.block.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.*;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.client.resources.model.ModelBakeryInjection;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Mixin(ItemOverrides.class)
public class ItemOverridesInject {
    @Mutable @Shadow @Final private ItemOverrides.BakedOverride[] overrides;

    @Shadow @Final private ResourceLocation[] properties;
    @Unique // So we don't get any null pointer exceptions from hacky but already working mods.
    private static final BlockModel DUMMY_MODEL = new BlockModel(
        null, List.of(), Map.of(), null,
        null, ItemTransforms.NO_TRANSFORMS, List.of()
    );

    public ItemOverridesInject(ModelBaker baker, BlockModel model, List<ItemOverride> overrides) {}

    @CreateInitializer
    public ItemOverridesInject(ModelBaker baker, UnbakedModel model, List<ItemOverride> overrides, Function<Material, TextureAtlasSprite> spriteGetter) {
        this(baker, model instanceof BlockModel m ? m : DUMMY_MODEL, overrides);
        init(baker, model, overrides, spriteGetter);
    }

    @Unique // CreateInitializer doesn't handle lambdas very well.
    private void init(ModelBaker baker, UnbakedModel model, List<ItemOverride> overrides, Function<Material, TextureAtlasSprite> spriteGetter) {
        // If we have a spriteGetter we redo all this. Most Forge mods will expect this to not be touched by mixins anyway I hope.
        Object2IntMap<ResourceLocation> object2IntMap = new Object2IntOpenHashMap<>();

        for(int i = 0; i < this.properties.length; ++i) {
            object2IntMap.put(this.properties[i], i);
        }

        List<ItemOverrides.BakedOverride> list = Lists.newArrayList();

        for(int j = overrides.size() - 1; j >= 0; --j) {
            ItemOverride itemOverride = overrides.get(j);
            BakedModel bakedModel = this.bakeModel(baker, model, itemOverride, spriteGetter);
            ItemOverrides.PropertyMatcher[] propertyMatchers = itemOverride.getPredicates().map(predicate -> {
                int i = object2IntMap.getInt(predicate.getProperty());
                return new ItemOverrides.PropertyMatcher(i, predicate.getValue());
            }).toArray(ItemOverrides.PropertyMatcher[]::new);
            list.add(new ItemOverrides.BakedOverride(propertyMatchers, bakedModel));
        }

        this.overrides = list.toArray(new ItemOverrides.BakedOverride[0]);
    }

    @Nullable
    private BakedModel bakeModel(
        ModelBaker p_249483_, UnbakedModel p_251965_, ItemOverride p_250816_,
        Function<Material, TextureAtlasSprite> spriteGetter
    ) {
        UnbakedModel unbakedmodel = p_249483_.getModel(p_250816_.getModel());
        if (Objects.equals(unbakedmodel, p_251965_)) {
            return null;
        } else if (p_249483_ instanceof ModelBakeryInjection.ModelBakerImplInjection inj) {
            return inj.bake(p_250816_.getModel(), BlockModelRotation.X0_Y0, spriteGetter);
        } else {
            return p_249483_.bake(p_250816_.getModel(), BlockModelRotation.X0_Y0);
        }
    }

    @Unique
    public ImmutableList<ItemOverrides.BakedOverride> getOverrides() {
        return ImmutableList.copyOf(overrides);
    }
}
