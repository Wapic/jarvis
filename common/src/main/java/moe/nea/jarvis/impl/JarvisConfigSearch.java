package moe.nea.jarvis.impl;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class JarvisConfigSearch extends Screen {
    private final JarvisContainer container;
    private final List<ConfigOptionWithCustody> allOptions;
    private final Screen parentScreen;
    private List<ConfigOptionWithCustody> filteredOptions = new ArrayList<>();
    private TextFieldWidget searchField;
    private String searchQuery = "";
    private int searchFieldWidth;

    public JarvisConfigSearch(JarvisContainer container, Screen parentScreen, List<ConfigOptionWithCustody> allOptions) {
        super(Text.translatable("jarvis.configlist"));
        this.container = container;
        this.allOptions = allOptions;
        this.parentScreen = parentScreen;
        updateSearchResults("");
    }

    double scroll;

    @Override
    protected void init() {
        assert client != null;
        super.init();
        searchFieldWidth = Math.min(400, width / 3);
        addDrawableChild(searchField = new TextFieldWidget(client.textRenderer, width / 2 - searchFieldWidth / 2,
            10, searchFieldWidth, 18, Text.translatable("jarvis.configlist.suggestion")));
        searchField.setText(searchQuery);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        assert client != null;
        super.render(context, mouseX, mouseY, delta);
        context.fill(0, 0, width, height, 0x50000000);
        context.enableScissor(0, 35, width, height);
        context.getMatrices().pushMatrix();

        int left = width / 2 - searchFieldWidth / 2;
        context.getMatrices().translate(left, 35F - (float) scroll);
        mouseY -= 35 - scroll;
        mouseX -= left;
        for (ConfigOptionWithCustody filteredOption : filteredOptions) {
            int height = 15 + filteredOption.option().description().size() * 10;
            if (0 <= mouseX && mouseX < searchFieldWidth &&
                0 <= mouseY && mouseY < height) {
                context.fill(0, 0, searchFieldWidth, height, 0x50A0A0A0);
            }

            context.drawText(client.textRenderer, Text.literal("").append(container.getModName(filteredOption.plugin())).append(Text.literal(" > ")).append(filteredOption.option().title()), 2, 2, -1, false);
            int offset = 15;
            for (Text descriptionLine : filteredOption.option().description()) {
                context.drawText(client.textRenderer, descriptionLine, 2, offset, 0xFF808080, true);
                offset += 10;
            }
            mouseY -= height;
            context.getMatrices().translate(0, height);
        }

        context.getMatrices().popMatrix();
        context.disableScissor();
    }

    @Override
    public void close() {
        assert client != null;
        client.setScreen(parentScreen);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }
        if (35 <= mouseY && mouseY < height && width / 2 - searchFieldWidth / 2 <= mouseX
            && mouseX < width / 2 + searchFieldWidth / 2) {
            scroll(verticalAmount);
            return true;
        }
        return false;
    }

    public void scroll(double amount) {
        int usedHeight = -height + 35;
        for (ConfigOptionWithCustody filteredOption : filteredOptions) {
            usedHeight += filteredOption.option().description().size() * 10 + 15;
        }
        scroll = JarvisUtil.coerce(scroll + amount * -5, 0, usedHeight);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        assert client != null;
        if (super.mouseClicked(click, doubled)) return true;
        double mouseX = click.x();
        double mouseY = click.y();
        mouseX -= 35 - scroll;
        int left = width / 2 - searchFieldWidth / 2;
        mouseX -= left;
        for (ConfigOptionWithCustody filteredOption : filteredOptions) {
            int height = 15 + filteredOption.option().description().size() * 10;
            if (0 <= mouseX && mouseX < searchFieldWidth &&
                0 <= mouseY && mouseY < height) {
                client.setScreen(filteredOption.option().jumpTo(this));
                return true;
            }
            mouseY -= height;
        }
        return false;
    }

    @Override
    public boolean charTyped(CharInput input) {
        String before = searchField.getText();
        searchField.setFocused(true);
        boolean ret = super.charTyped(input);
        String after = searchField.getText();
        if (!Objects.equals(before, after)) {
            updateSearchResults(after);
        }
        return ret;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        String before = searchField.getText();
        boolean ret = super.keyPressed(input);
        String after = searchField.getText();
        if (!Objects.equals(before, after)) {
            updateSearchResults(after);
        }
        return ret;
    }

    private void updateSearchResults(String searchTerm) {
        searchQuery = searchTerm;
        String[] groups = searchTerm.toLowerCase(Locale.ROOT).split("\\|");
        String[][] filter = new String[groups.length][];
        for (int i = 0; i < groups.length; i++) {
            String[] group = groups[i].trim().split(" +");
            for (int j = 0; j < group.length; j++) {
                group[j] = group[j].toLowerCase(Locale.ROOT);
            }
            filter[i] = group;
        }
        filteredOptions = allOptions.stream()
            .filter(it -> filterOption(it, filter))
            .collect(Collectors.toList());
        scroll(0);
    }

    private List<String> getTextPieces(ConfigOptionWithCustody withCustody) {
        ArrayList<String> objects = new ArrayList<>();
        var option = withCustody.option();
        objects.add(option.title().getString().toLowerCase(Locale.ROOT));
        Text category = option.category();
        if (category != null) {
            objects.add(category.getString().toLowerCase(Locale.ROOT));
        }
        objects.add(container.getModName(withCustody.plugin()).getString().toLowerCase(Locale.ROOT));
        for (Text text : option.description()) {
            objects.add(text.getString().toLowerCase(Locale.ROOT));
        }
        return objects;
    }

    private boolean filterOption(ConfigOptionWithCustody configOptionWithCustody, String[][] searchTerm) {
        List<String> textPieces = getTextPieces(configOptionWithCustody);
        for (String[] group : searchTerm) {
            boolean matched = true;
            for (String part : group) {
                boolean partMatched = configOptionWithCustody.option().match(part);
                if (!partMatched)
                    for (String textPiece : textPieces) {
                        if (textPiece.contains(part)) {
                            partMatched = true;
                            break;
                        }
                    }
                if (!partMatched) {
                    matched = false;
                    break;
                }
            }
            if (matched) return true;
        }
        return false;
    }
}
