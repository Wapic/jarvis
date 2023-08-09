package moe.nea.jarvis.forge;

import moe.nea.jarvis.api.JarvisPlugin;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;

public class ConnectorHook {

    public static List<JarvisPlugin> getConnectedPlugins() {
        return FabricLoader.getInstance().getEntrypoints("jarvis", JarvisPlugin.class);
    }

    public static Optional<Text> getConnectedModName(String modid) {
        return FabricLoader.getInstance().getModContainer(modid).map(it -> Text.literal(it.getMetadata().getName()));
    }
}
