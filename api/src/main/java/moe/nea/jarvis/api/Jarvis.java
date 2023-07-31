package moe.nea.jarvis.api;

import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.NotNull;

import java.util.List;
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
     * @see  #getHudEditor(Screen)
     * @param hudFilter filter the HUDs returned by {@link #getAllHuds()} before displaying them.
     */
    public abstract Screen getHudEditor(Screen lastScreen, BiPredicate<JarvisPlugin, JarvisHud> hudFilter);

}
