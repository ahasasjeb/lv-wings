# 末地传送门（end_portal）渲染实现说明

本文档汇总了 Minecraft 1.18.2（反编译源码）中末地传送门的渲染实现要点、关键源码位置与如何在模组中复用或修改的建议。

## 概要

末地传送门的渲染由一个 BlockEntity 渲染器负责（`TheEndPortalRenderer`），它绘制一个立方体（6 个面），并使用专门的 `RenderType`（`rendertype_end_portal`）与对应的 shader，同时绑定两张纹理：`end_sky.png` 与 `end_portal.png`。渲染的视觉效果由 shader 在片元阶段合成产生（星空与涡流/扭曲效果）。

## 关键源码与文件（位置）

- `RenderType` 定义（GL 状态与 end_portal RenderType）
  - 文件：`反编译1.18.2MC源代码/net/minecraft/client/renderer/RenderType.java`
  - 关键点：`END_PORTAL` 使用 `RENDERTYPE_END_PORTAL_SHADER` 和一个 `MultiTextureStateShard`，其中两张纹理来自 `TheEndPortalRenderer` 的常量。

- `TheEndPortalRenderer`（实际绘制立方体面）
  - 文件：`反编译1.18.2MC源代码/net/minecraft/client/renderer/blockentity/TheEndPortalRenderer.java`
  - 关键点：
    - 常量纹理路径：
      - `END_SKY_LOCATION = "textures/environment/end_sky.png"`
      - `END_PORTAL_LOCATION = "textures/entity/end_portal.png"`
    - `render(...)` 调用 `renderCube(...)`，后者通过 `renderFace(...)` 为六个面每个提交四个顶点（使用 `DefaultVertexFormat.POSITION`）。
    - `renderType()` 返回 `RenderType.endPortal()`。

- RenderState / Shader 连接
  - 文件：`反编译1.18.2MC源代码/net/minecraft/client/renderer/RenderStateShard.java`
  - 关键点：
    - `MultiTextureStateShard` 会把多张纹理依序绑定到连续的纹理单元（纹理单元 0、1、...），并设置过滤等。
    - `ShaderStateShard` 会在 setup 时把对应的 `ShaderInstance` 应用到渲染状态中。

- Shader 创建与文件
  - 文件：`反编译1.18.2MC源代码/net/minecraft/client/renderer/GameRenderer.java`
  - 关键点：
    - 在 `reloadShaders(...)` 中会创建 `ShaderInstance`：`new ShaderInstance(resourceManager, "rendertype_end_portal", DefaultVertexFormat.POSITION)`，这意味着 shader JSON/GLSL 存在于运行时资源路径 `shaders/core/rendertype_end_portal.json`（以及对应的 `.vsh`/`.fsh`）。
    - `ShaderInstance` 负责读取 JSON、加载并编译 vertex/fragment 程序并解析 samplers/uniforms。

- ShaderInstance（加载与绑定机制）
  - 文件：`反编译1.18.2MC源代码/net/minecraft/client/renderer/ShaderInstance.java`
  - 关键点：
    - 解析 `shaders/core/<name>.json` 来找到 vertex/fragment 程序、samplers、attributes、uniforms。
    - 在 `apply()` 时会根据解析的 sampler 列表把对应的纹理绑定到 shader 的采样器 uniform 上，并上传 uniforms。

## 渲染流程（概览）

1. `GameRenderer` 在重载 shader 时加载 `rendertype_end_portal` 的 `ShaderInstance`（从资源 `shaders/core/rendertype_end_portal.json` 加载其 vertex/fragment 程序）。
2. 渲染时，`TheEndPortalRenderer.render(...)` 获取 `VertexConsumer`（来自 `MultiBufferSource.getBuffer(RenderType.endPortal())`），并以矩阵为上下文提交六个面的顶点（仅位置）。
3. `RenderType.endPortal()` 的 CompositeState 在 setup 阶段会：
   - 设置并启用对应 shader（`rendertype_end_portal`）。
   - 使用 `MultiTextureStateShard` 将 `end_sky.png` 与 `end_portal.png` 绑定到连续的纹理单元。
   - 设定其它 GL 状态（如深度测试、剔除等，参见 `RenderType` 的声明）。
4. 片段着色器读取绑定的 samplers 并对两张纹理进行合成/扭曲，从而生成最终的末地传送门视觉效果。

> 注意：在当前工作区中没有包含 shader JSON 与 GLSL 的源码文本（这些通常存在于运行时的资源包或 Minecraft 的 jar 内），所以要查看 shader 的完整实现需要从游戏的资源中提取 `assets/minecraft/shaders/core/rendertype_end_portal.json` 及其引用的 `.vsh`/`.fsh` 文件。

## 重要代码片段参考（位置摘录）

- `RenderType` 中 END_PORTAL 的定义（表明 shader 名称与多纹理）
  - 在 `RenderType.java` 中：
    - `private static final RenderType END_PORTAL = create("end_portal", DefaultVertexFormat.POSITION, ..., RenderType.CompositeState.builder().setShaderState(RENDERTYPE_END_PORTAL_SHADER).setTextureState(RenderStateShard.MultiTextureStateShard.builder().add(TheEndPortalRenderer.END_SKY_LOCATION, false, false).add(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false).build()).createCompositeState(false));`

- `TheEndPortalRenderer` 的顶点提交
  - 在 `TheEndPortalRenderer.java` 中：
    - `renderFace(...)` 会调用 `vertex(matrix, x, y, z).endVertex()` 四次以构建一个面。
    - `renderCube(...)` 依次为 `SOUTH/NORTH/EAST/WEST/DOWN/UP` 绘制六个面。

- `GameRenderer` 中加载 shader
  - 在 `GameRenderer.reloadShaders(...)` 中：
    - `list1.add(Pair.of(new ShaderInstance(resourceManager, "rendertype_end_portal", DefaultVertexFormat.POSITION), (s) -> { rendertypeEndPortalShader = s; }));`

## 如何在模组中复用或修改末地传送门渲染

1. 覆写或替换 `BlockEntityRenderer`
   - 扩展 `TheEndPortalRenderer`，覆盖 `getOffsetUp`/`getOffsetDown` 或 `renderCube`/`renderFace` 来改变形状、尺寸或添加其他顶点数据。
   - 注册你的渲染器替代默认的 `TheEndPortalRenderer`（Forge 的 `BlockEntityRenderers` 注册系统）。

2. 使用或创建自定义 `RenderType`
   - 你可以创建新的 `RenderType`（`RenderType.create(...)`）并指定你自己的 `CompositeState`（自定义 shader、纹理、透明度、剔除、深度写入等）。
   - 注意 vertex format（原实现使用 `POSITION`），如果需要 UV 或颜色需保持 shader 与顶点格式一致。

3. 添加/替换 shader
   - 在模组资源中加入 `assets/<modid>/shaders/core/your_rendertype.json` 与对应的 `.vsh/.fsh`，然后在客户端加载时创建 `ShaderInstance` 或使用 `RegisterShadersEvent` 在 GameRenderer reload 时注入。
   - shader JSON 中的 samplers/uniform 名称必须与 shader 代码匹配，`ShaderInstance` 会在 `apply()` 时根据名称把纹理绑定到相应 texture unit。

4. 更换纹理
   - 可直接在资源包中替换 `textures/environment/end_sky.png` 与 `textures/entity/end_portal.png`，或者为新的 `RenderType` 指定不同纹理路径。

## 注意与边缘情况

- 顶点格式需匹配：原实现顶点仅包含位置（POSITION）。若你扩展为包含 UV/颜色，需要同时修改 shader 与顶点格式。
- 多纹理绑定顺序和 shader 中 sampler 的命名必须对应，否则 shader 会读取错误的纹理单元。
- GL 状态（深度测试、写掩码、透明）会影响视觉与遮挡关系，修改时要小心以避免深度写入导致的遮挡问题。
- shader 代码在不同显卡/驱动上可能表现不同，修改后建议多平台测试。

## 下一步（可选）

- 如果需要，我可以：
  - 打开并贴出运行时资源中的 `shaders/core/rendertype_end_portal.json` 与对应 `.vsh/.fsh`，逐行解释 shader 的实现（需要你允许我在工作区或 Minecraft 的资源中查找这些文件）；
  - 提供一个最小示例，展示如何在模组中注册自定义 shader + RenderType，并替换末地传送门的渲染。

---

文件生成自Deepseek，根据仓库反编译源码分析。
