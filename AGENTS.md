# 项目结构说明

这是一个 Minecraft 26.1.x / 26.1.2 的 Fabric + NeoForge 双加载器项目。源码统一放在根目录的 `src` 下，通过 source set 区分共享代码和平台胶水代码。

## 目录约定

- `src/main/java`：共享代码。这里应只放不直接依赖 Fabric API 或 NeoForge API 的代码，包括原版 Minecraft 逻辑、飞行/动画/模型/物品行为、共享命令执行逻辑，以及小型跨平台抽象。
- `src/main/resources`：共享资源。两个加载器都会打包。
- `src/fabric/java`：Fabric 专用入口、注册、事件、网络、mixin 胶水和 Fabric API 适配。
- `src/fabric/resources`：Fabric 专用资源，例如 `fabric.mod.json`、Fabric mixin 配置等。
- `src/neoforge/java`：NeoForge 专用入口、DeferredRegister、事件订阅、网络注册和 NeoForge API 适配。
- `src/neoforge/resources`：NeoForge 专用资源，例如 `META-INF/neoforge.mods.toml`、access transformer、NeoForge mixin 配置等。
- `src/neoforge/templates`：NeoForge metadata 模板。
- `fabric`、`neoforge`：Gradle 子项目。只描述构建配置，不放业务源码。

## 共享层规则

- 新增不依赖加载器 API 的类，优先放入 `src/main/java`。
- Fabric/NeoForge 目录只保留平台生命周期、注册表、事件总线、网络通道、配置文件格式、mixin 差异等必须分叉的代码。
- 两边逻辑相同但入口 API 不同的功能，应先抽到 `src/main/java`，平台类只传入回调或适配器。
- 现有共享抽象包括：
  - `cc.lvjia.wings.WingsCore`：mod id、资源定位、翅膀注册清单和默认翅膀实现。
  - `cc.lvjia.wings.server.FlightListenerSupport`：服务端飞行监听和同步触发逻辑。
  - `cc.lvjia.wings.server.command.WingsCommandActions`：`/wings` 命令的实际执行逻辑。
  - `cc.lvjia.wings.server.flight.FlightStateReset`：旁观者飞行状态清理。
  - `cc.lvjia.wings.server.item.WingsItemCatalog`：瓶装翅膀的共享展示顺序。
  - `cc.lvjia.wings.server.potion.WingsBrewingCatalog`：瓶装翅膀酿造配方清单。

## 构建与验证

- 完整验证命令：`.\gradlew.bat clean build`
- 成功标准是 Fabric 和 NeoForge 两个子项目都编译、处理资源并产出 jar。
- 参考工程和本地依赖目录只作对照，不应纳入版本控制；`.gitignore` 已排除 `NeoForge-26.1.x/`、`minecraft_client_26.1.2/`、`fabric-api-26.1.2/` 等目录。
