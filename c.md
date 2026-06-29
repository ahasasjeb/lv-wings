# 性能优化与潜在 Bug 记录

## 性能优化点

1. `src/main/java/cc/lvjia/wings/client/renderer/WingsLayerRenderer.java:46-73`
   每个可见带翅膀玩家每帧都会执行 `FlightViews.get(...).ifPresent(...)`，并在 `submitCustomGeometry` 回调里 `new PoseStack()`，再调用 `SodiumBypassVertexConsumer.wrap(...)`。这是渲染热路径上的对象分配点。可以考虑复用渲染用 `PoseStack` 包装逻辑，或让 `form.render` 直接接受当前 `PoseStack.Pose`，减少每帧分配。

2. `src/main/java/cc/lvjia/wings/client/renderer/SodiumBypassVertexConsumer.java:18-33`
   `wrap` 使用 `Collections.synchronizedMap(new WeakHashMap<>())`，渲染线程每次包装都要锁和哈希查找。若调用频率高，这会增加渲染开销。可以考虑渲染提交范围内的临时包装、ThreadLocal 单包装器，或确认 Sodium 兼容需求后避免全局同步弱缓存。

3. `src/main/java/cc/lvjia/wings/server/apparatus/BuffedFlightApparatus.java:177-187`
   `LVJIA_SUPER` 这类 buff 翅膀飞行时会定期执行敌对生物查询：`getEntitiesOfClass` 半径默认 14，间隔默认 10 tick。多人同时使用时是服务端热点。可以考虑配置更长间隔、仅飞行中执行、只在玩家移动一定距离后执行，或降低默认半径。

4. `src/main/java/cc/lvjia/wings/client/flight/FlightViews.java:14-24`
   客户端视图缓存使用 `synchronizedMap(new WeakHashMap<>())`，但访问主要发生在主客户端线程和渲染路径。若没有跨线程访问需求，可以改普通 `WeakHashMap` 或显式生命周期清理，避免锁成本。

5. `src/main/java/cc/lvjia/wings/client/flight/AnimatorAvian.java:79-85` 和 `src/main/java/cc/lvjia/wings/client/model/ModelWingsAvian.java:204-215`
   鸟翼每帧会为 4 段骨骼和 4 段羽毛计算多次 `sin`、噪声和角度换算。单个玩家成本不大，但多人同屏会放大。可以缓存同一帧的 `flapTime`、`sin(time)` 等中间值。

6. `src/main/java/cc/lvjia/wings/client/renderer/WingsLayerRenderer.java:109-116`
   渲染翅膀和披风隐藏判断都通过 `level.getEntity(state.id)` 反查玩家。项目里已有 `ClientRenderHookSupport` 的 render-state 玩家解析缓存思路，可以统一复用，减少每层渲染的实体查找。

7. `src/fabric/java/cc/lvjia/wings/util/FabricKeyInputListener.java:24-28` 和 `src/neoforge/java/cc/lvjia/wings/util/NeoForgeKeyInputListener.java:31-36`
   每 tick 或每按键事件用 Stream 遍历绑定。绑定目前很少，影响低；若追求零分配，可以换成普通 for 循环。

8. `src/main/java/cc/lvjia/wings/server/flight/FlightSpeedAntiCheat.java:34-52`
   每个服务端玩家 tick 都会 `System.nanoTime()` 和读取配置，再判断是否监控。可以先做飞行状态或配置启用的快速判断，再取时间和清理状态，减少非飞行玩家固定成本。

## 潜在 Bug

1. `src/main/java/cc/lvjia/wings/mixin/ServerPlayerMixin.java:13-16` 与 `src/main/java/cc/lvjia/wings/server/flight/FlightDefault.java:367-372`
   `checkMovementStatistics` 每次服务端移动统计都会派发 `PlayerFlownEvent`，但注释说“每次挥翅”。当前逻辑在玩家未飞行但 `deltaY < -0.5` 时会调用 `onLanding`，可能导致普通高速下落过程中反复扣饥饿值，而不是只在真正着陆或挥翅时触发。

2. `src/fabric/java/cc/lvjia/wings/server/asm/WingsHooks.java:18-19` 与 `src/neoforge/java/cc/lvjia/wings/server/asm/WingsHooks.java:17-18`
   `onFlightCheck(LivingEntity, defaultValue)` 对非 `Player` 直接返回 `false`，即使 `defaultValue` 是 `true`。这会把非玩家 `LivingEntity` 的原版 `isFallFlying()` 结果覆盖掉。更安全的逻辑通常应保留 `defaultValue`，再额外允许玩家翅膀飞行。

3. `src/neoforge/java/cc/lvjia/wings/WingsAttachments.java:21-31` 与 `src/neoforge/java/cc/lvjia/wings/server/flight/Flights.java:43-45`
   NeoForge 的 `FLIGHT` attachment 配了 `.copyOnDeath()`，但 Fabric 只在 `alive == true` 时复制 flight。结果可能是 NeoForge 死亡后短暂或实际保留翅膀 flight attachment，而 Fabric 不保留，平台行为不一致。即使药水效果死亡后消失，重生同步时也可能先把旧 wing 状态同步给客户端，再等 tick 清掉。

4. `src/main/java/cc/lvjia/wings/server/apparatus/BuffedFlightApparatus.java:177-187`
   `LVJIA_SUPER` 的效果和敌对生物回避是在 `canFly(player)` 时持续执行，不要求 `flight.isFlying()`。如果设计是“装备/拥有翅膀效果就生效”，没问题；如果设计是“飞行中才生效”，这是行为 bug，玩家站地上也会持续驱散敌对生物。

5. `src/main/java/cc/lvjia/wings/server/flight/FlightDefault.java:377-382`
   `clone` 不复制 `prevTimeFlying`。客户端收到同步快照后，`getFlyingAmount(delta)` 可能用旧 `prevTimeFlying` 和新 `timeFlying` 插值，造成一帧到数帧的展开或收起跳变。

6. `src/main/java/cc/lvjia/wings/client/renderer/WingsLayerRenderer.java:118-120`
   披风隐藏只看 `flight.getWing()`，不看是否仍有 `WINGS` 效果。若 attachment 中 wing 状态短暂滞留，可能出现“没有实际翅膀效果但披风被隐藏”的短暂显示问题。

## 已确认较好的点

1. `FlightDefault.setIsFlying` 和 `setWing` 只有状态变化才同步，避免无意义发包：`src/main/java/cc/lvjia/wings/server/flight/FlightDefault.java:140-172`。

2. 动画同步有状态切换冷却和 23 tick 周期同步，不是每 tick 发包：`src/main/java/cc/lvjia/wings/server/flight/FlightAnimationTracker.java:37-74`。

3. 翅膀模型和动画器基本是注册或状态切换时创建，不是每帧重建。
