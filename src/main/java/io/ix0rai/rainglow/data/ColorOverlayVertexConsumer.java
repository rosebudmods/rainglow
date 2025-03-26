package io.ix0rai.rainglow.data;

import com.mojang.blaze3d.vertex.VertexConsumer;

public class ColorOverlayVertexConsumer implements VertexConsumer {
    private final VertexConsumer base;
    private final float r;
    private final float g;
    private final float b;

    public ColorOverlayVertexConsumer(VertexConsumer base, float r, float g, float b) {
        this.base = base;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    // Vertex
    @Override
    public VertexConsumer xyz(float f, float g, float h) {
        return base.xyz(f, g, h);
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        // Adjust Alpha value to stop my eyes from blowing out
        // Change the float value to control transparency (For outer slime layer)
        int adjustedAlpha = (int)(alpha * 0.9f);

        return base.color(Math.min(255, (int)(red * r)), Math.min(255, (int)(green * g)), Math.min(255, (int)(blue * b)), adjustedAlpha);
    }

    // Texture
    @Override
    public VertexConsumer uv0(float f, float g) {
        return base.uv0(f, g);
    }

    // Overlay
    @Override
    public VertexConsumer uv1(int i, int j) {
        return base.uv1(i, j);
    }

    // Light
    @Override
    public VertexConsumer uv2(int i, int j) {
        return base.uv2(i, j);
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return base.normal(x, y, z);
    }
}
