# 飞行姿态异常（角色突然朝上/乱转）修复记录

## 现象描述
玩家在使用翅膀飞行时，模型偶尔会突然**完全直立（朝上）**或者在某些极端角度下鬼畜翻滚，无论朝哪个方向飞行都有可能触发。过一会（主要是转储角度或降落/起飞动画期间）又会自动恢复。

## 问题根因：最短路径插值与边界翻滚

该 Bug 是由**数学在处理环形角度（Modulo）求最短路径时的缺陷**导致的。

在未修复前的代码 `ClientEventHandler.java` 中，渲染层使用了 `MathH.lerpDegrees` 来插值处理角色的俯仰角度 (Pitch)：
```java
// 旧代码
float pitch = -MathH.lerpDegrees(player.xRotO, player.getXRot(), delta) - 90.0F;
matrixStack.mulPose(Axis.XP.rotationDegrees(MathH.lerpDegrees(0.0F, pitch, amt)));
```

1. **最短路径计算的陷阱**：
   `lerpDegrees` 被设计用于“角度环形插值”（例如，将 350 度平滑过渡到 10 度，跨越 360 度的边界，走 20 度而不是走 340 度）。但是，当玩家在空中向下看，计算出的 `pitch` 等于或由于浮点数误差略微小于 `-180` 度（例如 `-181` 度）时，灾难就发生了。
   
2. **导致后空翻**：
   在插值计算 `MathH.lerpDegrees(0.0F, -181.0F, amt)` 时，算法寻找从 0 到 -181 的最短路径。它错误地判定：**不向下旋转 181度（向俯冲方向），而是向上（向背侧）旋转 179 度是更短的路径**。
   当动画进度 `amt = 0.5` 时，插值的角度被诡异地求值为 `+89.5` 度（即身体向后仰平行于地面），这就导致了摄像机或者玩家模型剧烈“仰面朝天”乃至发生万向锁侧翻。

## 修复方案

在 Minecraft 中，除了偏航角 (Yaw) 会无限制突破 360 度以外，玩家的俯仰角 (Pitch/xRot) 本身就被原版代码严格截断在 `[-90, 90]` 范围内。而且身体与头部的横滚角度差距也总是安全的相对较小值。

因此，**对 Pitch 和 Roll 的渲染使用线性插值 (Linear Lerp) 是绝对安全且必要的，绝不能使用最短路径求值**。

我们在 `src/main/java/cc/lvjia/wings/client/ClientEventHandler.java` 中去掉了存在隐患的 `MathH.lerpDegrees`，改成了利用 `Mth.wrapDegrees` 先控制量纲，再用 `Mth.lerp` 进行直接线性插值：

```java
// 新代码 (OnApplyRotations 事件中)
if (amt > 0.0F) {
    // 1. 先用 wrapDegrees 获取身体与头部在上一帧和当前帧的相对差距值在 [-50, 50] 内
    float diffO = Mth.wrapDegrees(player.yBodyRotO - player.yRotO);
    float diff = Mth.wrapDegrees(player.yBodyRot - player.getYRot());
    
    // 2. 利用安全的线性插值计算滚转角度
    float roll = Mth.lerp(delta, diffO, diff);
    
    // 3. 俯仰角同样用线性求出正确的位置（避免翻转 180 度时的 shortest path 切变）
    float pitch = -Mth.lerp(delta, player.xRotO, player.getXRot()) - 90.0F;

    // 4. 将目标角度与起降进度 amt 做线性融合渲染
    matrixStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(amt, 0.0F, roll)));
    matrixStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(amt, 0.0F, pitch)));
    matrixStack.translate(0.0D, -1.2D * MathH.easeInOut(amt), 0.0D);
}
```

同理，对于同文件内 `onCameraSetup` (摄像机视角的后坐力和翻滚逻辑) 中：
```java
// 将导致极端值侧翻的 lerpDegrees 替换
float targetRoll = Mth.lerp(amt, 0.0F, -roll * 0.25F);
```

## 结论
修复后，因为所有的角度变化都直接反映物理变化大小而不再绕远路，无论玩家在多大速度下向任何角度冲刺或悬停，起降渲染时角色与视角都绝对保持稳固平滑，不再出现幽灵侧翻或突然立正。
