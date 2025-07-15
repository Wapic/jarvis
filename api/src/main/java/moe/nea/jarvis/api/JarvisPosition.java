package moe.nea.jarvis.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.Optional;
import java.util.Set;

/**
 * @param link     an anchoring to another hud element. if that hud element is not present, fall back to {@link #position}
 * @param position The position of this hud, relative to the top left of the screen, is overriden by {@link #link}
 */
public record JarvisPosition(
    Optional<JarvisHudLink> link,
    Vector2i position
) {

    private static final Codec<Vector2i> VECTOR_2I_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("x").forGetter(Vector2i::x),
            Codec.INT.fieldOf("y").forGetter(Vector2i::y)
        ).apply(instance, Vector2i::new));
    public static final Codec<JarvisPosition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            JarvisHudLink.CODEC.optionalFieldOf("link").forGetter(JarvisPosition::link),
            VECTOR_2I_CODEC.fieldOf("position").forGetter(JarvisPosition::position)
        ).apply(instance, JarvisPosition::new));

    public Vector2ic resolve(Jarvis jarvis, Set<Identifier> set) {
        return this.link.flatMap(link -> {
            set.add(link.parent());
            return jarvis.getHud(link.parent())
                .map(it -> it.getEffectivePosition(jarvis, set));
        }).orElse(position);
    }
}
