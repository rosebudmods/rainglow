package io.ix0rai.rainglow;

import net.minecraft.util.Identifier;

public record SquidColour(String id, Identifier texture, RGB passiveParticleRgb, RGB altPassiveParticleRgb, RGB inkRgb) {
    public static final SquidColour BLUE = new SquidColour(
            "blue",
            new Identifier("textures/entity/squid/glow_squid.png"),
            new RGB(0.6F, 1.0F, 0.8F),
            new RGB(0.08F, 0.4F, 0.4F),
            new RGB(204, 31, 102)
    );

    public static final SquidColour RED = new SquidColour(
            "red",
            new Identifier("textures/entity/squid/red.png"),
            new RGB(1.0F, 1.0F, 0.8F),
            new RGB(1.0F, 0.4F, 0.4F),
            new RGB(200, 0, 0)
    );

    public static final SquidColour GREEN = new SquidColour(
            "green",
            new Identifier("textures/entity/squid/green.png"),
            new RGB(0.6F, 1.0F, 0.8F),
            new RGB(0.08F, 1.0F, 0.4F),
            new RGB(0, 200, 0)
    );

    public static final SquidColour PINK = new SquidColour(
            "pink",
            new Identifier("textures/entity/squid/pink.png"),
            new RGB(1.0F, 0.6F, 0.8F),
            new RGB(1.0F, 0.08F, 0.4F),
            new RGB(200, 0, 200)
    );
}
