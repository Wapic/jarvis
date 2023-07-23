package moe.nea.jarvis.api;

import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;

public interface JarvisPlugin {
    /**
     * Get all HUDs that are made available by this plugin.
     * This includes all disabled HUDs. The returned list of HUDs
     * should not change at any point during the course of the game.
     */
    default @NotNull @Unmodifiable List<@NotNull JarvisHud> getAllHuds() {
        return Collections.emptyList();
    }

    /**
     * Get all config options that are made available by this plugin.
     * This includes all disabled, and turned off config options.
     */
    default @NotNull @Unmodifiable List<@NotNull JarvisConfigOption> getAllConfigOptions() {
        return Collections.emptyList();
    }

    /**
     * Multiple plugins may be associated with the same mod id.
     *
     * @return the mod id associated with this plugin.
     */
    @NotNull String getModId();

    /**
     * @return the name of this mod (defaults to mod name specified by launcher)
     */
    default @Nullable Text getName() {
        return null;
    }

    default void onInitialize(@NotNull Jarvis jarvis) {
    }

    /**
     * Called when a Hud Editor instance is closed.
     * Useful for saving to file.
     */
    default void onHudEditorClosed() {

    }
}
