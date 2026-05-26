package cc.lvjia.wings.server.config;

import cc.lvjia.wings.WingsMod;
import org.jspecify.annotations.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings("null")
public final class WingsItemsConfig {
    public static final @NonNull ConfigWingSettings ANGEL = new ConfigWingSettings(WingsMod.Names.ANGEL);
    public static final @NonNull ConfigWingSettings PARROT = new ConfigWingSettings(WingsMod.Names.PARROT);
    public static final @NonNull ConfigWingSettings SLIME = new ConfigWingSettings(WingsMod.Names.SLIME);
    public static final @NonNull ConfigWingSettings BLUE_BUTTERFLY = new ConfigWingSettings(WingsMod.Names.BLUE_BUTTERFLY);
    public static final @NonNull ConfigWingSettings MONARCH_BUTTERFLY = new ConfigWingSettings(WingsMod.Names.MONARCH_BUTTERFLY);
    public static final @NonNull ConfigWingSettings FIRE = new ConfigWingSettings(WingsMod.Names.FIRE);
    public static final @NonNull ConfigWingSettings BAT = new ConfigWingSettings(WingsMod.Names.BAT);
    public static final @NonNull ConfigWingSettings FAIRY = new ConfigWingSettings(WingsMod.Names.FAIRY);
    public static final @NonNull ConfigWingSettings EVIL = new ConfigWingSettings(WingsMod.Names.EVIL);
    public static final @NonNull ConfigWingSettings DRAGON = new ConfigWingSettings(WingsMod.Names.DRAGON);
    public static final @NonNull ConfigWingSettings LVJIA_SUPER = new ConfigWingSettings(WingsMod.Names.LVJIA_SUPER);

    private WingsItemsConfig() {
    }

    public static void validate() {
        Data data = ConfigFiles.load("wings-items.json", Data.class, WingsItemsConfig::defaultData, Data::normalize);
        all().forEach(settings -> settings.apply(data.wings.get(settings.getKey().getPath())));
    }

    private static Data defaultData() {
        Data data = new Data();
        all().forEach(settings -> data.wings.put(settings.getKey().getPath(), settings.defaultData()));
        return data;
    }

    private static Stream<ConfigWingSettings> all() {
        return Stream.of(ANGEL, PARROT, SLIME, BLUE_BUTTERFLY, MONARCH_BUTTERFLY, FIRE, BAT, FAIRY, EVIL, DRAGON,
                LVJIA_SUPER);
    }

    public static final class Data {
        public Map<String, ConfigWingSettings.Data> wings = new LinkedHashMap<>();

        Data normalize() {
            if (this.wings == null) {
                this.wings = new LinkedHashMap<>();
            }
            all().forEach(settings -> this.wings.compute(settings.getKey().getPath(), (key, value) -> {
                ConfigWingSettings.Data normalized = value != null ? value : settings.defaultData();
                return normalized.normalize();
            }));
            return this;
        }
    }
}
