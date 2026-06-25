package cc.lvjia.wings;

import cc.lvjia.wings.server.FlightListenerSupport;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.net.Message;
import cc.lvjia.wings.server.net.Network;
import cc.lvjia.wings.server.net.clientbound.MessageSyncFlight;
import cc.lvjia.wings.server.potion.PotionMix;
import cc.lvjia.wings.server.potion.WingsBrewingCatalog;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

// NeoForge 平台代理：网络注册 + 酿造配方 + 飞行监听器绑定
public class NeoForgeProxy {
    protected final Network network = new Network();

    public void init(IEventBus modBus) {
        this.network.register(modBus);
        // NeoForge 特有：通过事件总线注册酿造配方
        NeoForge.EVENT_BUS.addListener(this::registerBrewingRecipes);
    }

    // 注册瓶装翅膀的酿造配方（缓降药水 + 翅膀材料）
    private void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
        var builder = event.getBuilder();
        BiConsumer<ItemLike, Supplier<? extends Item>> reg = (item, supplier) -> {
            builder.addRecipe(new PotionMix(Potions.SLOW_FALLING, Ingredient.of(item), new ItemStack(supplier.get())));
            builder.addRecipe(new PotionMix(Potions.LONG_SLOW_FALLING, Ingredient.of(item), new ItemStack(supplier.get())));
        };

        WingsBrewingCatalog.forEachMix(reg);
    }

    // 为玩家 Flight 注册同步监听器，使用 NeoForge 网络 API 发送包
    public void addFlightListeners(Player player, Flight instance) {
        FlightListenerSupport.addFlightListeners(player, instance, new FlightListenerSupport.Sync() {
            @Override
            public void sendToPlayer(Player source, Flight flight, net.minecraft.server.level.ServerPlayer target) {
                network.sendToPlayer(new MessageSyncFlight(source, flight), target);
            }

            @Override
            public void sendToAllTracking(Player source, Flight flight, net.minecraft.server.level.ServerPlayer trackedEntity) {
                network.sendToAllTracking(new MessageSyncFlight(source, flight), trackedEntity);
            }
        });
    }

    public void invalidateFlightView(Player player) {
    }

    public void sendToServer(Message message) {
        throw new UnsupportedOperationException("sendToServer is client-only");
    }
}
