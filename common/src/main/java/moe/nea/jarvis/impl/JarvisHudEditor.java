package moe.nea.jarvis.impl;

import moe.nea.jarvis.api.JarvisAnchor;
import moe.nea.jarvis.api.JarvisHud;
import moe.nea.jarvis.api.JarvisPlugin;
import moe.nea.jarvis.api.Point;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class JarvisHudEditor extends Screen {
    private final Screen lastScreen;
    private final List<JarvisHud> huds;
    private final Map<JarvisHud, BinaryInterpolator> hoverProgress = new IdentityHashMap<>();
    private boolean isScaling = false;
    private JarvisHud grabbedHud;
    private JarvisAnchor grabbedAnchor;
    private Point grabbedHudTopLeftCoordOffset;
    private Point oppositeCorner;
    private double scalePerDistance;
    private final JarvisContainer container;

    public JarvisHudEditor(Screen lastScreen, List<JarvisHud> huds, JarvisContainer container) {
        super(Text.translatable("jarvis.editor"));
        this.lastScreen = lastScreen;
        this.huds = huds;
        this.container = container;
        for (JarvisHud hud : huds) {
            hoverProgress.put(hud, new BinaryInterpolator(200));
        }
    }

    private boolean isOverlayHovered(JarvisHud hud, double mouseX, double mouseY) {
        var position = hud.getEffectivePosition(container);
        int absoluteX = position.x();
        int absoluteY = position.y();
        return absoluteX < mouseX && mouseX < absoluteX + hud.getEffectiveWidth()
            && absoluteY < mouseY && mouseY < absoluteY + hud.getEffectiveHeight();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        assert client != null;
        context.drawCenteredTextWithShadow(client.textRenderer,
            Text.translatable("jarvis.editor.title").setStyle(Style.EMPTY.withColor(new Color(100, 200, 255, 255).getRGB())),
            width / 2, 20, -1);
        context.drawCenteredTextWithShadow(client.textRenderer,
            Text.translatable("jarvis.editor.scaleBlurb").setStyle(Style.EMPTY.withColor(new Color(200, 200, 200, 255).getRGB())), width / 2, 35, -1);


        boolean hasHoveredAny = grabbedHud != null;
        for (JarvisHud hud : huds) {
            context.getMatrices().pushMatrix();
            hud.applyTransformations(this.container, context.getMatrices());
            boolean hovered = grabbedHud == hud;
            if (!hasHoveredAny && isOverlayHovered(hud, mouseX, mouseY)) {
                hovered = true;
                hasHoveredAny = true;
            }
            BinaryInterpolator hoverInterpolator = hoverProgress.get(hud);
            hoverInterpolator.lerpTo(hovered ? 1 : 0);
            fillFadeOut(context, hud.getEffectiveWidth(), hud.getEffectiveHeight(), 1F);
            fillOutline(context, hud.getEffectiveWidth(), hud.getEffectiveHeight(),
                hoverInterpolator.lerp(new Color(0xFF343738, true), new Color(0xFF85858A, true)).getRGB()
            );
            context.drawCenteredTextWithShadow(client.textRenderer, hud.getLabel(), hud.getEffectiveWidth() / 2, hud.getEffectiveHeight() / 2, -1);
            context.getMatrices().popMatrix();
        }

        if (JarvisUtil.isTest) {
            if (oppositeCorner != null) {
                var pos = oppositeCorner.roundToInt();
                context.fill(pos.x(), pos.y(), pos.x() + 2, pos.y() + 2, 0xFFFF0000);
            }
        }
    }

    public void fillFadeOut(DrawContext drawContext, int width, int height, float opaquePercentage) {
        drawContext.fill(0, 0, width, height, 0x80000000);
    }

    public void fillOutline(DrawContext drawContext, int width, int height, int color) {
        drawContext.fill(0, 0, width, 1, color);
        drawContext.fill(0, height - 1, width, height, color);
        drawContext.fill(0, 1, 1, height - 1, color);
        drawContext.fill(width - 1, 1, width, height - 1, color);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (grabbedHud != null)
                return false;
            isScaling = false;
            tryGrabOverlay(click.x(), click.y());
            return true;
        }
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (grabbedHud != null)
                return false;
            isScaling = true;
            tryGrabOverlay(click.x(), click.y());
            if (!(grabbedHud instanceof JarvisHud.Scalable scalable)) {
                tryReleaseOverlay();
                return false;
            }
            JarvisAnchor opposite = grabbedAnchor.getOpposite();
            scalePerDistance = scalable.getScale() / oppositeCorner.distanceTo(new Point(click.x(), click.y()));
            System.out.printf("Scaling in relation to %s (%s). Scale per distance: %.5f%n", opposite, oppositeCorner, scalePerDistance);
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if ((click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && !isScaling)
            || (click.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT && isScaling)) {
            tryReleaseOverlay();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (isScaling) {
            tryScaleGrabbedOverlay(mouseX, mouseY);
        } else
            tryMoveGrabbedOverlay(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public void close() {
        assert client != null;
        client.setScreen(lastScreen);
        container.getAllPlugins().forEach(JarvisPlugin::onHudEditorClosed);
    }

    public void tryGrabOverlay(double mouseX, double mouseY) {
        for (JarvisHud hud : huds) {
            if (isOverlayHovered(hud, mouseX, mouseY)) {
                grabbedHud = hud;
                var inTopLeftSpace = hud.getEffectivePosition(container);
                double offsetX = mouseX - inTopLeftSpace.x();
                double offsetY = mouseY - inTopLeftSpace.y();
                JarvisAnchor closestAnchor = JarvisAnchor.byQuadrant(
                    offsetX < hud.getEffectiveWidth() / 2,
                    offsetY < hud.getEffectiveHeight() / 2
                );
                grabbedHudTopLeftCoordOffset = new Point(offsetX, offsetY);
                oppositeCorner = JarvisAnchor.TOP_LEFT.translate(closestAnchor.getOpposite(), inTopLeftSpace.x(), inTopLeftSpace.y(), hud.getEffectiveWidth(), hud.getEffectiveHeight());
                grabbedAnchor = closestAnchor;
                System.out.printf("Anchored to %s : %s%n", grabbedAnchor, grabbedHudTopLeftCoordOffset);
                return;
            }
        }
    }

    public void tryScaleGrabbedOverlay(double mouseX, double mouseY) {
        JarvisHud grabbedHud = this.grabbedHud;
        if (!(grabbedHud instanceof JarvisHud.Scalable scalable))
            return; // TODO: show a warning for non scalable overlays
        double distance = new Point(mouseX, mouseY).distanceTo(oppositeCorner);
        double newScale = distance * scalePerDistance;
        if (newScale < 0.2) return;
        scalable.setScale((float) newScale);
        grabbedHud.setPosition(
            grabbedAnchor.getOpposite().translate(JarvisAnchor.TOP_LEFT, oppositeCorner.x(), oppositeCorner.y(), scalable.getEffectiveWidth(), scalable.getEffectiveHeight())
                .roundToInt()
        );
    }

    public void tryMoveGrabbedOverlay(double mouseX, double mouseY) {
        JarvisHud grabbedHud = this.grabbedHud;
        if (grabbedHud == null) return;
        double x = mouseX - grabbedHudTopLeftCoordOffset.x();
        double y = mouseY - grabbedHudTopLeftCoordOffset.y();
        grabbedHud.setPosition(new Point(x, y).roundToInt());
    }

    public void tryReleaseOverlay() {
        grabbedHud = null;
    }
}
