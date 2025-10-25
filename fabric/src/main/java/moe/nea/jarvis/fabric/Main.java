package moe.nea.jarvis.fabric;

import moe.nea.jarvis.api.JarvisPlugin;
import moe.nea.jarvis.impl.JarvisContainer;
import moe.nea.jarvis.impl.JarvisUtil;
import moe.nea.jarvis.impl.test.TestPluginClass;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;

import java.util.List;

public class Main implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        List<JarvisPlugin> jarvisPlugins = FabricLoader.getInstance().getEntrypoints("jarvis", JarvisPlugin.class);
        JarvisContainer container = JarvisContainer.init(modId -> FabricLoader.getInstance().getModContainer(modId).map(it -> Text.literal(it.getMetadata().getName())));
        container.plugins.addAll(jarvisPlugins);
        var hudKeybind = KeyBindingHelper.registerKeyBinding(container.hudKeyBinding);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (hudKeybind.wasPressed()) {
                container.hudKeyBindingPressed();
            }
        });
        if (!JarvisUtil.isTest)
            container.plugins.removeIf(it -> it instanceof TestPluginClass);
        container.finishLoading();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            container.registerCommands(dispatcher);
        });
    }
}
