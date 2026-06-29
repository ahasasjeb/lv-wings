# c.md 内容核实报告

核实方式：只读对照源码（2026-06-29），未做运行时 profiling 或游戏内复现。  
结论标记：**属实** / **部分属实** / **不属实** / **待确认**

---

## 性能优化点

### 1. `WingsLayerRenderer.java:46-73` 渲染热路径分配

**结论：属实**

核实依据：

- `submitWings` 对每个可见玩家调用 `FlightViews.get(player).ifPresent(...)`（第 46 行）。
- `submitCustomGeometry` 回调内 `new PoseStack()`（第 59 行）。
- 随后调用 `SodiumBypassVertexConsumer.wrap(safeBuffer)`（第 65 行）。
- 该路径在 Fabric/NeoForge 翅膀层 `submit` 中每帧执行（`FabricLayerWings` / `NeoForgeLayerWings` 均调用 `WingsLayerRenderer.submitWings`）。

优化建议（复用 PoseStack、减少包装分配）与代码事实一致。

---

### 2. `SodiumBypassVertexConsumer.java:18-33` 同步弱缓存开销

**结论：属实**

核实依据：

- 第 18-19 行：`Collections.synchronizedMap(new WeakHashMap<>())`。
- `wrap()` 每次经 `computeIfAbsent` 查表（第 31-32 行），同步 map 有锁成本。
- 类注释明确用途为绕过 Sodium 的 `VertexBufferWriter` 接口。

---

### 3. `BuffedFlightApparatus.java:177-187` LVJIA_SUPER 敌对生物查询热点

**结论：部分属实**

核实依据（属实部分）：

- `applyHostileMobAvoidance` 使用 `getEntitiesOfClass(Mob.class, ...)`（第 64-65 行）。
- 默认 `MobAvoidanceSettings.DEFAULT`：半径 14、间隔 10 tick（第 224 行）。
- `LVJIA_SUPER` 注册为 `BuffedFlightApparatus`，未自定义回避参数，使用默认值（`WingsCore.java:53-56`）。
- 冷却逻辑在 `onUpdate` 的 183-187 行，多人同时有翅膀效果时确为服务端热点。

与 c.md 表述的差异：

- c.md 写「**飞行时**」定期查询；实际触发条件是 `canFly(player)` 为真时的 `state.onUpdate`，**不要求** `flight.isFlying()`。
- 触发链：`FlightDefault.onWornUpdate` 第 315-316 行 → `BuffedFlightApparatus.createState` 包装的 `onUpdate`。
- `canFly` 条件：有 `WINGS` 药水效果、饥饿 > 0、装置可用、未禁水下飞行（`FlightDefault.java:218-221`）。
- 因此玩家**站在地上但仍有翅膀效果**时也会周期性查询，热点场景比 c.md 描述的更广。

---

### 4. `FlightViews.java:14-24` synchronized WeakHashMap

**结论：部分属实**

核实依据（属实部分）：

- 第 14-15 行确为 `Collections.synchronizedMap(new WeakHashMap<>())`。
- 主要读写在客户端渲染/ tick 路径（`WingsLayerRenderer`、`FlightViewDefault`）。

未证实部分：

- 「访问主要发生在主客户端线程」——代码路径支持该推断，但**未找到**明确的多线程调用证据；`invalidate` 仅在客户端代理/事件处理中调用（`FabricClientProxy`、`NeoForgeClientProxy`、`ClientEventHandlerSupport`），大概率同线程。
- 去掉同步是否安全属于优化推断，不能从静态分析完全证明。

---

### 5. `AnimatorAvian.java:79-85` + `ModelWingsAvian.java:204-215` sin/噪声计算

**结论：属实**

核实依据：

- `ModelWingsAvian` 骨骼/羽毛各 4 段（`bonesLeft/Right`、`feathersLeft/Right` 各 `ImmutableList` 4 元素，第 78-88 行）。
- `render` 对 4 骨 + 4 羽循环调用 `getWingRotation` / `getFeatherRotation`（第 204-215 行）。
- `AnimatorAvian` 各 `Movement` 实现含多次 `Math.sin`；`GlideMovement` 等还使用 `SimplexNoise`（如第 204、209-214 行一带）。
- 多人同屏成本放大判断合理。

---

### 6. `WingsLayerRenderer.java:109-116` 实体反查 vs `ClientRenderHookSupport`

**结论：部分属实**

核实依据（属实部分）：

- `WingsLayerRenderer.resolvePlayer`（第 109-116 行）直接 `level.getEntity(state.id)`。
- 翅膀层与披风层（`submitCape` 第 85 行）均走此路径，未使用 `ClientRenderHookSupport`。
- 项目内 `WingsHooksClient` 已在动画/旋转钩子使用 `ClientRenderHookSupport.withResolvedPlayer`。

与 c.md 表述的差异：

- `ClientRenderHookSupport.resolvePlayer` **并非**实体 ID 查找缓存；它仍先 `getEntity`，失败才回退 `ThreadLocal<AbstractClientPlayer>`（`ClientRenderHookSupport.java:33-44`）。
- `ThreadLocal` 在 `onExtractPlayerRenderState` 时设置（第 18-19 行），是渲染阶段回退机制，不是跳过 `getEntity` 的缓存。
- 「统一复用以减少查找」作为优化方向合理，但 c.md 对现有机制「缓存」的表述略强。

---

### 7. `FabricKeyInputListener` / `NeoForgeKeyInputListener` Stream 遍历

**结论：属实**

核实依据：

- Fabric：`ClientTickEvents.END_CLIENT_TICK` 内 `entrySet().stream().filter(...).flatMap(...).forEach(...)`（`FabricKeyInputListener.java:25-28`）。
- NeoForge：`onKey` 同样 Stream 链（`NeoForgeKeyInputListener.java:33-36`）。
- 绑定数量很少，「影响低」判断合理。

---

### 8. `FlightSpeedAntiCheat.java:34-52` 每 tick 固定成本

**结论：属实**

核实依据：

- `ServerEventActions.onPlayerTick` 对每个服务端玩家每 tick 调用 `FlightSpeedAntiCheat.tick`（`ServerEventActions.java:82`）。
- `tick` 开头无条件：`System.nanoTime()`（第 35 行）、`cleanupExpiredStates`（第 36 行）、`WingsConfig.getFlightAntiCheatSettings()`（第 38 行）。
- 仅当 `state == null && !shouldMonitor(...)` 时在第 45-46 行提前返回；非飞行且无 tracking state 的玩家仍承担上述固定成本。
- `shouldMonitor` 要求 `flight.isFlying()` 等（第 176-182 行）。

---

## 潜在 Bug

### 1. `checkMovementStatistics` 派发 `PlayerFlownEvent` vs「每次挥翅」

**结论：属实**

核实依据：

- `ServerPlayerMixin` 在 `checkMovementStatistics` 尾部注入，每次统计更新都调用 `WingsHooks.onAddFlown`（`ServerPlayerMixin.java:13-15`）。
- `ServerEventActions.onPlayerFlown` 注释写「每次挥翅时触发」（`ServerEventActions.java:104`），与注入点不符。
- `FlightDefault.onFlown`（第 367-372 行）：
  - 飞行中 → `onFlight`（按移动距离扣饥饿，`SimpleFlightApparatus.java:19-23`）。
  - **未飞行**且 `direction.y() < -0.5` → `onLanding`（固定扣 `landingExertion`，第 27-28 行）。
- 高速下落且未处于 `isFlying()` 时，每次移动统计满足条件即可反复触发 `onLanding`，存在误扣饥饿风险。

---

### 2. `WingsHooks.onFlightCheck(LivingEntity, ...)` 非 Player 返回 false

**结论：属实**

核实依据：

- Fabric / NeoForge 实现一致（`WingsHooks.java:17-19`）：
  ```java
  return living instanceof Player && WingsHooks.onFlightCheck((Player) living, defaultValue);
  ```
- 非 `Player` 时短路为 `false`，**忽略** `defaultValue`。
- `LivingEntityMixin` 将 `isVisuallySwimming()` 内的 `isFallFlying()` 重定向到此钩子（`LivingEntityMixin.java:30-32`），影响所有 `LivingEntity`。
- 对非玩家实体（如原版本应 `isFallFlying()==true` 的情况）会被强制为 `false`，与 c.md 描述一致。

---

### 3. NeoForge `copyOnDeath()` vs Fabric 仅 `alive` 时复制

**结论：属实**

核实依据：

- NeoForge `WingsAttachments.FLIGHT` 配置 `.copyOnDeath()`（`WingsAttachments.java:21-25`）。
- NeoForge `Flights.onPlayerClone`：`copyFlightState = !event.isWasDeath()`（`Flights.java:44-45`），死亡时**不**手动 `clone`。
- Fabric `ServerPlayerEvents.COPY_FROM`：`Flights.onPlayerClone(old, new, alive)`（`FabricServerEventHandler.java:50-51`），仅 `alive == true` 时 `FlightEventSupport.onPlayerClone` 执行复制（`FlightEventSupport.java:36-40`）。
- Fabric attachment 无 `copyOnDeath` 等价配置（`fabric/.../WingsAttachments.java`）。
- 死亡重生后：NeoForge 可能通过 attachment 机制保留 flight 数据；Fabric 不复制。两边行为不一致。
- c.md 关于「药水消失后旧 wing 状态短暂同步」为合理推断：重生时 `syncSelf` 会发包（`Flights.onPlayerRespawn`），而 `FlightDefault.tick` 无效果时会清 wing（第 329-330、334-335 行），存在短暂不一致窗口。**待确认**实际可见程度需游戏内验证。

---

### 4. `BuffedFlightApparatus` 效果不要求 `isFlying()`

**结论：属实**

核实依据：

- 效果与敌对回避均在 `onUpdate` 中执行（`BuffedFlightApparatus.java:177-188`），无 `flight.isFlying()` 判断。
- 调用前提为 `canFly(player)`（见性能项 3 触发链）。
- 是否为 bug 取决于设计意图；c.md 的条件表述正确。

---

### 5. `clone` 不复制 `prevTimeFlying`

**结论：属实**（影响范围比 c.md 略广）

核实依据：

- `clone` 复制 `isFlying`、`timeFlying`、`wing`、`animationState`（`FlightDefault.java:377-381`），**未**复制 `prevTimeFlying`。
- `getFlyingAmount` 使用 `MathH.lerp(prevTimeFlying, timeFlying, delta)`（第 192-194 行）。
- 网络 `deserialize` 同样只更新 `timeFlying`，不重置 `prevTimeFlying`（第 401-411 行）；`prevTimeFlying` 仅在 `tick` 末尾更新（第 341 行）。
- 因此维度切换 clone、网络同步后，到下一次 `tick` 之前都可能出现展开/收起插值跳变。c.md 结论成立。

---

### 6. 披风隐藏不看 `WINGS` 效果

**结论：属实**

核实依据：

- `hasVisibleWings` 仅检查 `Flights.get(player).getWing()` 是否为 `NONE` / `WINGLESS`（`WingsLayerRenderer.java:118-120`）。
- 未调用 `flight.hasEffect(player)` 或等效判断。
- `FlightDefault.tick` 在服务端无药水效果时会 `setWing(NONE)`（第 334-335 行），存在 attachment 中 wing 滞后于效果消失的窗口期；此间披风可能被错误隐藏。

---

## 已确认较好的点

### 1. `setIsFlying` / `setWing` 仅在变化时同步

**结论：属实**

核实依据：`FlightDefault.java:140-146`、`165-172` 均有 `if (this.isFlying != isFlying)` / `if (this.flightApparatus != wing)` 守卫后才 `sync`。

---

### 2. 动画同步有冷却与 23 tick 周期

**结论：属实**

核实依据：

- `PERIODIC_SYNC_INTERVAL_TICKS = 23`（`FlightAnimationTracker.java:12`）。
- 状态切换冷却 `TRANSITION_SYNC_COOLDOWN_TICKS = 4`（第 14 行）。
- `tick` 仅在状态切换（含冷却逻辑）或周期计数归零时返回 `true` 触发同步（第 37-73 行）。

---

### 3. 模型/动画器非每帧重建

**结论：属实**

核实依据：

- `FlightViewDefault.tick` 仅在翅膀类型变化时 `PresentWingState.newState`（第 66-68 行）。
- `WingStrategy` 构造时一次性 `shape.createAnimator()`（第 136-138 行），同类型翅膀复用同一 `WingState` 实例（第 108-111 行）。
- 每帧只调用 `animator.update()` 与渲染，不重建模型/动画器对象。

---

## 汇总

| 分类 | 条目 | 结论 |
|------|------|------|
| 性能 | 1 WingsLayerRenderer 分配 | 属实 |
| 性能 | 2 SodiumBypass 同步缓存 | 属实 |
| 性能 | 3 BuffedFlight 敌对查询 | **部分属实**（触发条件是 canFly 而非 isFlying） |
| 性能 | 4 FlightViews 同步 map | **部分属实**（跨线程未证实） |
| 性能 | 5 Avian sin/噪声 | 属实 |
| 性能 | 6 实体反查未复用 Hook | **部分属实**（Hook 非查找缓存） |
| 性能 | 7 Key Stream | 属实 |
| 性能 | 8 AntiCheat 每 tick 成本 | 属实 |
| Bug | 1 PlayerFlownEvent / onLanding | 属实 |
| Bug | 2 非 Player onFlightCheck | 属实 |
| Bug | 3 copyOnDeath 平台差异 | 属实（客户端可见性待确认） |
| Bug | 4 Buffed 不要求飞行 | 属实 |
| Bug | 5 prevTimeFlying 未复制 | 属实 |
| Bug | 6 披风隐藏逻辑 | 属实 |
| 较好 | 1 条件同步 | 属实 |
| 较好 | 2 动画节流 | 属实 |
| 较好 | 3 动画器复用 | 属实 |

**总体**：c.md 21 条中 **17 条完全属实**，**4 条部分属实**（性能 3/4/6，Bug 3 的可见性待确认），**0 条不属实**。  
主要需修正的表述：性能项 3 的触发时机（`canFly` ≠ `isFlying`）；性能项 6 对 `ClientRenderHookSupport`「缓存」的描述略强于实现。