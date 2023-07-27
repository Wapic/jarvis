package moe.nea.jarvis.forge;

import moe.nea.jarvis.api.JarvisConstants;
import moe.nea.jarvis.api.JarvisPlugin;
import moe.nea.jarvis.impl.JarvisContainer;
import moe.nea.jarvis.impl.JarvisUtil;
import moe.nea.jarvis.impl.LoaderSupport;
import moe.nea.jarvis.impl.test.TestPluginClass;
import net.minecraft.text.Text;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Optional;

@Mod(JarvisConstants.MODID)
public class Main {
    JarvisContainer jarvisContainer = JarvisContainer.init(new LoaderSupport() {
        @Override
        public Optional<Text> getModName(String modid) {
            return Optional.of(Text.literal(FMLLoader.getLoadingModList().getModFileById(modid).getMods().stream().findFirst().get().getDisplayName()));
        }
    });

    public Main() {
        if (FMLEnvironment.dist.isClient()) {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onDequeue);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onEnqueue);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onComplete);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterKeyBindings);
            MinecraftForge.EVENT_BUS.register(this);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            while (jarvisContainer.hudKeyBinding.wasPressed()) {
                jarvisContainer.hudKeyBindingPressed();
            }
        }
    }

    public void onRegisterKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(jarvisContainer.hudKeyBinding);
    }

    public void onEnqueue(InterModEnqueueEvent event) {
        if (JarvisUtil.isTest)
            InterModComms.sendTo(JarvisConstants.MODID, JarvisConstants.IMC_REGISTER_PLUGIN, () -> TestPluginClass.class);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterClientCommandsEvent event) {
        jarvisContainer.registerCommands(event.getDispatcher());
    }

    public void onComplete(FMLLoadCompleteEvent event) {
        jarvisContainer.finishLoading();
    }

    public void onDequeue(InterModProcessEvent event) {
        InterModComms.getMessages(JarvisConstants.MODID).forEach(mes -> {
            if (!Objects.equals(JarvisConstants.IMC_REGISTER_PLUGIN, mes.method())) {
                throw new IllegalArgumentException("Invalid message to jarvis: " + mes + ". Unknown method name." + " Contact the authors of " + mes.senderModId() + " before contacting Jarvis.");
            }
            Object argument = mes.messageSupplier().get();
            if (!(argument instanceof Class<?> clazz)) {
                throw new IllegalArgumentException("Invalid message to jarvis: " + mes + ". " + argument + " should be a class" + " Contact the authors of " + mes.senderModId() + " before contacting Jarvis.");
            }
            if (!JarvisPlugin.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Invalid message to jarvis: " + mes + ". " + argument + " should be subclass of JarvisPlugin" + " Contact the authors of " + mes.senderModId() + " before contacting Jarvis.");
            }

            JarvisPlugin jarvisPlugin;
            try {
                Constructor<?> declaredConstructor = clazz.getConstructor();
                jarvisPlugin = (JarvisPlugin) declaredConstructor.newInstance();
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Invalid message to jarvis: " + mes + ". No public default constructor." + " Contact the authors of " + mes.senderModId() + " before contacting Jarvis.");
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new IllegalArgumentException("Invalid message to jarvis: " + mes + ". Uncaught exception during constructor invocation." + " Contact the authors of " + mes.senderModId() + " before contacting Jarvis.", e);
            }
            jarvisContainer.plugins.add(jarvisPlugin);
        });
    }

}
