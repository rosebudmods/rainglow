package io.ix0rai.rainglow.data;

import net.minecraft.entity.passive.PassiveEntity;

public class GlowSquidEntityData extends PassiveEntity.PassiveData {
	private final RainglowColour colour;

	public GlowSquidEntityData(RainglowColour colour) {
		// copied from SquidEntity#initialize. as far as i can tell we have to duplicate this constant
		super(0.05F);
		this.colour = colour;
	}

	public  RainglowColour getColour() {
		return colour;
	}
}
