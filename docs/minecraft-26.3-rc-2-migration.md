# Minecraft 26.3-rc-2 迁移记录

本次目标是 Fabric 26.3-rc-2。NeoForge 源码已同步调整，但没有编译，也不能用现有的 NeoForge 26.2 依赖运行。

## 安装与重新构建

- Minecraft：26.3-rc-2，不能将本次 jar 用于 26.2。
- Java：25。
- Fabric Loader：0.19.3 或兼容的更新版本。
- Fabric API：0.160.4+26.3，这是官方对应 RC 2 的版本。
  参考：https://github.com/FabricMC/fabric-api/releases/tag/0.160.4%2B26.3
- 成品：`fabric/build/libs/wings-26.3-rc-2-Fabric-1.1.11.jar`。
  不要安装同目录的 `-sources.jar`，也不要保留旧版 Wings jar。

在项目根目录用 PowerShell 执行：

```powershell
$env:JAVA_HOME = 'D:\Program Files\Java\jdk-25'
.\gradlew.bat -PfabricOnly=true :fabric:build --max-workers=4
```

`-PfabricOnly=true` 会跳过 NeoForge 项目的配置。当前 PATH 默认 Java 是 21，所以这里显式选择本机 JDK 25；无需修改全局 PATH。

无窗口验证命令：

```powershell
$env:JAVA_HOME = 'D:\Program Files\Java\jdk-25'
.\gradlew.bat -PfabricOnly=true -I scripts/verify-fabric-migration.gradle :fabric:migrationSmoke --max-workers=4
```

验证程序作为临时 Fabric 模组运行在独立 Java 进程中，完成后自动退出。它的输出和运行目录在根目录 `build/fabric-migration-verification`，不会打包进正式模组。

## 已修改的内容

- 更新 Fabric API，并将实际验证过的 Loom 固定为 1.17.20。
- 打包时将 Mojang 的 `26.3-rc-2` 转成 Fabric Loader 匹配所需的 `26.3-rc.2`；下载依赖和 jar 文件名仍使用原版本号。也处理同格式的 pre 版本号。
- 第一人称手臂改用 `FirstPersonHandsAndItems` 和 `FirstPersonHandsAndItemsRenderer`，副手动画及渲染 Mixin 下沉到共享层。副手显示条件随渲染状态传递。
- 服务器飞行检查改为注入 `handlePlayerPositionChange`，避免旧目标方法中没有调用点而导致启动崩溃。
- 两个平台的 R 键注册均改用 SDL 对应的原版 `InputConstants.KEY_R` 和 `Type.KEYBOARD`。
- 适配旋转矩阵、噪声采样、顶点 UV3、披风模型提交和服务端掉落物预测参数。
- 删除旧酿造 Mixin、Fabric 手工配方匹配和 NeoForge 酿造注册，改用 `src/main/resources/data/wings/recipe/brewing` 下十个共享原版配方。
- 每种酿造配方都接受普通和延长版缓降药水；材料和输出保持原有设定，不新增超级翅膀配方。
- `pack.mcmeta` 使用新的 `min_format` / `max_format`，覆盖此版本资源格式 97.1 和数据格式 121.0。

## 验证范围

- Fabric 完整构建与 33 项原有单元测试。
- 使用 Fabric Loader 0.19.3 实际加载并变换所有相关 Mixin 目标类。
- 实际构建鸟类和昆虫类翅膀模型，覆盖反射访问的模型结构。
- 使用原版配方 Codec 解码十个配方，验证二十种有效输入的匹配及输出，并拒绝三十种错误输入组合。

无窗口验证不会进入游戏世界，也没有验证 GPU 画面、Sodium/Iris 兼容性或多人联机。发布前建议用测试存档检查：喝下翅膀药水后按 R 起降、第三人称翅膀与披风、第一人称空副手、主手地图、采集蝙蝠血、酿造，以及死亡重生和换维度后的状态同步。若 R 键无响应，先在控制设置里重新绑定。

## 后续版本怎么处理

- 26.3 正式版发布后，不要只把 `minecraft_version` 改掉就发布。需要更新对应 Fabric API，重新运行构建和无窗口验证，再进世界试飞。
- `neo_version=26.2.0.1-beta` 当前只是未升级的占位值。等兼容 26.3 的 NeoForge 发布，再填写实际版本并运行 `:neoforge:build`，检查 NeoForge 事件、按键和注册 API 是否有新变化。
- NeoForge 编译任务增加了游戏版本检查，防止把旧加载器依赖误当作 26.3 构建。共享源码现在已面向 26.3；如果需要发布 26.2 修复，应使用升级前的独立分支或检出版本。
- 源码与验证脚本保存在本地项目中，后续可以直接按上述命令构建和排查，不依赖继续这次对话。
