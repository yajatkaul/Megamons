package com.cobblemon.yajatkaul.mega_showdown.mixin;

import com.cobblemon.yajatkaul.mega_showdown.MegaShowdown;
import com.cobblemon.yajatkaul.mega_showdown.item.ModItems;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow
    @Final
    private ItemModels models;

    @Shadow
    public abstract ItemModels getModels();

    @ModifyVariable(
            method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
            at = @At(value = "HEAD"),
            argsOnly = true
    )
    public BakedModel renderItem(BakedModel bakedModel, @Local(argsOnly = true) ItemStack stack, @Local(argsOnly = true) ModelTransformationMode renderMode) {
        if (stack.getItem() == ModItems.MEGA_BRACELET && (renderMode == ModelTransformationMode.GUI || renderMode == ModelTransformationMode.GROUND || renderMode == ModelTransformationMode.FIXED)) {
            return getModels().getModelManager().getModel(ModelIdentifier.ofInventoryVariant(Identifier.of(MegaShowdown.MOD_ID, "megabracelet")));
        }
        else if (stack.getItem() == ModItems.MEGA_RED_BRACELET && (renderMode == ModelTransformationMode.GUI || renderMode == ModelTransformationMode.GROUND || renderMode == ModelTransformationMode.FIXED)) {
            return getModels().getModelManager().getModel(ModelIdentifier.ofInventoryVariant(Identifier.of(MegaShowdown.MOD_ID, "megabracelet_red")));
        }
        else if (stack.getItem() == ModItems.MEGA_PINK_BRACELET && (renderMode == ModelTransformationMode.GUI || renderMode == ModelTransformationMode.GROUND || renderMode == ModelTransformationMode.FIXED)) {
            return getModels().getModelManager().getModel(ModelIdentifier.ofInventoryVariant(Identifier.of(MegaShowdown.MOD_ID, "megabracelet_pink")));
        }
        else if (stack.getItem() == ModItems.MEGA_YELLOW_BRACELET && (renderMode == ModelTransformationMode.GUI || renderMode == ModelTransformationMode.GROUND || renderMode == ModelTransformationMode.FIXED)) {
            return getModels().getModelManager().getModel(ModelIdentifier.ofInventoryVariant(Identifier.of(MegaShowdown.MOD_ID, "megabracelet_yellow")));
        }
        else if (stack.getItem() == ModItems.MEGA_GREEN_BRACELET && (renderMode == ModelTransformationMode.GUI || renderMode == ModelTransformationMode.GROUND || renderMode == ModelTransformationMode.FIXED)) {
            return getModels().getModelManager().getModel(ModelIdentifier.ofInventoryVariant(Identifier.of(MegaShowdown.MOD_ID, "megabracelet_green")));
        }
        else if (stack.getItem() == ModItems.MEGA_BLUE_BRACELET && (renderMode == ModelTransformationMode.GUI || renderMode == ModelTransformationMode.GROUND || renderMode == ModelTransformationMode.FIXED)) {
            return getModels().getModelManager().getModel(ModelIdentifier.ofInventoryVariant(Identifier.of(MegaShowdown.MOD_ID, "megabracelet_blue")));
        }
        else if (stack.getItem() == ModItems.MEGA_BLACK_BRACELET && (renderMode == ModelTransformationMode.GUI || renderMode == ModelTransformationMode.GROUND || renderMode == ModelTransformationMode.FIXED)) {
            return getModels().getModelManager().getModel(ModelIdentifier.ofInventoryVariant(Identifier.of(MegaShowdown.MOD_ID, "megabracelet_black")));
        }
        else if (stack.getItem() == ModItems.MEGA_RING && (renderMode == ModelTransformationMode.GUI || renderMode == ModelTransformationMode.GROUND || renderMode == ModelTransformationMode.FIXED)) {
            return getModels().getModelManager().getModel(ModelIdentifier.ofInventoryVariant(Identifier.of(MegaShowdown.MOD_ID, "megaring")));
        }
        else if (stack.getItem() == ModItems.BRENDAN_MEGA_CUFF && (renderMode == ModelTransformationMode.GUI || renderMode == ModelTransformationMode.GROUND || renderMode == ModelTransformationMode.FIXED)) {
            return getModels().getModelManager().getModel(ModelIdentifier.ofInventoryVariant(Identifier.of(MegaShowdown.MOD_ID, "brendan_mega_cuff")));
        }

        return bakedModel;
    }

    @ModifyVariable(
            method = "getModel",
            at = @At(value = "STORE"),
            ordinal = 1
    )
    public BakedModel getHeldItemModelMixin(BakedModel bakedModel, @Local(argsOnly = true) ItemStack stack) {
        if (stack.getItem() == ModItems.MEGA_BRACELET) {
            return this.models.getModelManager().getModel(ModelIdentifier.ofInventoryVariant(Identifier.of(MegaShowdown.MOD_ID, "megabracelet_3d")));
        }
        else if (stack.getItem() == ModItems.MEGA_RED_BRACELET) {
            return this.models.getModelManager().getModel(ModelIdentifier.ofInventoryVariant(Identifier.of(MegaShowdown.MOD_ID, "megabracelet_red_3d")));
        }
        else if (stack.getItem() == ModItems.MEGA_BLUE_BRACELET) {
            return this.models.getModelManager().getModel(ModelIdentifier.ofInventoryVariant(Identifier.of(MegaShowdown.MOD_ID, "megabracelet_blue_3d")));
        }
        else if (stack.getItem() == ModItems.MEGA_BLACK_BRACELET) {
            return this.models.getModelManager().getModel(ModelIdentifier.ofInventoryVariant(Identifier.of(MegaShowdown.MOD_ID, "megabracelet_black_3d")));
        }
        else if (stack.getItem() == ModItems.MEGA_PINK_BRACELET) {
            return this.models.getModelManager().getModel(ModelIdentifier.ofInventoryVariant(Identifier.of(MegaShowdown.MOD_ID, "megabracelet_pink_3d")));
        }
        else if (stack.getItem() == ModItems.MEGA_GREEN_BRACELET) {
            return this.models.getModelManager().getModel(ModelIdentifier.ofInventoryVariant(Identifier.of(MegaShowdown.MOD_ID, "megabracelet_green_3d")));
        }
        else if (stack.getItem() == ModItems.MEGA_YELLOW_BRACELET) {
            return this.models.getModelManager().getModel(ModelIdentifier.ofInventoryVariant(Identifier.of(MegaShowdown.MOD_ID, "megabracelet_yellow_3d")));
        }
        else if (stack.getItem() == ModItems.MEGA_RING) {
            return this.models.getModelManager().getModel(ModelIdentifier.ofInventoryVariant(Identifier.of(MegaShowdown.MOD_ID, "megaring_3d")));
        }
        else if (stack.getItem() == ModItems.BRENDAN_MEGA_CUFF) {
            return this.models.getModelManager().getModel(ModelIdentifier.ofInventoryVariant(Identifier.of(MegaShowdown.MOD_ID, "brendan_mega_cuff_3d")));
        }


        return bakedModel;
    }
}