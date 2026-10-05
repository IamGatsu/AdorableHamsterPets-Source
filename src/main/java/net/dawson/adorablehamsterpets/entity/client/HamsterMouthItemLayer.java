package net.dawson.adorablehamsterpets.entity.client;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.dawson.adorablehamsterpets.entity.custom.HamsterEntity;
import net.dawson.adorablehamsterpets.util.HamsterMouthItemOffsets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Renders the item a hamster carries in its mouth, attached to the "nose" bone. */
public class HamsterMouthItemLayer extends BlockAndItemGeoLayer<HamsterEntity, Void, LivingEntityRenderState> {

    public HamsterMouthItemLayer(EntityRendererProvider.Context context, GeoRenderer<HamsterEntity, Void, LivingEntityRenderState> renderer) {
        super(context, renderer);
    }

    @Override
    protected List<RenderData> getRelevantBones(HamsterEntity animatable, @Nullable Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        if (!animatable.isHoldingMouthItem()) {
            return List.of();
        }
        ItemStack stack = animatable.getMouthItemStack();
        if (stack.isEmpty()) {
            return List.of();
        }
        return List.of(RenderData.item("nose", ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                RenderUtil.createRenderStateForItem(stack, this.itemModelResolver, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, animatable)));
    }

    @Override
    public void addRenderData(HamsterEntity animatable, @Nullable Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        List<RenderData> contents = getRelevantBones(animatable, relatedObject, renderState, partialTick);
        if (!contents.isEmpty()) {
            renderState.addGeckolibData(BlockAndItemGeoLayer.CONTENTS, contents);
        }
    }

    @Override
    protected void submitItemStackRender(PoseStack poseStack, GeoBone bone, ItemStackRenderState stackState, ItemDisplayContext displayContext,
                                         LivingEntityRenderState renderState, SubmitNodeCollector renderTasks, int packedLight) {
        poseStack.pushPose();
        HamsterMouthItemOffsets.applyMouthItemTransforms(poseStack);
        super.submitItemStackRender(poseStack, bone, stackState, displayContext, renderState, renderTasks, packedLight);
        poseStack.popPose();
    }
}
