# TopAppBar

`TopAppBar` 是 Miuix 中的顶部应用栏组件，用于在界面顶部提供导航、标题和操作按钮。支持大标题模式和普通模式，以及滚动时的动态效果。

此组件通常与 `Scaffold` 组件结合使用，以便在应用程序的不同页面中保持一致的布局和行为。

<div style="position: relative; height: 300px; border-radius: 10px; overflow: hidden; border: 1px solid #777;">
    <iframe id="demoIframe" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: none;" src="../../compose/index.html?id=topAppBar" title="Demo" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin"></iframe>
</div>

## 引入

```kotlin
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
```

## 基本用法

### 玻璃顶栏材质显示

可选模块 `miuix-glass` 中的 `GlassTopAppBar` 提供带必填参数 `isContentScrolled`
的独立重载。传入 `listState.canScrollBackward` 后，页面离开顶部时开始显示材质，
回到顶部时反向退出，即使大标题仍处于收起状态也能正确恢复。顶栏遮罩使用 100ms
线性过渡，导航按钮与操作按钮的表面共用 350ms 线性过渡；保留原有 Compose 阴影
外观及随滚动变化的阴影进度。

顶部两侧按钮的边距均为 `GlassTopAppBarDefaults.HorizontalPadding`（12dp）。
页面卡片和标签复用此值，即可对齐外边缘。

```kotlin
GlassTopAppBar(
    title = "Glass",
    isContentScrolled = listState.canScrollBackward,
    backdrop = backdrop,
    scrollBehavior = scrollBehavior,
    navigationIcon = {
        GlassIconButton(onClick = { navigator.pop() }) {
            Icon(
                imageVector = MiuixIcons.Back,
                contentDescription = "Back",
                modifier = Modifier.size(20.dp),
            )
        }
    },
)
```

`navigationIcon` 和 `actions` 都直接绘制调用方内容。两侧均使用 `GlassIconButton`
即可获得一致的按压反馈，导航栏不会再额外包裹玻璃背景。按钮继承导航栏的材质、
尺寸、形状、填充、描边和阴影默认值。原有带 clickable 的导航图标应改为
`GlassIconButton(onClick = { navigator.pop() }) { Icon(...) }`。

不带 `isContentScrolled` 的原重载仍然可用，通过 `scrollBehavior.state.contentOffset`
推导显示状态。能够获取列表准确顶部位置时，建议使用显式重载。以 `GlassIconButton`
为锚点的变换菜单会自动继承按钮材质及当前表面透明度。

### 小标题顶部栏

```kotlin
Scaffold(
    topBar = {
        SmallTopAppBar(
            title = "标题",
            navigationIcon = {
                IconButton(onClick = { /* 处理点击事件 */ }) {
                    Icon(MiuixIcons.Back, contentDescription = "返回")
                }
            },
            actions = {
                IconButton(onClick = { /* 处理点击事件 */ }) {
                    Icon(MiuixIcons.More, contentDescription = "更多")
                }
            }
        )
    }
)
```

### 大标题顶部栏

```kotlin
Scaffold(
    topBar = {
        TopAppBar(
            title = "标题",
            largeTitle = "大标题", // 如果不指定，将使用 title 的值
            navigationIcon = {
                IconButton(onClick = { /* 处理点击事件 */ }) {
                    Icon(MiuixIcons.Back, contentDescription = "返回")
                }
            },
            actions = {
                IconButton(onClick = { /* 处理点击事件 */ }) {
                    Icon(MiuixIcons.More, contentDescription = "更多")
                }
            }
        )
    }
)
```

## 大标题顶部栏滚动行为（使用脚手架）

TopAppBar 支持随内容滚动时改变其显示状态：

```kotlin
val scrollBehavior = MiuixScrollBehavior()

Scaffold(
    topBar = {
        TopAppBar(
            title = "标题",
            largeTitle = "大标题", // 如果不指定，将使用 title 的值
            scrollBehavior = scrollBehavior
        )
    }
) { paddingValues ->
    // 内容区域需要考虑 padding
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            // 如需添加越界回弹效果，则应在绑定滚动行为之前添加
            .overScrollVertical()
            // 绑定 TopAppBar 滚动事件
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentPadding = PaddingValues(top = paddingValues.calculateTopPadding())
    ) {
        // 列表内容
    }
}
```

## 自定义样式

### 自定义颜色

```kotlin
TopAppBar(
    title = "标题",
    color = MiuixTheme.colorScheme.primary,
    titleColor = MiuixTheme.colorScheme.onPrimary,
    largeTitleColor = MiuixTheme.colorScheme.onPrimary
)
```

### 自定义内容边距

```kotlin
TopAppBar(
    title = "标题",
    titlePadding = 32.dp
)
```

### 自定义图标边距

```kotlin
TopAppBar(
    title = "标题",
    navigationIconPadding = 12.dp,
    actionIconPadding = 12.dp
)
```

## 属性

### TopAppBar 属性

| 属性名                     | 类型                            | 说明                           | 默认值                            | 是否必须 |
| -------------------------- | ------------------------------- | ------------------------------ | --------------------------------- | -------- |
| title                      | String                          | 顶部栏标题                     | -                                 | 是       |
| modifier                   | Modifier                        | 应用于顶部栏的修饰符           | Modifier                          | 否       |
| color                      | Color                           | 顶部栏背景颜色                 | MiuixTheme.colorScheme.surface    | 否       |
| titleColor                 | Color                           | 折叠时小标题文字颜色           | MiuixTheme.colorScheme.onSurface  | 否       |
| largeTitle                 | String                          | 大标题文本                     | title                             | 否       |
| largeTitleColor            | Color                           | 展开时大标题文字颜色           | MiuixTheme.colorScheme.onSurface  | 否       |
| subtitle                   | String                          | 显示在标题栏下方的副标题文本   | ""                                | 否       |
| subtitleColor              | Color                           | 副标题文字颜色                 | MiuixTheme.colorScheme.onSurfaceVariantSummary  | 否       |
| navigationIcon             | @Composable () -> Unit          | 导航图标区域的可组合函数       | {}                                | 否       |
| actions                    | @Composable RowScope.() -> Unit | 操作按钮区域的可组合函数       | {}                                | 否       |
| scrollBehavior             | ScrollBehavior?                 | 控制顶部栏滚动行为             | null                              | 否       |
| defaultWindowInsetsPadding | Boolean                         | 是否应用默认窗口边距           | true                              | 否       |
| titlePadding          | Dp                              | 水平内容边距                   | TopAppBarDefaults.TitlePadding | 否       |
| navigationIconPadding      | Dp                              | 导航图标的起始边距             | TopAppBarDefaults.NavigationIconPadding | 否       |
| actionIconPadding          | Dp                              | 操作图标的末尾边距             | TopAppBarDefaults.ActionIconPadding | 否       |
| bottomContent              | @Composable () -> Unit          | 显示在标题栏下方的可组合内容 | {}                                | 否       |

### SmallTopAppBar 属性

| 属性名                     | 类型                            | 说明                     | 默认值                            | 是否必须 |
| -------------------------- | ------------------------------- | ------------------------ | --------------------------------- | -------- |
| title                      | String                          | 顶部栏标题               | -                                 | 是       |
| modifier                   | Modifier                        | 应用于顶部栏的修饰符     | Modifier                          | 否       |
| color                      | Color                           | 顶部栏背景颜色           | MiuixTheme.colorScheme.surface    | 否       |
| titleColor                 | Color                           | 标题文字颜色             | MiuixTheme.colorScheme.onSurface  | 否       |
| subtitle                   | String                          | 显示在标题栏下方的副标题文本 | ""                                | 否       |
| subtitleColor              | Color                           | 副标题文字颜色           | MiuixTheme.colorScheme.onSurfaceVariantSummary  | 否       |
| navigationIcon             | @Composable () -> Unit          | 导航图标区域的可组合函数 | {}                                | 否       |
| actions                    | @Composable RowScope.() -> Unit | 操作按钮区域的可组合函数 | {}                                | 否       |
| scrollBehavior             | ScrollBehavior?                 | 控制顶部栏滚动行为       | null                              | 否       |
| defaultWindowInsetsPadding | Boolean                         | 是否应用默认窗口边距     | true                              | 否       |
| titlePadding          | Dp                              | 水平内容边距             | TopAppBarDefaults.TitlePadding | 否       |
| navigationIconPadding      | Dp                              | 导航图标的起始边距       | TopAppBarDefaults.NavigationIconPadding | 否       |
| actionIconPadding          | Dp                              | 操作图标的末尾边距       | TopAppBarDefaults.ActionIconPadding | 否       |
| bottomContent              | @Composable () -> Unit          | 显示在标题栏下方的可组合内容 | {}                                | 否       |

### TopAppBarDefaults 对象

TopAppBarDefaults 对象提供了 TopAppBar 和 SmallTopAppBar 组件的默认值。

#### 常量

| 常量名                    | 类型 | 说明                              | 默认值 |
| ------------------------- | ---- | --------------------------------- | ------ |
| TitlePadding    | Dp   | 标题和大标题的水平内边距          | 26.dp  |
| NavigationIconPadding      | Dp   | 导航图标的起始边距               | 16.dp  |
| ActionIconPadding          | Dp   | 操作图标的末尾边距               | 16.dp  |
| CollapsedHeight            | Dp   | TopAppBar 折叠时的高度           | 52.dp  |
| SmallTopAppBarCenterHeight | Dp   | SmallTopAppBar 布局的垂直中心高度 | 50.dp  |
| LargeTitleBottomPadding    | Dp   | 无副标题时大标题下方的底部边距   | 4.dp   |
| SubtitleBottomPadding      | Dp   | 副标题下方的底部边距（大小标题均适用） | 8.dp   |

### ScrollBehavior

MiuixScrollBehavior 是用于控制顶部栏滚动行为的配置对象。

#### rememberTopAppBarState

用于创建和记住 TopAppBarState：

```kotlin
val scrollBehavior = MiuixScrollBehavior(
    state = rememberTopAppBarState(),
    snapAnimationSpec = spring(stiffness = 2500f),
    canScroll = { true }
)
```

| 参数名             | 类型                        | 默认值                     | 说明                       |
| ------------------ | --------------------------- | -------------------------- | -------------------------- |
| state              | TopAppBarState              | rememberTopAppBarState()   | 控制滚动状态的状态对象     |
| canScroll          | () -> Boolean               | { true }                   | 控制是否允许滚动的回调     |
| snapAnimationSpec  | AnimationSpec\<Float>?      | spring(stiffness = 2500f)  | 定义顶部栏滚动后的吸附动画 |
| flingAnimationSpec | DecayAnimationSpec\<Float>? | rememberSplineBasedDecay() | 定义顶部栏滑动的衰减动画   |

## 进阶用法

### 处理窗口边距

```kotlin
TopAppBar(
    title = "标题",
    largeTitle = "大标题",
    defaultWindowInsetsPadding = false // 自行处理窗口嵌入边距
)
```

### 自定义滚动行为动画

```kotlin
var isScrollingEnabled by remember { mutableStateOf(true) }
val scrollBehavior = MiuixScrollBehavior(
    snapAnimationSpec = tween(durationMillis = 100),
    flingAnimationSpec = rememberSplineBasedDecay(),
    canScroll = { isScrollingEnabled } // 可以动态控制是否允许滚动
)

TopAppBar(
    title = "标题",
    largeTitle = "大标题",
    scrollBehavior = scrollBehavior
)
```

### 大标题和小标题结合使用

```kotlin
var useSmallTopBar by remember { mutableStateOf(false) }

Box(modifier = Modifier.fillMaxSize()) {
    if (useSmallTopBar) {
        SmallTopAppBar(
            title = "精简模式",
            navigationIcon = {
                IconButton(onClick = { useSmallTopBar = false }) {
                    Icon(
                        imageVector = MiuixIcons.Back,
                        contentDescription = "切换到大标题",
                        tint = MiuixTheme.colorScheme.onBackground
                    )
                }
            }
        )
    } else {
        TopAppBar(
            title = "标题",
            largeTitle = "展开模式",
            navigationIcon = {
                IconButton(onClick = { useSmallTopBar = true }) {
                    Icon(
                        imageVector = MiuixIcons.Back,
                        contentDescription = "切换到小标题",
                        tint = MiuixTheme.colorScheme.onBackground
                    )
                }
            }
        )
    }
}
```

### 玻璃顶栏的搜索过渡

`GlassTopAppBar` 提供 `contentModifier: Modifier = Modifier`，用于独立变换前景，
保持背景遮罩不动。搜索模式的缩放、透明度和模糊应放在 `contentModifier` 上；
`modifier` 会变换整个顶栏，包括遮挡滚动内容的背景带。

`GlassTopAppBar` 和 `BlurTopAppBar` 均支持 `titleAlpha: () -> Float = { 1f }`。
该值在绘制阶段读取，与现有的折叠透明度相乘，作用于小标题、大标题及副标题容器。

```kotlin
GlassTopAppBar(
    title = "Glass",
    contentModifier = Modifier.graphicsLayer { alpha = foregroundAlpha.value },
    titleAlpha = { titleOpacity.value },
)
```
