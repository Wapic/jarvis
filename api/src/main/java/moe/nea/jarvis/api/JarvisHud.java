package moe.nea.jarvis.api;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

/**
 * A Jarvis HUD interface.
 */
public interface JarvisHud {
    /**
     * Check if this hud is enabled / turned on. It might still not render, since it is scoped to certain areas,
     * or only visible while doing certain actions.
     *
     * @return if this hud is currently enabled
     * @see #isActive()
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * @return if this hud is enabled and currently rendering
     * @see #isEnabled()
     */
    default boolean isActive() {
        return isEnabled();
    }

    /**
     * @return the current x position of this hud elements {@link #getAnchor anchor}, as a percentage of the {@link Window#getScaledWidth()}
     */
    double getX();

    /**
     * Set the x position of this hud elements by its {@link #getAnchor anchor}, as a percentage of the {@link Window#getScaledWidth()}
     *
     * @param newX the new x position
     */
    void setX(double newX);

    /**
     * @return the anchor point of this hud element
     */
    default @NotNull JarvisAnchor getAnchor() {
        return JarvisAnchor.TOP_LEFT;
    }

    /**
     * @return the current y position of this hud elements {@link #getAnchor anchor}, as a percentage of the {@link Window#getScaledHeight()}
     */
    double getY();

    /**
     * Set the y position of this hud elements by its {@link #getAnchor anchor}, as a percentage of the {@link Window#getScaledHeight()}
     *
     * @param newY the new y position
     */
    void setY(double newY);

    /**
     * @return the absolute x position in pixels of the {@link #getAnchor() anchor}.
     */
    default int getAbsoluteX() {
        return (int) (getX() * (MinecraftClient.getInstance().getWindow().getScaledWidth() - getEffectiveWidth()));
    }

    /**
     * @return the absolute y position in pixels of the {@link #getAnchor() anchor}.
     */
    default int getAbsoluteY() {
        return (int) (getY() * (MinecraftClient.getInstance().getWindow().getScaledHeight() - getEffectiveHeight()));
    }

    /**
     * @return the label of this hud element
     */
    @NotNull Text getLabel();

    /**
     * @return the width of this hud element in pixels, before any applied scaling
     */
    int getWidth();

    /**
     * @return the height of this hud element in pixels, before any applied scaling
     */
    int getHeight();

    /**
     * Get the height of the content. This function can return a dynamic size that may exceed the actual bounds of {@link #getHeight()}.
     *
     * @return the height of the content of this hud element in pixels
     */
    default int getContentHeight() {
        return getHeight();
    }

    /**
     * This function describes how this hud element should fade out it's background along the Y axis in the hud editor.
     * A value of {@code 0.5} for example indicates that after 50% of the {@link #getHeight()} the background slowly
     * fades out towards 0 opacity. A value of {@code 1} or higher disables the fade out altogether.
     * By default, this is enabled if the {@link #getContentHeight()} exceeds the {@link #getHeight()}
     */
    default float getFadeOutPercentage() {
        if (getContentHeight() > getHeight()) {
            return 0.7F;
        }
        return 1F;
    }

    /**
     * @return the width of this hud element with local scaling applied
     */
    default double getEffectiveWidth() {
        return getWidth();
    }

    /**
     * @return the height of this hud element with local scaling applied
     */
    default double getEffectiveHeight() {
        return getHeight();
    }

    /**
     * Transform the matrix stack towards the anchor. Does not push the stack.
     *
     * @param matrixStack the matrix stack to transform
     */
    default void applyTransformations(@NotNull MatrixStack matrixStack) {
        matrixStack.translate(getAbsoluteX(), getAbsoluteY(), 0);
        if (this instanceof JarvisScalable scalable) {
            matrixStack.scale(scalable.getScale(), scalable.getScale(), 1);
        }
    }

}
