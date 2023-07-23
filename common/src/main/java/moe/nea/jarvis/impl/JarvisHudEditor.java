package moe.nea.jarvis.impl;

import moe.nea.jarvis.api.*;
import moe.nea.jarvis.api.Point;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
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
    private Point grabbedHudCoordOffset;
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
        int absoluteX = hud.getAbsoluteX();
        int absoluteY = hud.getAbsoluteY();
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
            context.getMatrices().push();
            hud.applyTransformations(context.getMatrices());
            hud.getAnchor().transformTo(JarvisAnchor.TOP_LEFT, context.getMatrices(), hud.getWidth(), hud.getHeight());
            boolean hovered = grabbedHud == hud;
            if (!hasHoveredAny && isOverlayHovered(hud, mouseX, mouseY)) {
                hovered = true;
                hasHoveredAny = true;
            }
            BinaryInterpolator hoverInterpolator = hoverProgress.get(hud);
            hoverInterpolator.lerpTo(hovered ? 1 : 0);
            fillFadeOut(context, hud.getWidth(), hud.getHeight(), hud.getFadeOutPercentage());
            context.drawBorder(0, 0, hud.getWidth(), hud.getHeight(),
                    hoverInterpolator.lerp(new Color(0xFF343738, true), new Color(0xFF85858A, true)).getRGB()
            );
            context.drawCenteredTextWithShadow(client.textRenderer, hud.getLabel(), hud.getWidth() / 2, hud.getHeight() / 2, -1);
            context.getMatrices().pop();
        }
    }

    public void fillFadeOut(DrawContext drawContext, int width, int height, float opaquePercentage) {
        if (opaquePercentage >= 1F) {
            drawContext.fill(0, 0, width, height, 0x80000000);
            return;
        }
        VertexConsumer vertexConsumer = drawContext.getVertexConsumers().getBuffer(RenderLayer.getGui());
        float translucentStart = opaquePercentage * height;
        float translucentEnd = height;
        Matrix4f matrix4f = drawContext.getMatrices().peek().getPositionMatrix();
        vertexConsumer.vertex(matrix4f, 0, 0, 0).color(0x0, 0x0, 0x0, 0x80).next();
        vertexConsumer.vertex(matrix4f, 0, translucentStart, 0).color(0x0, 0x0, 0x0, 0x80).next();
        vertexConsumer.vertex(matrix4f, width, translucentStart, 0).color(0x0, 0x0, 0x0, 0x80).next();
        vertexConsumer.vertex(matrix4f, width, 0, 0).color(0x0, 0x0, 0x0, 0x80).next();
        vertexConsumer.vertex(matrix4f, 0, translucentStart, 0).color(0x0, 0x0, 0x0, 0x80).next();
        vertexConsumer.vertex(matrix4f, 0, translucentEnd, 0).color(0x0, 0x0, 0x0, 0).next();
        vertexConsumer.vertex(matrix4f, width, translucentEnd, 0).color(0x0, 0x0, 0x0, 0).next();
        vertexConsumer.vertex(matrix4f, width, translucentStart, 0).color(0x0, 0x0, 0x0, 0x80).next();
        drawContext.draw();
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (grabbedHud != null)
                return false;
            isScaling = false;
            tryGrabOverlay(mouseX, mouseY);
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (grabbedHud != null)
                return false;
            isScaling = true;
            tryGrabOverlay(mouseX, mouseY);
            if (!(grabbedHud instanceof JarvisScalable)) {
                tryReleaseOverlay();
                return false;
            }
            JarvisAnchor opposite = grabbedAnchor.getOpposite();
            oppositeCorner = grabbedHud.getAnchor().translate(opposite, grabbedHud.getAbsoluteX(), grabbedHud.getAbsoluteY(), grabbedHud.getEffectiveWidth(), grabbedHud.getEffectiveHeight());
            scalePerDistance = ((JarvisScalable) grabbedHud).getScale() / oppositeCorner.distanceTo(new Point(mouseX, mouseY));
            System.out.printf("Scaling in relation to %s (%s). Scale per distance: %.5f", opposite, oppositeCorner, scalePerDistance);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if ((button == GLFW.GLFW_MOUSE_BUTTON_LEFT && !isScaling)
                || (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && isScaling)) {
            tryReleaseOverlay();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
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
        client.setScreen(lastScreen);
        container.getAllPlugins().forEach(JarvisPlugin::onHudEditorClosed);
    }

    public void tryGrabOverlay(double mouseX, double mouseY) {
        for (JarvisHud hud : huds) {
            if (isOverlayHovered(hud, mouseX, mouseY)) {
                grabbedHud = hud;
                Point inTopLeftSpace = hud.getAnchor().translate(JarvisAnchor.TOP_LEFT, hud.getAbsoluteX(), hud.getAbsoluteY(), hud.getEffectiveWidth(), hud.getEffectiveHeight());
                double offsetX = mouseX - inTopLeftSpace.x();
                double offsetY = mouseY - inTopLeftSpace.y();
                JarvisAnchor closestAnchor = JarvisAnchor.byQuadrant(
                        offsetX < hud.getEffectiveWidth() / 2,
                        offsetY < hud.getEffectiveHeight() / 2
                );
                grabbedHudCoordOffset = closestAnchor.translate(JarvisAnchor.TOP_LEFT, offsetX, offsetY, hud.getEffectiveWidth(), hud.getEffectiveHeight());
                grabbedAnchor = closestAnchor;
                System.out.printf("Anchored to %s : %s%n", grabbedAnchor, grabbedHudCoordOffset);
            }
        }
    }

    public void tryScaleGrabbedOverlay(double mouseX, double mouseY) {
        JarvisHud grabbedHud = this.grabbedHud;
        if (!(grabbedHud instanceof JarvisScalable scalable)) return;
        double distance = new Point(mouseX, mouseY).distanceTo(oppositeCorner);
        double newScale = distance * scalePerDistance;
        if (newScale < 0.2) return;
        scalable.setScale((float) newScale);
        Point position = grabbedAnchor.getOpposite().translate(grabbedHud.getAnchor(), oppositeCorner.x(), oppositeCorner.y(), grabbedHud.getEffectiveWidth(), grabbedHud.getEffectiveHeight());
        grabbedHud.setX(position.x() / (client.getWindow().getScaledWidth() - grabbedHud.getEffectiveWidth()));
        grabbedHud.setY(position.y() / (client.getWindow().getScaledHeight() - grabbedHud.getEffectiveHeight()));
    }

    public void tryMoveGrabbedOverlay(double mouseX, double mouseY) {
        JarvisHud grabbedHud = this.grabbedHud;
        if (grabbedHud == null) return;
        double x = mouseX - grabbedHudCoordOffset.x();
        double y = mouseY - grabbedHudCoordOffset.y();
        Point inTopLeftSpace = grabbedAnchor.translate(JarvisAnchor.TOP_LEFT, x, y, grabbedHud.getEffectiveWidth(), grabbedHud.getEffectiveHeight());
        Point inOriginalSpace = JarvisAnchor.TOP_LEFT.translate(grabbedHud.getAnchor(),
                JarvisUtil.coerce(inTopLeftSpace.x(), 0, client.getWindow().getScaledWidth() - grabbedHud.getEffectiveWidth()),
                JarvisUtil.coerce(inTopLeftSpace.y(), 0, client.getWindow().getScaledHeight() - grabbedHud.getEffectiveHeight()),
                grabbedHud.getEffectiveWidth(),
                grabbedHud.getEffectiveHeight()
        );
        grabbedHud.setX(inOriginalSpace.x() / (client.getWindow().getScaledWidth() - grabbedHud.getEffectiveWidth()));
        grabbedHud.setY(inOriginalSpace.y() / (client.getWindow().getScaledHeight() - grabbedHud.getEffectiveHeight()));
    }

    public void tryReleaseOverlay() {
        grabbedHud = null;
    }
}
