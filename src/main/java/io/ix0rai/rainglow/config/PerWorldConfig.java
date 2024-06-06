package io.ix0rai.rainglow.config;

import com.mojang.datafixers.util.Either;
import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.SerializedNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.NamingSchemes;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueMap;
import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowEntity;
import io.ix0rai.rainglow.data.RainglowMode;
import io.ix0rai.rainglow.mixin.MinecraftServerAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@SerializedNameConvention(NamingSchemes.SNAKE_CASE)
public class PerWorldConfig extends ReflectiveConfig {
	@Comment("The mode used for each non-local world.")
	@Comment("Note that for singleplayer worlds, the mode is saved in the world folder in the file \"config/rainglow.json\".")
	public final TrackedValue<ValueMap<ValueMap<String>>> modesByWorld = this.map(ValueMap.builder("").build()).build();

	// todo hot garbage
	public Map<RainglowEntity, RainglowMode> getModes(World world) {
		var saveName = getSaveName(world);
		Either<Map<String, String>, ValueMap<String>> modes = null;

		if (saveName.right().isPresent()) {
			modes = Either.right(modesByWorld.value().get(saveName.right().get()));
		} else if (saveName.left().isPresent()) {
			Path path = getJsonPath(saveName.left().get());
			if (Files.exists(path)) {
				try {
					var data = readJson(path);
					modes = Either.left(data.entities);
				} catch (Exception e) {
					Rainglow.LOGGER.error("Failed to load Rainglow config for world " + saveName.left().get(), e);
				}
			} else {
				save(saveName.left().get(), RainglowMode.get(Rainglow.CONFIG.defaultMode.value()), null);
			}
		}

		if (modes == null) {
			var map = new HashMap<RainglowEntity, RainglowMode>();
			for (RainglowEntity entity : RainglowEntity.values()) {
				map.put(entity, RainglowMode.get(Rainglow.CONFIG.defaultMode.value()));
			}

			return map;
		} else {
			if (modes.left().isPresent()) {
				var map = new HashMap<RainglowEntity, RainglowMode>();
				for (RainglowEntity entity : RainglowEntity.values()) {
					map.put(entity, RainglowMode.get(modes.left().get().get(entity.getId())));
				}

				return map;
			} else {
				var map = new HashMap<RainglowEntity, RainglowMode>();
				for (RainglowEntity entity : RainglowEntity.values()) {
					map.put(entity, RainglowMode.get(modes.right().get().get(entity.getId())));
				}

				return map;
			}
		}
	}

	public RainglowMode getMode(World world, RainglowEntity entity) {
		var saveName = getSaveName(world);
		String mode = null;

		if (saveName.right().isPresent()) {
			mode = modesByWorld.value().get(entity.getId()).get(saveName.right().get());
		} else if (saveName.left().isPresent()) {
			Path path = getJsonPath(saveName.left().get());
			if (Files.exists(path)) {
				try {
					var data = readJson(path);
					mode = data.entities.get(entity.getId());
				} catch (Exception e) {
					Rainglow.LOGGER.error("Failed to load Rainglow config for world " + saveName.left().get(), e);
				}
			} else {
				save(saveName.left().get(), RainglowMode.get(Rainglow.CONFIG.defaultMode.value()), entity);
			}
		}

		if (mode == null) {
			return RainglowMode.get(Rainglow.CONFIG.defaultMode.value());
		} else {
			return RainglowMode.get(mode);
		}
	}

	private static RainglowJson readJson(Path path) {
		if (Files.exists(path)) {
			try {
				return Rainglow.GSON.fromJson(Files.readString(path), RainglowJson.class);
			} catch (Exception e) {
				Rainglow.LOGGER.error("Failed to load Rainglow config for world " + path, e);
			}
		}

		return new RainglowJson(RainglowMode.get(Rainglow.CONFIG.defaultMode.value()));
	}

	public void setMode(World world, RainglowMode mode, @Nullable RainglowEntity entity) {
		Consumer<String> setter = (id) -> {
			var saveName = getSaveName(world);
			if (saveName.right().isPresent()) {
				modesByWorld.value().get(id).put(saveName.right().get(), mode.getId());
			} else if (saveName.left().isPresent()) {
				save(saveName.left().get(), mode, entity);
			}
		};

		if (entity != null) {
			setter.accept(entity.getId());
		} else {
			for (RainglowEntity e : RainglowEntity.values()) {
				setter.accept(e.getId());
			}
		}
	}

	private static void save(Path worldPath, RainglowMode mode, @Nullable RainglowEntity entity) {
		Path path = getJsonPath(worldPath);
		RainglowJson data;

		if (Files.exists(path) && entity != null) {
			data = readJson(path);
			data.entities.put(entity.getId(), mode.getId());
		} else {
			data = new RainglowJson(mode);
		}

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
			if (MinecraftClient.getInstance().isInSingleplayer()) {
				return Either.left(getWorldPath(MinecraftClient.getInstance().getServer()));
			} else {
				return Either.right(MinecraftClient.getInstance().getCurrentServerEntry().address);
			}
		}
	}

	@SuppressWarnings("ConstantConditions")
	private static Path getWorldPath(MinecraftServer server) {
		return ((MinecraftServerAccessor) server).getSession().method_54543().path();
	}

	private record RainglowJson(Map<String, String> entities) {
		public RainglowJson(RainglowMode mode) {
			this(new HashMap<>());
			for (RainglowEntity entity : RainglowEntity.values()) {
				this.entities.put(entity.getId(), mode.getId());
			}
		}
	}
}
