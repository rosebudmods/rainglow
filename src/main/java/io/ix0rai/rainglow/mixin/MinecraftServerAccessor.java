package io.ix0rai.rainglow.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.WorldSaveStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {
	@Accessor
	WorldSaveStorage.Session getSession();
}
