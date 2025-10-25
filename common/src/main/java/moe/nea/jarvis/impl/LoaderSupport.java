package moe.nea.jarvis.impl;

import net.minecraft.text.Text;

import java.util.Optional;

public interface LoaderSupport {
    Optional<Text> getModName(String modId);
}
