package pbone.armor_toughness_bar.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* We need to mixin to InGameHud.renderStatusBars and draw there because using
    fabric-api's HudRenderCallback results in our bar drawing after chat and having general transparency issues.
 */

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Unique
    private static final Identifier TOUGHNESS_BAR = new Identifier("armor_toughness_bar:toughness_bar.png");

    @Inject(method = "renderStatusBars", at = @At("TAIL"))
    private void renderStatusBars(DrawContext context, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.options.hudHidden
                || client.interactionManager == null
                || !client.interactionManager.hasStatusBars()
                || !(client.getCameraEntity() instanceof PlayerEntity player)
        ) {
            return;
        }

        if (!player.canTakeDamage()) {
            return;
        }

        client.getProfiler().push("armor_toughness_bar");

        int windowWidth = context.getScaledWindowWidth();
        int windowHeight = context.getScaledWindowHeight();

        int armorToughness = (int) player.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
        int maxAir = player.getMaxAir();
        int currentAir = Math.min(maxAir, player.getAir());
        boolean areBubblesVisible = currentAir < maxAir || player.isSubmergedIn(FluidTags.WATER);

        int origY = windowHeight - 49;
        int origX = windowWidth / 2 + 82;

        if (areBubblesVisible) {
            origY -= 10;
        }

        if (armorToughness > 0) {
            for (int i = 0; i < 10; i++) {
                int posX = origX - i * 8;

                if (armorToughness >= 2) {
                    drawToughnessIcon(context, posX, origY, 18);
                    armorToughness -= 2;
                } else if (armorToughness == 1) {
                    drawToughnessIcon(context, posX, origY, 9);
                    armorToughness -= 1;
                } else {
                    drawToughnessIcon(context, posX, origY, 0);
                }
            }
        }

        client.getProfiler().pop();
    }

    @Unique
    private static void drawToughnessIcon(DrawContext context, int posX, int posY, int u) {
        context.drawTexture(
                TOUGHNESS_BAR,
                posX,
                posY,
                u,
                0,
                9,
                9
        );
    }
}
