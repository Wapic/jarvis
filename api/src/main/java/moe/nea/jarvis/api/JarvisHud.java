package moe.nea.jarvis.api;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;
import org.joml.Vector2ic;

import java.util.HashSet;
import java.util.Set;

public interface JarvisHud {
    /**
     * The id of this hud. The {@link Identifier#getNamespace()} must be the {@link JarvisPlugin#getModId()}.
     */
    @NotNull Identifier getHudId();

    /**
     * Use {@link #getEffectivePosition} if this hud is anchorable.
     *
     * @return the fixed position of the hud
     */
    @NotNull Vector2ic getPosition();

    /**
     * Sets the hard coded position of this hud.
     */
    void setPosition(Vector2ic position);

    /**
     * If this hud is enabled in its respective mods settings.
     */
    boolean isEnabled();

    /**
     * If this hud is visible (as in currently rendering).
     */
    boolean isVisible();

    /**
     * the current width of this hud (as it has been rendering)
     */
    int getUnscaledWidth();

    /**
     * the current height of this hud (as it has been rendering)
     */
    int getUnscaledHeight();

    /**
     * @return the label of this hud element
     */
    @NotNull Text getLabel();

    /**
     * An interface indicating a {@link JarvisHud} can be scaled.
     */
    interface Scalable extends JarvisHud {
        float getScale();

        void setScale(float scale);
    }

    // <editor-fold desc="Default implementations">
    default int getEffectiveWidth() {
        if (this instanceof Scalable scalable) {
            return (int) (getUnscaledWidth() * scalable.getScale());
        }
        return getUnscaledWidth();
    }

    default Vector2ic getEffectivePosition(Jarvis jarvis) {
        return getEffectivePosition(jarvis, new HashSet<>());
    }

    default Vector2ic getEffectivePosition(Jarvis jarvis, Set<Identifier> visited) {
        return getPosition();
    }

    default int getEffectiveHeight() {
        if (this instanceof Scalable scalable) {
            return (int) (getUnscaledHeight() * scalable.getScale());
        }
        return getUnscaledHeight();
    }

    default void applyTransformations(Jarvis jarvis, Matrix3x2f matrices) {
        var position = getEffectivePosition(jarvis, new HashSet<>());
        matrices.translate(position.x(), position.y());
        if (this instanceof Scalable scalable)
            matrices.scale(scalable.getScale(), scalable.getScale());
    }
    // </editor-fold>
}
