package cc.lvjia.wings;

import cc.lvjia.wings.server.apparatus.BuffedFlightApparatus;
import cc.lvjia.wings.server.apparatus.FlightApparatus;
import cc.lvjia.wings.server.apparatus.SimpleFlightApparatus;
import cc.lvjia.wings.server.config.WingsItemsConfig;
import cc.lvjia.wings.server.flight.Flight;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

// 跨加载器共享核心：mod ID、资源定位、翅膀注册与默认翅膀清单
public final class WingsCore {
    public static final String ID = "wings";
    // 自定义注册表键，两加载器共享同一个 ResourceKey
    public static final @NonNull ResourceKey<Registry<FlightApparatus>> WINGS_KEY = Objects.requireNonNull(
            ResourceKey.createRegistryKey(locate("wings")), "wings registry key");

    private WingsCore() {
    }

    // 生成 mod 命名空间的 Identifier，参数非法时抛异常而非返回 null
    public static @NonNull Identifier locate(@NonNull String name) {
        Identifier id = Identifier.tryBuild(ID, name);
        if (id == null) {
            throw new IllegalArgumentException("Invalid resource path: " + name);
        }
        return id;
    }

    // 注册所有默认翅膀，通过 WingRegistrar 回调解耦具体注册 API（Fabric Registry / NeoForge DeferredRegister）
    public static @NonNull WingSet registerWings(@NonNull WingRegistrar registrar) {
        @NonNull FlightApparatus none = registrar.register(Names.NONE, FlightApparatus.NONE);
        @NonNull FlightApparatus wingless = registrar.register(Names.WINGLESS, wingless());
        @NonNull FlightApparatus angel = registrar.register(Names.ANGEL, new SimpleFlightApparatus(WingsItemsConfig.ANGEL));
        @NonNull FlightApparatus parrot = registrar.register(Names.PARROT, new SimpleFlightApparatus(WingsItemsConfig.PARROT));
        @NonNull FlightApparatus bat = registrar.register(Names.BAT, new SimpleFlightApparatus(WingsItemsConfig.BAT));
        @NonNull FlightApparatus blueButterfly = registrar.register(Names.BLUE_BUTTERFLY,
                new SimpleFlightApparatus(WingsItemsConfig.BLUE_BUTTERFLY));
        @NonNull FlightApparatus dragon = registrar.register(Names.DRAGON, new SimpleFlightApparatus(WingsItemsConfig.DRAGON));
        @NonNull FlightApparatus evil = registrar.register(Names.EVIL, new SimpleFlightApparatus(WingsItemsConfig.EVIL));
        @NonNull FlightApparatus fairy = registrar.register(Names.FAIRY, new SimpleFlightApparatus(WingsItemsConfig.FAIRY));
        @NonNull FlightApparatus monarchButterfly = registrar.register(Names.MONARCH_BUTTERFLY,
                new SimpleFlightApparatus(WingsItemsConfig.MONARCH_BUTTERFLY));
        @NonNull FlightApparatus slime = registrar.register(Names.SLIME, new SimpleFlightApparatus(WingsItemsConfig.SLIME));
        @NonNull FlightApparatus fire = registrar.register(Names.FIRE, new SimpleFlightApparatus(WingsItemsConfig.FIRE));
        @NonNull FlightApparatus lvjiaSuper = registrar.register(Names.LVJIA_SUPER,
                new BuffedFlightApparatus(WingsItemsConfig.LVJIA_SUPER,
                        BuffedFlightApparatus.EffectSettings.of(MobEffects.RESISTANCE, 2, 40, 40),
                        BuffedFlightApparatus.EffectSettings.of(MobEffects.JUMP_BOOST, 1, 40, 40)));
        return new WingSet(none, wingless, angel, parrot, bat, blueButterfly, dragon, evil, fairy,
                monarchButterfly, slime, fire, lvjiaSuper);
    }

    private static @NonNull FlightApparatus wingless() {
        return new FlightApparatus() {
            @Override
            public void onFlight(@NonNull Player player, @NonNull Vec3 direction) {
                FlightApparatus.NONE.onFlight(player, direction);
            }

            @Override
            public void onLanding(@NonNull Player player, @NonNull Vec3 direction) {
                FlightApparatus.NONE.onLanding(player, direction);
            }

            @Override
            public boolean isUsable(@NonNull Player player) {
                return FlightApparatus.NONE.isUsable(player);
            }

            @Override
            public boolean isLandable(@NonNull Player player) {
                return FlightApparatus.NONE.isLandable(player);
            }

            @Override
            public FlightApparatus.@NonNull FlightState createState(@NonNull Flight flight) {
                return FlightApparatus.NONE.createState(flight);
            }
        };
    }

    // 注册回调接口，解耦具体 Registry API
    @FunctionalInterface
    public interface WingRegistrar {
        @NonNull FlightApparatus register(@NonNull Identifier id, @NonNull FlightApparatus wing);
    }

    // 所有翅膀实例的定型封装，编译期确保不遗漏
    public record WingSet(
            @NonNull FlightApparatus none,
            @NonNull FlightApparatus wingless,
            @NonNull FlightApparatus angel,
            @NonNull FlightApparatus parrot,
            @NonNull FlightApparatus bat,
            @NonNull FlightApparatus blueButterfly,
            @NonNull FlightApparatus dragon,
            @NonNull FlightApparatus evil,
            @NonNull FlightApparatus fairy,
            @NonNull FlightApparatus monarchButterfly,
            @NonNull FlightApparatus slime,
            @NonNull FlightApparatus fire,
            @NonNull FlightApparatus lvjiaSuper) {
    }

    // 所有翅膀的 Identifier 常量，集中管理避免拼写错误
    public static final class Names {
        public static final @NonNull Identifier
                NONE = create("none"),
                WINGLESS = create("wingless"),
                ANGEL = create("angel_wings"),
                PARROT = create("parrot_wings"),
                SLIME = create("slime_wings"),
                BLUE_BUTTERFLY = create("blue_butterfly_wings"),
                MONARCH_BUTTERFLY = create("monarch_butterfly_wings"),
                FIRE = create("fire_wings"),
                BAT = create("bat_wings"),
                FAIRY = create("fairy_wings"),
                EVIL = create("evil_wings"),
                DRAGON = create("dragon_wings"),
                LVJIA_SUPER = create("lvjia_super_wings");

        private Names() {
        }

        private static @NonNull Identifier create(@NonNull String path) {
            return locate(path);
        }
    }
}
