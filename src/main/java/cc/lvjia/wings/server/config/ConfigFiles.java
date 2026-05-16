package cc.lvjia.wings.server.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

final class ConfigFiles {
    private static final Logger LOGGER = LogManager.getLogger("WingsConfig");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ConfigFiles() {
    }

    static <T> T load(String fileName, Class<T> type, Supplier<T> defaults, UnaryOperator<T> normalizer) {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(fileName);
        T config = defaults.get();
        JsonElement loadedJson = null;
        boolean shouldSave = !Files.exists(path);

        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                loadedJson = JsonParser.parseReader(reader);
                T loaded = GSON.fromJson(loadedJson, type);
                if (loaded != null) {
                    config = loaded;
                } else {
                    shouldSave = true;
                }
            } catch (Exception ex) {
                LOGGER.warn("Failed to load config '{}'. Rewriting defaults.", path, ex);
                shouldSave = true;
            }
        }

        config = normalizer.apply(config);
        if (!shouldSave && loadedJson != null) {
            shouldSave = !GSON.toJsonTree(config).equals(loadedJson);
        }
        if (shouldSave) {
            save(path, config);
        }
        return config;
    }

    static void save(String fileName, Object config) {
        save(FabricLoader.getInstance().getConfigDir().resolve(fileName), config);
    }

    private static void save(Path path, Object config) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException ex) {
            LOGGER.warn("Failed to save config '{}'.", path, ex);
        }
    }
}
