# Sodium / Embeddium 渲染兼容修复指南

本指南记录了 LV Wings 在安装 Embeddium（Sodium Forge 移植版）时翅膀模型渲染消失/塌陷的根本原因与解决方案，方便后续移植或排错。

## 问题现象

- 打开 Embeddium 后，装备模组翅膀的玩家模型身后不再显示 3D 贴图面片。
- 控制台无报错，但翅膀会在切换视角或刷新后持续缺失。

## 根本原因

Embeddium/Sodium 为了提升性能，会在 `ModelPart` 渲染阶段尝试将传入的 `VertexConsumer` 转换为其自定义的 `VertexBufferWriter`。

- 一旦转换成功，Sodium 会跳过原版 `ModelPart.Cube#compile` 的顶点写入逻辑，使用初始化时捕获的静态顶点快照。
- LV Wings 的 `Model3DTexture` 会在构造完成后通过反射重写 `ModelPart.Cube` 的 `polygons`，以实现自定义 3D 面片。Sodium 的快照因此与真实顶点数据不一致，导致渲染结果缺失或畸形。

## 解决方案概览

1. **引入 VertexConsumer 代理**
   - 新增 `com.toni.wings.client.renderer.SodiumBypassVertexConsumer`，对所有 `VertexConsumer` 方法进行透明代理，但**不实现** `VertexBufferWriter` 接口。
   - 当 Sodium 检测到代理对象不满足接口时，会自动回退到原版的顶点写入路径。

2. **在渲染入口统一包裹**
   - `LayerWings.render` 中获取渲染缓冲后立即调用 `SodiumBypassVertexConsumer.wrap(buffer.getBuffer(renderType))`。
   - 确保整个翅膀渲染流程都使用无法被 Sodium 接管的 `VertexConsumer`。

3. **保留模型内的安全包裹**
   - `Model3DTexture.compile` 继续对传入的 `VertexConsumer` 进行二次包裹，保证其他地方直接调用该模型时也能获得兼容性（双重保障，零副作用）。

4. **移除旧的 Embeddium Hook 方案**
   - 删除 `com.toni.wings.compat.embeddium` 目录及相关 mixin 配置，避免残留的线程局部 bypass 代码造成混淆。

## 关键代码片段

```java
// LayerWings.java
VertexConsumer builder = SodiumBypassVertexConsumer.wrap(buffer.getBuffer(form.getRenderType()));
```

```java
// Model3DTexture.java
@Override
public void compile(PoseStack.Pose pose, VertexConsumer buffer, ... ) {
    super.compile(pose, SodiumBypassVertexConsumer.wrap(buffer), ...);
}
```

## 验证步骤

1. `./gradlew build` — 确认工程能正常编译。
2. 启动装有 Embeddium 的客户端，进入任意世界。
3. 装备翅膀后，切换第一/第三人称视角，验证 3D 面片正常显示、随动画运动。

## 迁移与注意事项

- 若未来添加新的自定义 `ModelPart.Cube` 实现，只要在渲染入口继续包裹 `VertexConsumer` 即可复用此方案。
- 避免再度尝试实现 `VertexBufferWriter` 或拦截 Sodium 的 mixin，除非需要做深度性能优化。
- 若升级至高版本 Sodium/Embeddium，请再次确认其 `ModelPart` 混入逻辑是否发生变化；若接口保持兼容，此方案通常仍然适用。
