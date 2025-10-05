import os

# 从用户提供的最终对照表中提取的精确替换映射（类名）
replacements = {
    "net.minecraftforge.api.distmarker.OnlyIn": "net.neoforged.api.distmarker.OnlyIn",
    "net.minecraftforge.client.event.EntityRenderersEvent": "net.neoforged.neoforge.client.event.EntityRenderersEvent",
    "net.minecraftforge.client.event.InputEvent": "net.neoforged.neoforge.client.event.InputEvent",
    "net.minecraftforge.client.event.ModelEvent": "net.neoforged.neoforge.client.event.ModelEvent",
    "net.minecraftforge.client.event.RegisterClientReloadListenersEvent": "net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent",
    "net.minecraftforge.client.event.RegisterKeyMappingsEvent": "net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent",
    "net.minecraftforge.client.event.ViewportEvent": "net.neoforged.neoforge.client.event.ViewportEvent",
    "net.minecraftforge.client.settings.IKeyConflictContext": "net.neoforged.neoforge.client.settings.IKeyConflictContext",
    "net.minecraftforge.client.settings.KeyConflictContext": "net.neoforged.neoforge.client.settings.KeyConflictContext",
    "net.minecraftforge.client.settings.KeyModifier": "net.neoforged.neoforge.client.settings.KeyModifier",
    "net.minecraftforge.common.brewing.BrewingRecipe": "net.neoforged.neoforge.common.brewing.BrewingRecipe",
    "net.minecraftforge.common.capabilities.AutoRegisterCapability": "net.neoforged.neoforge.common.capabilities.AutoRegisterCapability",
    "net.minecraftforge.common.capabilities.Capability": "net.neoforged.neoforge.common.capabilities.Capability",
    "net.minecraftforge.common.capabilities.CapabilityManager": "net.neoforged.neoforge.common.capabilities.CapabilityManager",
    "net.minecraftforge.common.capabilities.CapabilityToken": "net.neoforged.neoforge.common.capabilities.CapabilityToken",
    "net.minecraftforge.common.capabilities.ForgeCapabilities": "net.neoforged.neoforge.common.capabilities.Capabilities",
    "net.minecraftforge.common.capabilities.ICapabilityProvider": "net.neoforged.neoforge.common.capabilities.ICapabilityProvider",
    "net.minecraftforge.common.ForgeConfigSpec": "net.neoforged.neoforge.common.NeoForgeConfigSpec",
    "net.minecraftforge.common.MinecraftForge": "net.neoforged.neoforge.common.NeoForge",
    "net.minecraftforge.common.util.INBTSerializable": "net.neoforged.neoforge.common.util.INBTSerializable",
    "net.minecraftforge.common.util.LazyOptional": "net.neoforged.neoforge.common.util.LazyOptional",
    "net.minecraftforge.event.AttachCapabilitiesEvent": "net.neoforged.neoforge.event.AttachCapabilitiesEvent",
    "net.minecraftforge.event.brewing.BrewingRecipeRegisterEvent": "net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent",
    "net.minecraftforge.event.BuildCreativeModeTabContentsEvent": "net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent",
    "net.minecraftforge.event.entity.EntityJoinLevelEvent": "net.neoforged.neoforge.event.entity.EntityJoinLevelEvent",
    "net.minecraftforge.event.entity.EntityMountEvent": "net.neoforged.neoforge.event.entity.EntityMountEvent",
    "net.minecraftforge.event.entity.living.LivingDeathEvent": "net.neoforged.neoforge.event.entity.living.LivingDeathEvent",
    "net.minecraftforge.event.entity.living.LivingEvent": "net.neoforged.neoforge.event.entity.living.LivingEvent",
    "net.minecraftforge.event.entity.player.PlayerEvent": "net.neoforged.neoforge.event.entity.player.PlayerEvent",
    "net.minecraftforge.event.entity.player.PlayerInteractEvent": "net.neoforged.neoforge.event.entity.player.PlayerInteractEvent",
    "net.minecraftforge.event.ForgeEventFactory": "net.neoforged.neoforge.event.EventHooks",
    "net.minecraftforge.event.network.CustomPayloadEvent": "net.neoforged.neoforge.network.NetworkEvent.ClientCustomPayloadEvent",  # 选择Client版本作为示例
    "net.minecraftforge.event.RegisterCommandsEvent": "net.neoforged.neoforge.event.RegisterCommandsEvent",
    "net.minecraftforge.event.TickEvent": "net.neoforged.neoforge.event.TickEvent",
    "net.minecraftforge.eventbus.api.Event": "net.neoforged.bus.api.Event",
    "net.minecraftforge.eventbus.api.EventPriority": "net.neoforged.bus.api.EventPriority",
    "net.minecraftforge.eventbus.api.IEventBus": "net.neoforged.bus.api.IEventBus",
    "net.minecraftforge.eventbus.api.SubscribeEvent": "net.neoforged.bus.api.SubscribeEvent",
    "net.minecraftforge.fml.common.Mod": "net.neoforged.fml.common.Mod",
    "net.minecraftforge.fml.config.ModConfig": "net.neoforged.fml.config.ModConfig",
    "net.minecraftforge.fml.DistExecutor": "net.neoforged.fml.DistExecutor",
    "net.minecraftforge.fml.event.config.ModConfigEvent": "net.neoforged.fml.event.config.ModConfigEvent",
    "net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent": "net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent",
    "net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext": "net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext",
    "net.minecraftforge.fml.LogicalSide": "net.neoforged.fml.LogicalSide",
    "net.minecraftforge.fml.ModLoadingContext": "net.neoforged.fml.ModLoadingContext",
    "net.minecraftforge.items.IItemHandler": "net.neoforged.neoforge.items.IItemHandler",
    "net.minecraftforge.network.Channel": "net.neoforged.neoforge.network.NetworkInstance",
    "net.minecraftforge.network.ChannelBuilder": "net.neoforged.neoforge.network.NetworkRegistry.ChannelBuilder",
    "net.minecraftforge.network.PacketDistributor": "net.neoforged.neoforge.network.PacketDistributor",
    "net.minecraftforge.network.SimpleChannel": "net.neoforged.neoforge.network.simple.SimpleChannel",
    "net.minecraftforge.registries.DeferredRegister": "net.neoforged.neoforge.registries.DeferredRegister",
    "net.minecraftforge.registries.ForgeRegistries": "net.neoforged.neoforge.registries.ForgeRegistries",
    "net.minecraftforge.registries.RegistryObject": "net.neoforged.neoforge.registries.RegistryObject",
}

def migrate_file(file_path, replacements):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original_content = content
    for old, new in replacements.items():
        content = content.replace(old, new)
    
    if content != original_content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Updated: {file_path}")

def main():
    src_dir = r"E:\zaxiang4\lv-wings\src"
    for root, dirs, files in os.walk(src_dir):
        for file in files:
            if file.endswith('.java'):
                file_path = os.path.join(root, file)
                migrate_file(file_path, replacements)

if __name__ == "__main__":
    main()