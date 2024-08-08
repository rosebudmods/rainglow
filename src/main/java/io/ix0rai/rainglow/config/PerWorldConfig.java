package io.ix0rai.rainglow.config;

import com.mojang.datafixers.util.Either;
import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.SerializedNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.NamingSchemes;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueMap;
import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.client.RainglowClient;
import io.ix0rai.rainglow.data.RainglowMode;
import io.ix0rai.rainglow.mixin.MinecraftServerAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.World;

import java.nio.file.Files;
import java.nio.file.Path;

@SerializedNameConvention(NamingSchemes.SNAKE_CASE)
public class PerWorldConfig extends ReflectiveConfig {
	@Comment("The mode used for each non-local world.")
	@Comment("Note that for singleplayer worlds, the mode is saved in the world folder in the file \"config/rainglow.json\".")
	public final TrackedValue<ValueMap<String>> modesByWorld = this.map("").build();

	public RainglowMode getMode(World world) {
		var saveName = getSaveName(world);
		String mode = null;

		if (saveName.right().isPresent()) {
			mode = modesByWorld.value().get(saveName.right().get());
		} else if (saveName.left().isPresent()) {
			Path path = getJsonPath(saveName.left().get());
			if (Files.exists(path)) {
				try {
					var data = Rainglow.GSON.fromJson(Files.readString(path), RainglowJson.class);
					if (data != null) {
						mode = data.mode;
					}
				} catch (Exception e) {
					Rainglow.LOGGER.error("Failed to load Rainglow config for world " + saveName.left().get(), e);
				}
			} else {
				save(saveName.left().get(), RainglowMode.get(Rainglow.CONFIG.defaultMode.value()));
			}
		}

		if (mode == null) {
			return RainglowMode.get(Rainglow.CONFIG.defaultMode.value());
		} else {
			return RainglowMode.get(mode);
		}
	}

	public void setMode(World world, RainglowMode mode) {
		var saveName = getSaveName(world);
		if (saveName.right().isPresent()) {
			modesByWorld.value().put(saveName.right().get(), mode.getId());
		} else if (saveName.left().isPresent()) {
			save(saveName.left().get(), mode);
		}
	}

	private static void save(Path worldPath, RainglowMode mode) {
		Path path = getJsonPath(worldPath);
		var data = new RainglowJson(mode.getId());

		try {
			Path configPath = getConfigFolderPath(worldPath);
			if (!Files.exists(configPath)) {
				Files.createDirectories(configPath);
			}

			Files.writeString(path, Rainglow.GSON.toJson(data));
		} catch (Exception e) {
			Rainglow.LOGGER.error("Failed to save Rainglow config for world " + worldPath, e);
		}
	}

	private static Path getJsonPath(Path worldPath) {
		return getConfigFolderPath(worldPath).resolve("rainglow.json");
	}

	private static Path getConfigFolderPath(Path worldPath) {
		return worldPath.resolve("config");
	}

	@SuppressWarnings("ConstantConditions")
	private static Either<Path, String> getSaveName(World world) {
		if (!world.isClient) {
			if (world.getServer() instanceof DedicatedServer dedicatedServer) {
				return Either.right(dedicatedServer.getLevelName()); // "world" or something
			} else {
				return Either.left(getWorldPath(world.getServer()));
			}
		} else {
			return RainglowClient.getSaveNameClient();
		}
	}

	@SuppressWarnings("ConstantConditions")
	public static Path getWorldPath(MinecraftServer server) {
		return ((MinecraftServerAccessor) server).getSession().method_54543().path();
	}

	private record RainglowJson(String mode) {

	}
}
