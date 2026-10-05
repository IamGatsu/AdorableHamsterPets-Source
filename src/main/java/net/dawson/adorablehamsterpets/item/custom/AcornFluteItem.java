package net.dawson.adorablehamsterpets.item.custom;

import dev.architectury.platform.Platform;
import net.dawson.adorablehamsterpets.config.Configs;
import net.dawson.adorablehamsterpets.flute.AcornFluteVariant;
import net.dawson.adorablehamsterpets.flute.FlutePerformanceManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

/**
 * Cosmetic Acorn Flute variant that starts one server-authoritative performance.
 */
public final class AcornFluteItem extends Item {

    private final AcornFluteVariant variant;

    public AcornFluteItem(Item.Properties settings, AcornFluteVariant variant) {
        super(settings);
        this.variant = variant;
    }

    public AcornFluteVariant variant() {
        return this.variant;
    }

    public AcornFluteVariant getVariant() {
        return this.variant;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (!world.isClientSide() && user instanceof ServerPlayer player) {
            FlutePerformanceManager.startPerformance(player, stack, this.variant);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag type) {
        if (Configs.AHP_UI.enableItemTooltips) {
            tooltip.accept(Component.translatable("tooltip.adorablehamsterpets.acorn_flute.common")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.accept(Component.translatable(
                            "tooltip.adorablehamsterpets.acorn_flute_"
                                    + this.variant.name().toLowerCase(Locale.ROOT)
                                    + ".variant")
                    .withStyle(ChatFormatting.GRAY));
        } else if (!Platform.isModLoaded("emi")) {
            tooltip.accept(Component.literal("Adorable Hamster Pets").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
        }
        super.appendHoverText(stack, context, tooltipDisplay, tooltip, type);
    }
}
