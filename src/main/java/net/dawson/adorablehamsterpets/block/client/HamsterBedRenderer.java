package net.dawson.adorablehamsterpets.block.client;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dawson.adorablehamsterpets.block.custom.HamsterBedBlock;
import net.dawson.adorablehamsterpets.block.entity.HamsterBedBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** GeckoLib 5 block entity renderer for the hamster bed. */
public class HamsterBedRenderer extends GeoBlockRenderer<HamsterBedBlockEntity, BlockEntityRenderState> {

    private static final DataTicket<Boolean> UPSIDE_DOWN = DataTicket.create("adorablehamsterpets_bed_upside_down", Boolean.class);

    public HamsterBedRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new HamsterBedModel());
    }

    /**
     * The bedding leaves are zero-thickness planes, so each one has an up and a down face on the exact same
     * plane. The original (1.21.1) forced the culled {@code entityCutout} for that reason. In 26.x the culled
     * variant was renamed to {@code entityCutoutCull}, while {@code entityCutout} (GeckoLib's default) no longer
     * culls back faces -> both faces render and z-fight, which shaders make visible as constant flickering.
     */
    @Override
    public @Nullable RenderType getRenderType(BlockEntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityCutoutCull(texture);
    }

    @Override
    protected Direction getBlockStateDirection(HamsterBedBlockEntity block) {
        BlockState state = block.getBlockState();
        return state.hasProperty(HamsterBedBlock.ORIENTATION)
                ? state.getValue(HamsterBedBlock.ORIENTATION)
                : super.getBlockStateDirection(block);
    }

    @Override
    public void addRenderData(HamsterBedBlockEntity animatable, @Nullable Void relatedObject, BlockEntityRenderState renderState, float partialTick) {
        BlockState state = animatable.getBlockState();
        renderState.addGeckolibData(HamsterBedModel.WOOD_VARIANT, state.getValue(HamsterBedBlock.WOOD_VARIANT));
        renderState.addGeckolibData(UPSIDE_DOWN, state.getValue(HamsterBedBlock.UPSIDE_DOWN));
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<BlockEntityRenderState> renderPassInfo) {
        if (Boolean.TRUE.equals(renderPassInfo.getGeckolibData(UPSIDE_DOWN))) {
            PoseStack poseStack = renderPassInfo.poseStack();
            poseStack.translate(0, 0.5, 0);
            poseStack.mulPose(Axis.XP.rotationDegrees(180));
            poseStack.translate(0, -0.5, 0);
        }
        super.adjustRenderPose(renderPassInfo);
    }
}
