package moe.nea.jarvis.impl.test;

import moe.nea.jarvis.api.JarvisConfigOption;
import moe.nea.jarvis.api.JarvisConstants;
import moe.nea.jarvis.api.JarvisHud;
import moe.nea.jarvis.api.JarvisPlugin;
import moe.nea.jarvis.impl.JarvisUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestPluginClass implements JarvisPlugin {
    JarvisHud hud = new JarvisHud.Scalable() {
        @Override
        public float getScale() {
            return (float) JarvisUtil.coerce(scale, 0.1F, 10F);
        }

        @Override
        public void setScale(float newScale) {
            scale = newScale;
        }

        float scale = 1F;
        Vector2i position = new Vector2i();

        @Override
        public @NotNull Text getLabel() {
            return Text.literal("Test HUD Element");
        }

        @Override
        public @NotNull Identifier getHudId() {
            return Identifier.of(JarvisConstants.MODID, "test_hud");
        }

        @Override
        public @NotNull Vector2ic getPosition() {
            return position;
        }

        @Override
        public void setPosition(Vector2ic position) {
            this.position = new Vector2i(position);
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public boolean isVisible() {
            return true;
        }

        @Override
        public int getUnscaledWidth() {
            return 200;
        }

        @Override
        public int getUnscaledHeight() {
            return 300;
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
                MinecraftClient.getInstance().player.sendMessage(Text.literal("jumpTo invoked: ").append(title), false);
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
        return List.of(hud);
    }

    @Override
    public @NotNull String getModId() {
        return JarvisConstants.MODID;
    }
}
