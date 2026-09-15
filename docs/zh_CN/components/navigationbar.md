# NavigationBar

`NavigationBar` 是 Miuix 中的底部导航栏组件，用于在应用底部创建导航菜单，支持 2 到 5 个导航项，提供不同的显示模式（仅图标、图标和文本、仅选中项显示文本）。

`FloatingNavigationBar` 是一个悬浮样式的底部导航栏组件，同样支持 2 到 5 个导航项，仅显示图标。

这些组件通常与 `Scaffold` 组件结合使用，以便在应用程序的不同页面中保持一致的布局和行为。

<div style="position: relative; height: 300px; border-radius: 10px; overflow: hidden; border: 1px solid #777;">
    <iframe id="demoIframe" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: none;" src="../../compose/index.html?id=navigationBar" title="Demo" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin"></iframe>
</div>

## 引入

```kotlin
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarDefaults
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationBarDisplayMode
```

## 基本用法

### NavigationBar

NavigationBar 组件可用于创建固定在底部的导航菜单：

```kotlin
var selectedIndex by remember { mutableStateOf(0) }
val items = listOf("首页", "我的", "设置")
val icons = listOf(MiuixIcons.VerticalSplit, MiuixIcons.Contacts, MiuixIcons.Settings)

Scaffold(
    bottomBar = {
        NavigationBar {
            items.forEachIndexed { index, label ->
                NavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                    icon = icons[index],
                    label = label
                )
            }
        }
    }
)
```

### FloatingNavigationBar

FloatingNavigationBar 组件可用于创建悬浮在底部的导航菜单：

```kotlin
var selectedIndex by remember { mutableStateOf(0) }
val items = listOf("首页", "我的", "设置")
val icons = listOf(MiuixIcons.VerticalSplit, MiuixIcons.Contacts, MiuixIcons.Settings)

Scaffold(
    bottomBar = {
        FloatingNavigationBar {
            items.forEachIndexed { index, label ->
                FloatingNavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                    icon = icons[index],
                    label = label
                )
            }
        }
    }
)
```

### 自定义颜色

两种导航项都支持通过 `NavigationBarDefaults.navigationBarItemColors()` 创建的 `colors`。例如，使用主题主色显示选中项：

```kotlin
val itemColors = NavigationBarDefaults.navigationBarItemColors(
    unselectedContentColor = MiuixTheme.colorScheme.onSurfaceContainer,
    selectedContentColor = MiuixTheme.colorScheme.primary,
)

NavigationBar {
    items.forEachIndexed { index, label ->
        NavigationBarItem(
            selected = selectedIndex == index,
            onClick = { selectedIndex = index },
            icon = icons[index],
            label = label,
            colors = itemColors,
        )
    }
}
```

在 `FloatingNavigationBar` 内向 `FloatingNavigationBarItem` 传入同样的 `colors = itemColors`，即可自定义其图标颜色。

传入的颜色是基础颜色：未选中时 alpha 乘以 `0.4`，未选中且按下时乘以 `0.6`，选中且按下时乘以 `0.5`。选中且未按下时直接使用传入的颜色。`Color.Transparent` 在所有状态下均保持透明。

## 组件状态

### 选中状态

`NavigationBarItem` 会自动处理选中项的视觉样式，选中项将显示粗体文本并高亮图标/文本。`FloatingNavigationBarItem` 在选中时高亮图标。

## 属性

### NavigationBar 属性

| 属性名                     | 类型                    | 说明                       | 默认值                                | 是否必须 |
| -------------------------- | ----------------------- | -------------------------- | ------------------------------------- | -------- |
| modifier                   | Modifier                | 应用于导航栏的修饰符       | Modifier                              | 否       |
| color                      | Color                   | 导航栏背景颜色             | MiuixTheme.colorScheme.surface        | 否       |
| showDivider                | Boolean                 | 是否显示顶部分割线         | true                                  | 否       |
| defaultWindowInsetsPadding | Boolean                 | 是否应用默认窗口嵌入边距   | true                                  | 否       |
| mode                       | NavigationBarDisplayMode   | 导航项的显示模式           | NavigationBarDisplayMode.IconAndText     | 否       |
| content                    | @Composable RowScope.() | 导航栏的内容               | -                                     | 是       |

### NavigationBarItem 属性

| 属性名 | 类型        | 说明             | 默认值 | 是否必须 |
| ------ | ----------- | ---------------- | ------ | -------- |
| selected | Boolean     | 是否选中         | -      | 是       |
| onClick  | () -> Unit  | 点击时的回调     | -      | 是       |
| icon     | ImageVector | 图标             | -      | 是       |
| label    | String      | 文本标签         | -      | 是       |
| modifier | Modifier    | 应用于导航项的修饰符 | Modifier | 否       |
| enabled  | Boolean     | 是否启用         | true     | 否       |
| colors | NavigationBarItemColors | 图标和文本的基础颜色 | NavigationBarDefaults.navigationBarItemColors() | 否 |
| badge    | (@Composable () -> Unit)? | 显示在该项图标上的可选徽章，通常是一个 `Badge` | null | 否 |

### FloatingNavigationBar 属性

| 属性名                     | 类型                      | 说明                               | 默认值                                  | 是否必须 |
| -------------------------- | ------------------------- | ---------------------------------- | --------------------------------------- | -------- |
| modifier                   | Modifier                  | 应用于导航栏的修饰符               | Modifier                                | 否       |
| color                      | Color                     | 导航栏背景颜色                     | MiuixTheme.colorScheme.surfaceContainer | 否       |
| cornerRadius               | Dp                        | 导航栏的圆角半径                   | FloatingToolbarDefaults.CornerRadius    | 否       |
| horizontalAlignment        | Alignment.Horizontal      | 导航栏在其父容器中的水平对齐方式   | CenterHorizontally                      | 否       |
| horizontalOutSidePadding   | Dp                        | 导航栏外部的水平内边距             | FloatingNavigationBarDefaults.HorizontalOutSidePadding | 否       |
| shadowElevation            | Dp                        | 导航栏的阴影高度                   | FloatingNavigationBarDefaults.ShadowElevation          | 否       |
| showDivider                | Boolean                   | 是否显示导航栏周围的分割线         | false                                   | 否       |
| defaultWindowInsetsPadding | Boolean                   | 是否应用默认窗口嵌入边距           | true                                    | 否       |
| content                    | @Composable () -> Unit    | 导航栏的内容                       | -                                       | 是       |

### FloatingNavigationBarItem 属性

| 属性名 | 类型        | 说明             | 默认值 | 是否必须 |
| ------ | ----------- | ---------------- | ------ | -------- |
| selected | Boolean     | 是否选中         | -      | 是       |
| onClick  | () -> Unit  | 点击时的回调     | -      | 是       |
| icon     | ImageVector | 图标             | -      | 是       |
| label    | String      | 文本标签         | -      | 是       |
| modifier | Modifier    | 应用于导航项的修饰符 | Modifier | 否       |
| enabled  | Boolean     | 是否启用         | true     | 否       |
| colors | NavigationBarItemColors | 图标和文本的基础颜色 | NavigationBarDefaults.navigationBarItemColors() | 否 |
| badge    | (@Composable () -> Unit)? | 显示在该项图标上的可选徽章，通常是一个 `Badge` | null | 否 |

### NavigationBarDefaults 对象

NavigationBarDefaults 对象提供了 NavigationBar 和 NavigationBarItem 组件的默认值。

#### 常量

| 常量名                 | 类型     | 说明                       | 默认值 |
| ---------------------- | -------- | -------------------------- | ------ |
| ItemHeight             | Dp       | 项目高度                   | 64.dp  |
| IconSize               | Dp       | 图标尺寸                   | 26.dp  |
| LabelFontSize          | TextUnit | 标签字号                   | 12.sp  |
| IconTopPadding         | Dp       | 图标顶部内边距             | 8.dp   |
| BottomPadding          | Dp       | 标签底部内边距             | 8.dp   |
| SelectedPressedAlpha   | Float    | 选中项按压时的透明度       | 0.5f   |
| UnselectedPressedAlpha | Float    | 未选中项按压时的透明度     | 0.6f   |
| UnselectedAlpha        | Float    | 未选中项的透明度           | 0.4f   |

#### navigationBarItemColors()

此可组合工厂函数返回通过 `remember` 缓存的 `NavigationBarItemColors`，供两种导航项共用。不传入 `colors` 时保持默认主题效果。

| 参数 | 类型 | 说明 | 默认值 |
| ---- | ---- | ---- | ------ |
| unselectedContentColor | Color | 未选中内容的基础颜色 | MiuixTheme.colorScheme.onSurfaceContainer |
| selectedContentColor | Color | 选中内容的基础颜色 | MiuixTheme.colorScheme.onSurfaceContainer |

### FloatingNavigationBarDefaults 对象

FloatingNavigationBarDefaults 对象提供了 FloatingNavigationBar 和 FloatingNavigationBarItem 组件的默认值。

#### 常量

| 常量名                   | 类型  | 说明                       | 默认值 |
| ------------------------ | ----- | -------------------------- | ------ |
| HorizontalOutSidePadding | Dp    | 外部水平内边距             | 36.dp  |
| ShadowElevation          | Dp    | 阴影高度                   | 1.dp   |
| HorizontalPadding        | Dp    | 内部水平内边距             | 12.dp  |
| ItemSpacing              | Dp    | 项目间距                   | 12.dp  |
| IconSize                 | Dp    | 图标尺寸                   | 28.dp  |
| IconPadding              | Dp    | 图标周围的内边距           | 10.dp  |
| SelectedPressedAlpha     | Float | 选中项按压时的透明度       | 0.5f   |
| UnselectedPressedAlpha   | Float | 未选中项按压时的透明度     | 0.6f   |
| UnselectedAlpha          | Float | 未选中项的透明度           | 0.4f   |

### NavigationBarDisplayMode 枚举

| 值                    | 说明                   |
| --------------------- | ---------------------- |
| IconAndText           | 显示图标和文本         |
| IconOnly              | 仅显示图标             |
| IconWithSelectedLabel | 始终显示图标，仅选中时显示文本 |

## 进阶用法

### NavigationBar

#### 自定义颜色

```kotlin
NavigationBar(
    color = Color.Red.copy(alpha = 0.3f)
) {
    // ... items ...
}
```

#### 无分割线

```kotlin
NavigationBar(
    showDivider = false
) {
    // ... items ...
}
```

#### 处理窗口边距

```kotlin
NavigationBar(
    defaultWindowInsetsPadding = false // 自行处理窗口嵌入边距
) {
    // ... items ...
}
```

### FloatingNavigationBar

#### 自定义颜色和圆角

```kotlin
FloatingNavigationBar(
    color = MiuixTheme.colorScheme.primaryContainer,
    cornerRadius = 28.dp
) {
    // ... items ...
}
```

#### 自定义对齐和边距

```kotlin
FloatingNavigationBar(
    horizontalAlignment = Alignment.Start, // 左对齐
    horizontalOutSidePadding = 16.dp // 设置外部边距
) {
    // ... items ...
}
```

### 结合页面切换使用（使用脚手架）

#### 使用 NavigationBar

```kotlin
val pages = listOf("首页", "我的", "设置")
val icons = listOf(MiuixIcons.VerticalSplit, MiuixIcons.Contacts, MiuixIcons.Settings)
var selectedIndex by remember { mutableStateOf(0) }

Scaffold(
    bottomBar = {
        NavigationBar {
            pages.forEachIndexed { index, label ->
                NavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                    icon = icons[index],
                    label = label
                )
            }
        }
    }
) { paddingValues ->
    // 内容区域需要考虑 padding
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "当前页面：${pages[selectedIndex]}",
            style = MiuixTheme.textStyles.title1
        )
    }
}
```

### GlassNavigationBar 尺寸

可选模块 `miuix-glass` 中的 `GlassNavigationBar` 使用 28dp 图标和上下各 6dp
的内容留白。标签基于主题的 `footnote2` 样式，字号为 11dp；系统字体缩放达到
1.6 时改为 16dp，转换为文字单位时不再重复应用字体缩放。标签最多两行，超出时
显示省略号。`height` 为最小高度（默认 54dp），较长的标签可撑高底栏，默认胶囊
形状跟随实际尺寸。这些默认值不影响 `NavigationBar` 或 `FloatingNavigationBar`。

```kotlin
GlassNavigationBar(
    items = items,
    selectedIndex = selectedIndex,
    onSelect = { selectedIndex = it },
    backdrop = backdrop,
    modifier = Modifier.padding(horizontal = 24.dp),
)
```

GlassPage 示例另行应用四项目宽屏规则：计入项目重叠后的可用宽度超过 400dp 时，
底栏宽度为 344dp。底部导航栏 inset 小于 24dp 时，底部间距为 24dp，否则为
inset 加 8dp。外部位置由调用方控制，组件不自行添加系统栏 inset。

拖动时，指示器通过阻尼 1、响应时间 0.15 秒的弹簧跟随手指，尾部按每次事件的
移动距离拉伸 4 倍，最多 60px；前缘到达底栏边界时停止拉伸。每帧显示的左右边缘
均限制在首尾项目的外边界内，避免弹簧过冲导致越界。跨项目时使用左右
边缘的快慢弹簧，松手后从当前动画位置和速度继续归位，不先跳到手指目标位置。
`onSelect` 仍在按下和拖入其他项目时触发，此调整不包含松手后才提交选择的行为。

#### 使用 FloatingNavigationBar

```kotlin
val pages = listOf("首页", "我的", "设置")
val icons = listOf(MiuixIcons.VerticalSplit, MiuixIcons.Contacts, MiuixIcons.Settings)
var selectedIndex by remember { mutableStateOf(0) }

Scaffold(
    bottomBar = {
        FloatingNavigationBar {
            pages.forEachIndexed { index, label ->
                FloatingNavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                    icon = icons[index],
                    label = label
                )
            }
        }
    }
) { paddingValues ->
    // 内容区域需要考虑 padding
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "当前页面：${pages[selectedIndex]}",
            style = MiuixTheme.textStyles.title1
        )
    }
}
```
