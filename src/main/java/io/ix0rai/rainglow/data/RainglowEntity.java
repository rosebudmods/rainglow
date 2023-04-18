package io.ix0rai.rainglow.data;

public enum RainglowEntity {
    GLOW_SQUID("glow_squid"),
    ALLAY("allay"),
    SLIME("slime");

    private final String id;

    RainglowEntity(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
