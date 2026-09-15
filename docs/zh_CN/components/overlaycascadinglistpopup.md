---
title: OverlayCascadingListPopup
requiresScaffoldHost: true
prerequisites:
  - 必须在 `Scaffold` 内使用以提供 `MiuixPopupHost`
  - 在 `Scaffold` 外使用会导致弹窗内容不渲染
  - 支持多个嵌套或并列的 `Scaffold`，无需额外配置
hostComponent: Scaffold
popupHost: MiuixPopupHost
---

# OverlayCascadingListPopup

`OverlayCascadingListPopup` 是支持二级菜单的弹出列表组件。`DropdownItem.children` 非空的项会成为子菜单触发器：点击后弹窗会以触发行为锚点变形为二级列表，主列表则缩小并被半透明遮罩覆盖。级联深度限制为 **2 级**。

<div style="position: relative; height: 410px; border-radius: 10px; overflow: hidden; border: 1px solid #777;">
    <iframe id="demoIframe" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: none;" src="../../compose/index.html?id=overlayCascadingListPopup" title="Demo" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin"></iframe>
</div>

::: danger 前置条件
此组件依赖于 `Scaffold` 提供的 `MiuixPopupHost` 来渲染弹窗内容。必须在 `Scaffold` 内部使用，否则弹窗内容将无法正常渲染。
:::

## 引入

### OS4 玻璃二级菜单

可选模块 `miuix-glass` 提供 `GlassSecondaryPopup`，可在同一个根 `Box` 中配合
`GlassTransformPopup` 使用。这是独立的玻璃 API，不改变
`OverlayCascadingListPopup` 或其 `surface` 接口。

二级面板从触发行加上下内容留白的矩形展开，不淡入淡出或模糊行内容，玻璃材质的
透明效果保持不变；四条裁剪边同时
运动，行内容保持原尺寸。面板按起始边对齐（RTL 镜像），下方空间不足时整体上移。
展开使用原生默认弹簧（0.95 / 0.35 秒），收起使用 0.95 / 0.2 秒。一级菜单通过
`stacked` 状态缩至 0.95 并加遮罩，采用相同的展开／收起时序。

```kotlin
GlassSecondaryPopup(
    show = submenuExpanded,
    onDismissRequest = { submenuExpanded = false },
    anchorBounds = frozenTriggerBounds,
    backdrop = secondaryBackdrop,
    materialAnchor = menuAnchor, // 与 GlassTransformPopup 使用同一锚点
    sizing = GlassPopupSizing(minWidth = primaryMenuWidth),
    onDismissFinished = { submenuPresent = false },
) {
    GlassPopupItem(text = "排序方式", onClick = { submenuExpanded = false })
    GlassPopupItem(text = "名称", onClick = { /* 选择 */ })
}
```

收起期间应保留组件调用。展开前冻结触发行坐标，直到 `onDismissFinished` 后再恢复
坐标采集，不能在 `show` 刚变为 false 时恢复。传入一级按钮的 `materialAnchor`
可共享模糊、混色与高光；透明度和阴影仍取自 `visuals`。显式传入的 `backdrop`
优先于锚点背景，传 null 时才回退到锚点背景。返回键和外部点击请求收起，选项回调自行决定保留
或关闭菜单；打开动画期间允许行点击，请求关闭后立即禁止，包括一级 `GlassTransformPopup` 缩回按钮的过程。
二级菜单的显示应以一级菜单仍打开为前提，不能反过来重新打开一级菜单。
已有的 `GlassPopup(secondary = true)` 签名
会转入此几何实现，但不提供锚点材质继承。完整配对示例见 `GlassPage`。

在包住页面和一级菜单的 `Box` 上使用 `Modifier.layerBackdrop(secondaryBackdrop)`，
并将二级菜单放在这个 `Box` 外，再传入 `backdrop = secondaryBackdrop`。这样二级模糊
会包含一级菜单的内容、缩放和遮罩，同时不会采样自身。完整结构见 `GlassPage`。
一级遮罩独立执行透明度弹簧：展开 0.95 / 0.35 秒，收起 0.95 / 0.2 秒；
停止阈值为 `(1 / 256) × 0.75`，透明度不超过 `1 / 256` 时隐藏，与原实现一致。

预测返回优先预览二级菜单收起；没有二级菜单时，预览一级菜单缩回按钮。
取消手势时弹回展开状态，完成手势后从当前进度继续关闭。共用的
`materialAnchor` 会同步一级菜单的缩放和遮罩。自定义子菜单箭头可通过
`arrowRotation = { rotation * (1f - menuAnchor.secondaryBackProgress) }`
跟随相同进度。宿主需要提供导航事件分发器，才能接收返回手势。

弹出层在透明度变化时会保留完整阴影所需的绘制范围，包括预测返回到页面顶部无可见材质的按钮时，避免阴影被面板矩形裁切。

### Overlay 引入

```kotlin
import top.yukonga.miuix.kmp.overlay.OverlayCascadingListPopup
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
```

## 基本用法

构造 `entries`，让某些 `DropdownItem` 携带非空的 `children` 列表。组件会在这些触发行的尾部渲染向右的 chevron；点击触发行后会就地展开二级菜单。

```kotlin
var showPopup by remember { mutableStateOf(false) }
var sortIndex by remember { mutableStateOf(0) }
var viewIndex by remember { mutableStateOf(0) }

val sortLabels = listOf("按拍摄日期排序", "按添加日期排序")
val viewLabels = listOf("按日期分组", "紧凑视图")
val entries = listOf(
    DropdownEntry(
        items = sortLabels.mapIndexed { idx, label ->
            DropdownItem(
                text = label,
                selected = sortIndex == idx,
                onClick = { sortIndex = idx },
            )
        },
    ),
    DropdownEntry(
        items = listOf(
            DropdownItem(
                text = "查看模式",
                children = viewLabels.mapIndexed { idx, label ->
                    DropdownItem(
                        text = label,
                        selected = viewIndex == idx,
                        onClick = { viewIndex = idx },
                    )
                },
            ),
        ),
    ),
)

Scaffold {
    Box {
        TextButton(
            text = "点击显示菜单",
            onClick = { showPopup = true },
        )
        OverlayCascadingListPopup(
            show = showPopup,
            entries = entries,
            onDismissRequest = { showPopup = false },
        )
    }
}
```

::: tip 级联深度
级联深度上限为 2。二级菜单中的项不会再处理自己的 `children`；更深层的子树会被静默忽略。
:::

## 组件状态

### 不同的对齐方式

```kotlin
OverlayCascadingListPopup(
    show = showPopup,
    entries = entries,
    onDismissRequest = { showPopup = false },
    alignment = PopupPositionProvider.Align.Start,
)
```

### 禁用窗口变暗

```kotlin
OverlayCascadingListPopup(
    show = showPopup,
    entries = entries,
    onDismissRequest = { showPopup = false },
    enableWindowDim = false,
)
```

### 选中后保持弹窗打开

默认情况下，用户点击任何一个叶子项后弹窗会自动关闭。设为 `collapseOnSelection = false` 即可保持弹窗打开（例如二级菜单内的多选）。

```kotlin
OverlayCascadingListPopup(
    show = showPopup,
    entries = entries,
    onDismissRequest = { showPopup = false },
    collapseOnSelection = false,
)
```

## 属性

### OverlayCascadingListPopup

| 属性名                | 类型                        | 说明                                                                                                            | 默认值                                     |
| --------------------- | --------------------------- | -------------------------------------------------------------------------------------------------------------- | ------------------------------------------ |
| show                  | Boolean                     | 是否显示弹窗                                                                                                   | -                                          |
| entries               | List\<DropdownEntry>        | 由分割线分组的下拉条目；顶层中 `children` 非空的项会成为子菜单触发行                                            | -                                          |
| onDismissRequest      | () -> Unit                  | 用户请求关闭（点击外部、触发返回等）时调用                                                                     | -                                          |
| popupModifier         | Modifier                    | 应用于弹窗主体的修饰符                                                                                         | Modifier                                   |
| onDismissFinished     | (() -> Unit)?               | 退出动画结束后调用                                                                                             | null                                       |
| popupPositionProvider | PopupPositionProvider       | 主弹窗相对锚点的定位策略                                                                                       | ListPopupDefaults.DropdownPositionProvider |
| alignment             | PopupPositionProvider.Align | 主弹窗的对齐方式                                                                                               | PopupPositionProvider.Align.End            |
| enableWindowDim       | Boolean                     | 弹窗显示时是否变暗背景                                                                                         | true                                       |
| maxHeight             | Dp?                         | 任一面的最大高度。null 时由安全区限制                                                                          | null                                       |
| minWidth              | Dp                          | 弹窗的最小宽度                                                                                                 | 200.dp                                     |
| renderInRootScaffold  | Boolean                     | 是否在根（最外层）Scaffold 中渲染弹窗。为 true 时，弹窗覆盖全屏。为 false 时，在当前 Scaffold 的范围内渲染并进行位置补偿 | true                                       |
| dropdownColors        | DropdownColors              | 每一行使用的颜色配置                                                                                           | DropdownDefaults.dropdownColors()          |
| collapseOnSelection   | Boolean                     | 为 true 时，选中任何叶子项后即关闭弹窗                                                                         | true                                       |

### DropdownEntry

| 属性名  | 类型                | 说明                                                                              | 默认值 | 是否必须 |
| ------- | ------------------- | --------------------------------------------------------------------------------- | ------ | -------- |
| items   | List\<DropdownItem> | 此分组中显示的条目                                                                | -      | 是       |
| enabled | Boolean             | 此分组是否启用。为 false 时禁用整组条目；为 true 时仍会遵循每个条目的 enabled 状态 | true   | 否       |

### DropdownItem

| 属性名   | 类型                              | 说明                                                                          | 默认值 | 是否必须 |
| -------- | --------------------------------- | ----------------------------------------------------------------------------- | ------ | -------- |
| text     | String                            | 选项显示的文本                                                                | -      | 是       |
| enabled  | Boolean                           | 选项是否可点击，禁用时置灰                                                    | true   | 否       |
| selected | Boolean                           | 选项是否处于选中状态                                                          | false  | 否       |
| onClick  | (() -> Unit)?                     | 点击选项时触发的回调。当 `children` 非空时被忽略（点击会改为展开二级菜单）    | null   | 否       |
| icon     | @Composable ((Modifier) -> Unit)? | 显示在选项文本前的图标                                                        | null   | 否       |
| summary  | String?                           | 显示在选项文本下方的摘要文本                                                  | null   | 否       |
| children | List\<DropdownItem>?              | 可选的子菜单项；仅级联变体会将其渲染为二级菜单（最多两级）                    | null   | 否       |

### DropdownColors

| 属性名                 | 类型  | 说明             |
| ---------------------- | ----- | ---------------- |
| contentColor           | Color | 选项标题颜色     |
| summaryColor           | Color | 选项摘要颜色     |
| containerColor         | Color | 选项背景颜色     |
| selectedContentColor   | Color | 选中项标题颜色   |
| selectedSummaryColor   | Color | 选中项摘要颜色   |
| selectedContainerColor | Color | 选中项背景颜色   |
| selectedIndicatorColor | Color | 选中指示图标颜色 |

### PopupPositionProvider.Align

| 值          | 说明                         |
| ----------- | ---------------------------- |
| Start       | 将弹窗对齐到锚点的起始端     |
| End         | 将弹窗对齐到锚点的结束端     |
| TopStart    | 将弹窗对齐到锚点的顶部起始端 |
| TopEnd      | 将弹窗对齐到锚点的顶部结束端 |
| BottomStart | 将弹窗对齐到锚点的底部起始端 |
| BottomEnd   | 将弹窗对齐到锚点的底部结束端 |
