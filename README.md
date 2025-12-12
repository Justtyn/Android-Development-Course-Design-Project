# Meow · 猫咪 Android 应用

## 项目简介
Meow 是一款面向猫咪爱好者的轻量级 Android 应用，集登录/注册、猫咪档案、图片墙、内置百科与本地 FM 播放于一体，帮助用户在离线场景下也能「云吸猫」。项目以课程设计为起点，突出 Activity + RecyclerView/Adapter + 多媒体播放等常见场景，适合作为入门示例或面试作品。

所有功能集中在单模块 `:app`，资源全部内置（`res/drawable` 猫图、`res/raw` 音频、`assets/cat_wiki.html` 百科），无需外网即可体验。首页提供登录状态展示、功能入口卡片，以及基于 `SharedPreferences` 的「撸猫打卡」连续天数统计。

## 功能特性
- 登录 / 注册：基于 `SQLiteOpenHelper` (`MeowDbHelper`) 的本地账号系统，保存昵称用于首页展示；登录状态保存在 `SharedPreferences`，未登录会被重定向到登录页。
- 首页导航：登录后展示当前用户昵称、退出登录按钮、功能卡片入口，以及「撸猫打卡」连续天数（`SharedPreferences` 记录日期与 streak，跨天续打可累加）。
- 猫咪档案：`CatProfileActivity` 使用 `RecyclerView + CatProfileAdapter` 列表展示 20 只猫的头像、品种、年龄与简介，点击条目弹出 Toast 反馈。
- 猫咪图片墙：`CatPicActivity` 采用 2 列 `GridLayoutManager`，展示 30+ 张「四月」猫图，点击弹出「正在云吸」提示。
- 猫咪百科：`CatWikiActivity` 通过 `WebView` 加载内置 `assets/cat_wiki.html`，开启 JavaScript，离线可读。
- 猫咪 FM：`CatFmActivity` 以 `RecyclerView` 列表 + `MediaPlayer` 播放 `res/raw` 内置音频，支持播放/暂停、继续、10 秒快进/快退、进度条跟踪，后台切换自动释放资源，Adapter 高亮当前播放条目。
- UI 与主题：基于 Material 组件和 Edge-to-Edge，内置多分辨率图标、圆角卡片与圆形头像背景，夜间/日间基础色值在 `values` 与 `values-night` 维护。

## 技术栈与架构
- 语言与工具：Java 11，Gradle Wrapper 8.13（`gradle/wrapper/gradle-wrapper.properties`），Android Gradle Plugin (AGP) 8.13.1（版本来源 `gradle/libs.versions.toml`）。
- Android 配置：`compileSdk` 36（release 通道）、`targetSdk` 36、`minSdk` 28；`applicationId`/`namespace`：`com.justyn.meow`；`versionCode` 1、`versionName` "1.0"；测试 Runner：`androidx.test.runner.AndroidJUnitRunner`；Release 关闭混淆（使用默认 ProGuard 模板）。
- AndroidX 组件：AppCompat 1.6.1、Activity 1.8.0、ConstraintLayout 2.1.4、RecyclerView 1.4.0。
- Material 组件：`com.google.android.material:material` 1.10.0 (`libs.material`) 与 1.13.0 (`libs.material.v1130`) 并存，需在后续统一版本或明确分版本使用原因（当前均被依赖）。
- 测试依赖：JUnit 4.13.2、AndroidX Test JUnit 1.1.5、Espresso 3.5.1。
- 架构与页面组织：传统 Activity 驱动，采用 `EdgeToEdge` 适配沉浸式；`RecyclerView + Adapter` 用于档案、图片墙与 FM 列表；`MediaPlayer` 管理音频播放与进度；`SharedPreferences` 负责登录态和打卡；`SQLiteOpenHelper` 维护本地用户表。
- 资源与本地化：大量猫图与音频位于 `res/drawable`、`res/raw`，静态百科位于 `assets`；主题、颜色、字符串在 `values` 与 `values-night` 分层定义。

## 开发与运行环境要求
- Android Studio Ladybug / Koala 及以上（与 AGP 8.13.1 兼容）。
- JDK 11（`compileOptions` 指定）。
- Android SDK：API 36 平台；最低运行 Android 9.0（API 28）。
- 设备/模拟器：建议 Android 10+，2GB+ 内存；FM 播放需扬声器/耳机。

## 快速开始
1. 克隆仓库并进入项目根目录（包含 `Meow` 子目录）。
2. 使用 Android Studio 打开 `Meow/`，等待 Gradle 同步完成（Wrapper 版本 8.13 自动下载）。
3. 如需命令行构建：
   ```bash
   ./gradlew assembleDebug
   ```
4. 在 IDE 选择设备，直接 Run 运行；或使用命令安装 `app/build/outputs/apk/debug/app-debug.apk`。

## 项目结构
```text
Meow/                                     // Android 工程根目录
├── app/
│   ├── build.gradle                      // 模块级配置，依赖与构建参数
│   └── src/main/
│       ├── AndroidManifest.xml           // Activity 声明，Launcher 为 LoginActivity
│       ├── java/com/justyn/meow/
│       │   ├── MainActivity.java         // 登录后首页，功能入口 + 打卡 streak
│       │   ├── auth/
│       │   │   ├── LoginActivity.java    // 登录表单，校验 SQLite 用户并保存登录态
│       │   │   └── RegisterActivity.java // 注册表单，写入 SQLite
│       │   ├── cat/
│       │   │   ├── CatProfileActivity.java   // 猫档案列表（RecyclerView）
│       │   │   ├── CatPicActivity.java       // 猫图网格墙（GridLayoutManager）
│       │   │   ├── CatWikiActivity.java      // WebView 加载 assets/cat_wiki.html
│       │   │   ├── CatFmActivity.java        // FM 播放（MediaPlayer + 进度/快进）
│       │   │   ├── CatProfileAdapter.java    // 档案列表适配器
│       │   │   ├── CatPicAdapter.java        // 图片墙适配器
│       │   │   ├── CatFmAdapter.java         // FM 列表适配器，按钮文案高亮
│       │   │   ├── CatProfile.java | CatPic.java | FmTrack.java // 数据模型
│       │   └── data/MeowDbHelper.java     // SQLiteOpenHelper，user 表
│       ├── res/layout/                    // activity_*.xml、item_* 布局
│       ├── res/drawable/                  // 猫咪图片、圆形头像背景、图标
│       ├── res/raw/                       // FM 音频（20 首本地轨道）
│       ├── res/values/                    // colors/themes/strings，含夜间版
│       └── assets/cat_wiki.html           // 内置猫咪百科页面
├── build.gradle                           // 顶层脚本，仅声明应用插件
├── settings.gradle                        // 仓库配置（google() / mavenCentral()）与模块声明
├── gradle/wrapper/gradle-wrapper.properties // Gradle Wrapper 8.13
├── gradlew | gradlew.bat                  // Wrapper 启动脚本
└── README.md                              // 项目说明
```

## 构建与命令
- `./gradlew assembleDebug`：构建 Debug APK。
- `./gradlew test`：运行 JVM 单元测试。
- `./gradlew lint`：运行 Android Lint 检查潜在问题。
- `./gradlew clean`：清理构建产物。

## 测试说明
- 测试框架：JUnit4 + AndroidX Test + Espresso。
- 命令行运行单元测试：`./gradlew test`。
- 仪器化测试：在 Android Studio 选择 `androidTest` 运行，Runner 为 `androidx.test.runner.AndroidJUnitRunner`。

## 版本管理与依赖管理说明
- 版本 Catalog：`gradle/libs.versions.toml` 集中管理 AGP、AndroidX、Material 与测试依赖。
- 仓库来源：`settings.gradle` 配置 `google()` 和 `mavenCentral()`；顶层 `build.gradle` 仅引入应用插件。
- Material 组件：当前同时依赖 1.10.0 与 1.13.0（`libs.material` 与 `libs.material.v1130`）；请确认兼容性后统一版本，或明确保留两个版本的原因。

## 许可协议
暂未设置具体开源协议，如需使用或二次分发请先联系作者；若要开源，推荐采用 MIT 或 Apache 2.0 许可证。
