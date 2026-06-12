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
- 共享代码可以引用两边都实现的同包同名平台 facade（例如 `cc.lvjia.wings.server.item.WingsItems`、
  `cc.lvjia.wings.server.flight.Flights`），但这些 facade 的公共方法/字段应保持两边一致，并且不能把 Fabric API
  或 NeoForge API 类型泄漏到 `src/main/java` 的公共签名中。
- Fabric/NeoForge 同相对路径文件允许不同实现；这类文件通常应只承载注册、事件订阅、网络收发、能力/attachment
  存储和配置格式适配。若差异只剩纯业务逻辑，应继续下沉到 `src/main/java`。
- 现有共享抽象包括：
  - `cc.lvjia.wings.WingsCore`：mod id、资源定位、翅膀注册清单和默认翅膀实现。
  - `cc.lvjia.wings.server.FlightListenerSupport`：服务端飞行监听和同步触发逻辑。
  - `cc.lvjia.wings.server.ServerEventActions`：服务端事件的共享业务动作。
  - `cc.lvjia.wings.server.command.WingsCommandActions`：`/wings` 命令的实际执行逻辑。
  - `cc.lvjia.wings.server.flight.FlightEventSupport`：玩家克隆、维度切换、登录、追踪等飞行同步动作。
  - `cc.lvjia.wings.server.flight.FlightStateReset`：旁观者飞行状态清理。
  - `cc.lvjia.wings.server.item.WingsItemCatalog`：瓶装翅膀的共享展示顺序。
  - `cc.lvjia.wings.server.potion.WingsBrewingCatalog`：瓶装翅膀酿造配方清单。
  - `cc.lvjia.wings.server.config.WingsConfigDefaults`：跨加载器配置默认值、范围和 clamp 逻辑。
  - `cc.lvjia.wings.client.ClientEventHandlerSupport`、`ClientFlightListenerSupport`、
    `ClientFlightInputActions`、`ClientFlightSyncApplier`：客户端事件、按键和飞行同步的共享逻辑。
  - `cc.lvjia.wings.client.hooks.ClientRenderHookSupport`、`ItemInHandHookSupport` 和
    `cc.lvjia.wings.server.asm.BodyRotationHookSupport`：mixin/hook 后的共享行为。

## 构建与验证
- 如果你是OpenCode，请勿执行 gradlew 命令，让用户手动构建，因为OpenCode无法正确判断命令是否执行完成，如果你不是OpenCode，就可以执行
- 完整验证命令：`.\gradlew.bat clean build`
- 结构漂移快速检查：`.\gradlew.bat reportPlatformSourceDrift`，输出在
  `build/reports/platform-source-drift.txt`。这个报告用于定位同路径平台文件和同名异路径文件；看到同路径差异不代表必须合并，
  需要按上面的平台 facade 规则判断是否还有业务逻辑可下沉。
- 成功标准是 Fabric 和 NeoForge 两个子项目都编译、处理资源并产出 jar。
- 参考工程和本地依赖目录只作对照，不应纳入版本控制；`.gitignore` 已排除 `NeoForge-26.1.x/`、
  `minecraft_client_26.1.2/`、`fabric-api-26.1.2/`、`fabric-api-26.2/` 等目录。
