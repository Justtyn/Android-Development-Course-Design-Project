# Meow · 猫咪 Android 应用

## 项目简介
Meow 是一款面向猫咪爱好者的轻量级 Android 应用，集登录/注册、猫咪档案、图片墙、内置百科与本地 FM 播放于一体，帮助用户在离线场景下也能「云吸猫」。项目以课程设计为起点，突出 Activity + RecyclerView/Adapter + SQLite + 多媒体播放等常见场景，适合作为入门示例或课程作品展示。

<img width="1920" height="842" alt="Meow界面截图" src="https://github.com/user-attachments/assets/acfa327e-9e8d-402b-b2bb-a59ab3b76433" />


Android 工程位于仓库子目录 `Meow/`，所有功能集中在单模块 `:app`。应用内容资源全部内置（`res/drawable` 猫图、`res/raw` 音频、`assets/cat_wiki.html` 百科等），功能体验不依赖外网；但首次 Gradle 同步/构建仍需要联网下载依赖与 Gradle Wrapper。

首页（登录后）提供登录状态展示、功能入口卡片，以及基于 `SharedPreferences` 的「撸猫打卡」连续天数统计。

## 功能特性
- 登录 / 注册：基于 `SQLiteOpenHelper` (`MeowDbHelper`) 的本地账号系统，保存昵称用于首页展示；登录状态保存在 `SharedPreferences`，未登录会被重定向到登录页。
- 首页导航：登录后展示当前用户昵称、退出登录按钮、功能卡片入口，以及「撸猫打卡」连续天数（`SharedPreferences` 记录日期与 streak，跨天续打可累加）。
- 猫咪档案：`CatProfileActivity` 使用 `RecyclerView + CatProfileAdapter` 列表展示 20 只猫的头像、品种、年龄与简介，点击条目弹出 Toast 反馈。
- 猫咪图片墙：`CatPicActivity` 采用 2 列 `GridLayoutManager`，展示 37 张「四月」猫图，点击弹出「正在云吸」提示。
- 猫咪百科：`CatWikiActivity` 通过 `WebView` 加载内置 `assets/cat_wiki.html`（并启用 JavaScript），离线可读。
- 猫咪 FM：`CatFmActivity` 以 `RecyclerView` 列表 + `MediaPlayer` 播放 `res/raw` 内置 20 首音频，支持播放/暂停/继续、10 秒快进/快退、进度条跟踪；页面退出/切后台会释放资源；Adapter 负责高亮当前播放条目与按钮文案。
- UI 与主题：基于 Material 组件和 Edge-to-Edge；夜间/日间主题在 `values` 与 `values-night` 维护。

## 使用流程（体验路径）
1. 首次进入：在登录页点击注册，创建本地账号（数据写入应用私有目录的 SQLite 数据库）。
2. 登录成功：进入首页，可查看昵称/退出登录，并进入猫咪档案、图片墙、百科、FM。
3. 打卡：点击首页「撸猫打卡」卡片，统计连续天数（跨天续打 +1，中断则重置为 1）。

## 技术栈与架构
- 语言与工具：Java 17（`compileOptions` 指定），Gradle Wrapper 8.13（`Meow/gradle/wrapper/gradle-wrapper.properties`），Android Gradle Plugin (AGP) 8.13.1（版本来源 `Meow/gradle/libs.versions.toml`）。
- Android 配置：`compileSdk` 36（`release(36)`）、`targetSdk` 36、`minSdk` 28；`applicationId`/`namespace`：`com.justyn.meow`；`versionCode` 1、`versionName` "1.0"；测试 Runner：`androidx.test.runner.AndroidJUnitRunner`；Release 默认开启 R8 混淆与资源压缩（`minifyEnabled true`、`shrinkResources true`）。
- AndroidX 组件：AppCompat 1.6.1、Activity 1.8.0、ConstraintLayout 2.1.4、RecyclerView 1.4.0。
- Material 组件：当前模块依赖 `com.google.android.material:material` 1.13.0（`libs.material.v1130`）。
- 测试依赖：JUnit 4.13.2、AndroidX Test JUnit 1.1.5、Espresso 3.5.1。
- 架构与页面组织：传统 Activity 驱动，采用 `EdgeToEdge` 适配沉浸式；`RecyclerView + Adapter` 用于档案、图片墙与 FM 列表；`MediaPlayer` 管理音频播放与进度；`SharedPreferences` 负责登录态和打卡；`SQLiteOpenHelper` 维护本地用户表。
- 资源与本地化：大量猫图与音频位于 `res/drawable`、`res/raw`，静态百科位于 `assets`；主题、颜色、字符串在 `values` 与 `values-night` 分层定义。

## 开发与运行环境要求
- Android Studio Koala / Ladybug 及以上（与 AGP 8.13.1 兼容）。
- JDK 17（Android Studio 自带的 Embedded JDK 一般可直接满足）。
- Android SDK：API 36 平台；最低运行 Android 9.0（API 28）。
- 设备/模拟器：建议 Android 10+，2GB+ 内存；FM 播放需扬声器/耳机。

## 快速开始
1. 克隆仓库。
2. 使用 Android Studio 打开 `Meow/`，等待 Gradle 同步完成。
3. 命令行构建（在 `Meow/` 目录下执行）：
   ```bash
   cd Meow
   ./gradlew assembleDebug
   ```
4. 在 IDE 选择设备，直接 Run 运行；或安装 `Meow/app/build/outputs/apk/debug/app-debug.apk`。

## 常见问题
- Gradle 同步提示 SDK 路径错误：可删除 `Meow/local.properties` 后重新 Sync，或在 Android Studio 配置本机 SDK 路径后自动生成。

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
│       │   └── util/MeowPreferences.java  // SharedPreferences：登录态与当前用户
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
以下命令在 `Meow/` 目录下执行：
- `./gradlew assembleDebug`：构建 Debug APK。
- `./gradlew assembleRelease`：构建 Release APK（开启混淆与资源压缩）。
- `./gradlew installDebug`：构建并安装到设备/模拟器。
- `./gradlew test`：运行 JVM 单元测试。
- `./gradlew connectedAndroidTest`：运行仪器化测试（需连接设备/模拟器）。
- `./gradlew lint`：运行 Android Lint。
- `./gradlew clean`：清理构建产物。

## 测试说明
- 测试框架：JUnit4 + AndroidX Test + Espresso。
- 命令行运行单元测试：在 `Meow/` 下执行 `./gradlew test`。
- 仪器化测试：在 Android Studio 选择 `androidTest` 运行，Runner 为 `androidx.test.runner.AndroidJUnitRunner`。

## 权限说明
- `android.permission.INTERNET`：清单中声明了网络权限（`Meow/app/src/main/AndroidManifest.xml`）。本项目百科页面默认加载本地 `assets`；若你计划扩展为在线内容可继续保留，如需严格离线可评估移除。

## 数据与存储
- 本地账号：SQLite 数据库 `meow.db`（表 `user`），由 `MeowDbHelper` 维护。
- 登录态与用户信息：`MeowPreferences`（SharedPreferences）。
- 打卡数据：`meow_checkin_prefs`（SharedPreferences，记录日期与连续天数）。

## 安全提示（课程示例说明）
- 登录/注册为本地示例实现，密码以明文存储在 SQLite 中，仅适用于学习演示；如用于真实场景，请改用安全存储与加密/哈希策略。

## 版本管理与依赖管理说明
- 版本 Catalog：`Meow/gradle/libs.versions.toml` 集中管理 AGP、AndroidX、Material 与测试依赖。
- 仓库来源：`Meow/settings.gradle` 配置 `google()` 和 `mavenCentral()`；顶层 `Meow/build.gradle` 仅引入应用插件。
- 依赖引用：模块 `Meow/app/build.gradle` 通过 `libs.*` 引用依赖；当前使用 `libs.material.v1130`，`libs.material` 对应版本项未被引用（可按需清理/统一）。

## 文档
- `SWEU24025_焦梓豪_Android.doc`：课程设计文档（仓库根目录）。
- `Meow/论文.md`：项目论文/说明（如需）。

## 许可协议
代码以 MIT License 开源，见 `LICENSE`。

> 注：仓库内图片/音频等素材可能来自网络或个人收集，仅用于课程设计与学习展示；如用于公开发布/商用，请自行确认素材版权与授权。
