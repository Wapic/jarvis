package moe.nea.jarvis.api;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

/**
 * Obtained through {@link JarvisPlugin#onInitialize(Jarvis)}
 */
public abstract class Jarvis {

    /**
     * @return a stream of all plugins registered with jarvis
     */
    public abstract @NotNull Stream<@NotNull JarvisPlugin> getAllPlugins();

    /**
     * @return an index of all jarvis HUDs
     */
    public abstract @Unmodifiable @NotNull Map<@NotNull Identifier, @NotNull JarvisHud> getIndexedHuds();

    public @NotNull Optional<@NotNull JarvisHud> getHud(@NotNull Identifier id) {
        return Optional.ofNullable(getIndexedHuds().get(id));
    }

    /**
     * @return a stream of all HUDs registered by all plugins
     */
    public abstract @NotNull Stream<@NotNull JarvisHud> getAllHuds();

    /**
     * Get a HUD editor screen. You need to manually tell minecraft to display this screen. By default, displays all HUDs
     * according to {@link #getAllHuds()}, that are currently {@link JarvisHud#isEnabled() enabled}.
     *
     * @param lastScreen the screen to return to once the screen is closed.
     */
    public abstract Screen getHudEditor(Screen lastScreen);

    /**
     * @param hudList the list of HUDs to display
     * @see #getHudEditor(Screen)
     */
    public abstract Screen getHudEditor(Screen lastScreen, List<JarvisHud> hudList);

    /**
     * @param hudFilter filter the HUDs returned by {@link #getAllHuds()} before displaying them.
     * @see #getHudEditor(Screen)
     */
    public abstract Screen getHudEditor(Screen lastScreen, BiPredicate<JarvisPlugin, JarvisHud> hudFilter);

}
