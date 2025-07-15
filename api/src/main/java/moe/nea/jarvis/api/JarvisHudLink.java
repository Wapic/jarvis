package moe.nea.jarvis.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * Anchors a {@link JarvisHud} to another HUD.
 *
 * @param parent       the identifier of the hud this hud is parented to.
 * @param localAnchor  the corner of the anchored hud that is linked to {@link #parentAnchor}
 * @param parentAnchor the corner of the parent hud that is linked to {@link #localAnchor}
 */
public record JarvisHudLink(
    @NotNull Identifier parent,
    @NotNull JarvisAnchor localAnchor,
    @NotNull JarvisAnchor parentAnchor
) {

    public static final Codec<JarvisHudLink> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Identifier.CODEC.fieldOf("parent").forGetter(JarvisHudLink::parent),
            JarvisAnchor.CODEC.fieldOf("localAnchor").forGetter(JarvisHudLink::localAnchor),
            JarvisAnchor.CODEC.fieldOf("parentAnchor").forGetter(JarvisHudLink::parentAnchor)
        ).apply(instance, JarvisHudLink::new));
}
