package moe.nea.jarvis.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import moe.nea.jarvis.api.Jarvis;
import moe.nea.jarvis.api.JarvisHud;
import moe.nea.jarvis.api.JarvisPlugin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JarvisContainer extends Jarvis {
    public List<JarvisPlugin> plugins = new ArrayList<>();
    public LoaderSupport loaderSupport;

    public LoaderSupport getLoaderSupport() {
        return loaderSupport;
    }

    public static JarvisContainer init(LoaderSupport loaderSupport) {
        JarvisContainer jarvisContainer = new JarvisContainer();
        jarvisContainer.loaderSupport = loaderSupport;
        return jarvisContainer;
    }


    @Override
    public @NotNull Stream<@NotNull JarvisPlugin> getAllPlugins() {
        return plugins.stream();
    }

    @Override
    public @NotNull Stream<@NotNull JarvisHud> getAllHuds() {
        return plugins.stream().flatMap(it -> it.getAllHuds().stream());
    }

    @Override
    public @NotNull JarvisHudEditor getHudEditor(@Nullable Screen lastScreen) {
        return getHudEditor(lastScreen, getAllHuds());
    }

    @Override
    public @NotNull JarvisHudEditor getHudEditor(@Nullable Screen lastScreen, @NotNull List<@NotNull JarvisHud> hudList) {
        return new JarvisHudEditor(lastScreen, hudList, this);
    }

    @Override
    public @NotNull JarvisHudEditor getHudEditor(@Nullable Screen lastScreen, @NotNull BiPredicate<@NotNull JarvisPlugin, @NotNull JarvisHud> hudFilter) {
        return getHudEditor(lastScreen, getAllPlugins().flatMap(plugin -> plugin.getAllHuds().stream().filter(it -> hudFilter.test(plugin, it))));
    }

    private @NotNull JarvisHudEditor getHudEditor(@Nullable Screen lastScreen, @NotNull Stream<@NotNull JarvisHud> rStream) {
        return getHudEditor(lastScreen, rStream.collect(Collectors.toList()));
    }

    public void finishLoading() {
        plugins.forEach(it -> it.onInitialize(this));
    }

    public <S> void registerCommands(CommandDispatcher<S> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<S>literal("jarvis")
                .then(LiteralArgumentBuilder.<S>literal("gui")
                        .executes(context -> {
                            MinecraftClient.getInstance().send(() -> MinecraftClient.getInstance().setScreen(this.getHudEditor(null)));
                            return 0;
                        }))
                .then(LiteralArgumentBuilder.<S>literal("options")
                        .executes(context -> {
                            MinecraftClient.getInstance().send(() -> MinecraftClient.getInstance().setScreen(
                                    new JarvisConfigSearch(this, null, getAllPlugins().flatMap(it -> it.getAllConfigOptions().stream()
                                            .map(opt -> new ConfigOptionWithCustody(it, opt))).collect(Collectors.toList()))));
                            return 0;
                        })));
    }

    public Text getModName(JarvisPlugin plugin) {
        Text name = plugin.getName();
        if (name != null) return name;
        return loaderSupport.getModName(plugin.getModId()).get();
    }
}
