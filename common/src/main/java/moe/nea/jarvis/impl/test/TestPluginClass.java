package moe.nea.jarvis.impl.test;

import moe.nea.jarvis.api.*;
import moe.nea.jarvis.impl.JarvisUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestPluginClass implements JarvisPlugin {
    JarvisHud hud = new JarvisScalable() {
        @Override
        public float getScale() {
            return (float) JarvisUtil.coerce(scale, 0.1F, 10F);
        }

        @Override
        public void setScale(float newScale) {
            scale = newScale;
        }

        float scale = 1F;
        double x;
        double y;

        @Override
        public double getX() {
            return x;
        }

        @Override
        public void setX(double newX) {
            x = newX;
        }

        @Override
        public double getY() {
            return y;
        }

        @Override
        public void setY(double newY) {
            y = newY;
        }

        @Override
        public @NotNull Text getLabel() {
            return Text.literal("Test HUD Element");
        }

        @Override
        public int getContentHeight() {
            return 10000;
        }

        @Override
        public int getWidth() {
            return 200;
        }

        @Override
        public int getHeight() {
            return 300;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    };

    JarvisConfigOption ofOption(String title, String... description) {
        List<Text> desc = Stream.of(description).map(Text::literal).collect(Collectors.toList());
        return new JarvisConfigOption() {
            @Override
            public @NotNull Text title() {
                return Text.literal(title);
            }

            @Override
            public @NotNull List<@NotNull Text> description() {
                return desc;
            }

            @Override
            public @NotNull Screen jumpTo(@Nullable Screen parentScreen) {
                assert parentScreen != null;
                MinecraftClient.getInstance().player.sendMessage(Text.literal("jumpTo invoked: ").append(title));
                return parentScreen;
            }
        };
    }

    @Override
    public @Nullable Text getName() {
        if (true) return null;
        return Text.literal("Jarvis");
    }

    @Override
    public @NotNull List<@NotNull JarvisConfigOption> getAllConfigOptions() {
        return Arrays.asList(
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Test", "Description Test"),
                ofOption("Alpha", "Description Alpha"),
                ofOption("Beta", "Description Beta"),
                ofOption("Gamma", "Description Gamme", "1", "3", "2")
        );
    }

    @Override
    public @NotNull List<@NotNull JarvisHud> getAllHuds() {
        return Arrays.asList(hud);
    }

    @Override
    public @NotNull String getModId() {
        return JarvisConstants.MODID;
    }
}
