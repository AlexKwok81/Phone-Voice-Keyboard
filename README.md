# 🎹 PhoneMic Keyboard

把手机变成 PC 的无线键盘、鼠标和麦克风。手机通过 WiFi 连接 PC，发送按键指令、鼠标控制和语音音频。

![Android](https://img.shields.io/badge/Android-8.0%2B-green)
![Python](https://img.shields.io/badge/Python-3.8%2B-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

## ✨ 功能特性

### 键盘控制
- 🎹 **全键盘**：支持字母、数字、符号输入
- ⌨️ **功能键**：Esc、Tab、Backspace、Delete、Enter
- 🎯 **方向键**：上下左右方向控制
- 🔧 **修饰键**：Ctrl、Alt、Shift、Win 组合键支持

### 鼠标控制
- 🖱️ **触控板模式**：手指滑动控制光标，点击左键/右键
- 🔄 **滚轮控制**：右侧独立滚轮区域，上下滑动滚动页面
- 📡 **空中鼠标**：倾斜手机控制光标（需在设置中开启）
- ⚙️ **灵敏度调节**：可调整鼠标灵敏度和死区阈值

### 语音输入
- 🎤 **实时语音**：按住麦克风按钮，语音实时传输到 PC
- ⌨️ **自定义触发键**：支持配置语音输入的快捷键组合（默认 Ctrl+Win）

### 其他特性
- 🔄 **自动重连**：连接断开后自动尝试恢复
- 📱 **简洁界面**：基于 Jetpack Compose 的 Material 3 界面
- ⚙️ **设置持久化**：所有配置自动保存

## 📸 截图

| 主界面 | 设置页面 |
|--------|----------|
| 键盘 + 触控板 + 滚轮 | 服务器连接 + 语音触发键 + 鼠标设置 |

## 🏗️ 架构

```
手机 (Android App)  <--WebSocket-->  PC (Python Server)
     |                                     |
     |-- 发送按键指令 -->                   |-- pynput 模拟按键
     |-- 发送鼠标移动 -->                   |-- pyautogui 控制光标
     |-- 发送音频数据 -->                   |-- VB-Audio Cable 播放
```

## 📋 前置条件

### PC 端
- Python 3.8+
- [VB-Audio Virtual Cable](https://vb-audio.com/Cable/)（用于语音输入功能）

### 手机端
- Android 8.0+ (API 26)

## 🚀 安装与运行

### 1. 启动 PC 服务

**方式一：直接运行 exe（推荐，无需安装 Python）**

从 [Releases](../../releases) 页面下载最新版 `PhoneMicServer.exe`，双击运行即可。

**方式二：从源码运行**

```bash
cd pc_server
pip install -r requirements.txt
python main.py
```

或者直接双击 `pc_server/启动PC服务.bat` 启动。

服务启动后会显示 PC 的局域网 IP 地址：

```
================================
  PhoneMic Keyboard Server
================================
  PC IP: 192.168.1.100
  Port: 8765
  手机连接: 192.168.1.100:8765
================================
```

### 2. 构建并安装 Android 应用

```bash
cd android_app
./gradlew assembleDebug
# APK 位于 app/build/outputs/apk/debug/app-debug.apk
```

或者直接安装预编译的 APK（如果有的话）。

### 3. 连接

1. 确保手机和 PC 在同一局域网
2. 打开 Android 应用
3. 点击右上角齿轮图标进入设置
4. 输入 PC 显示的地址（如 `192.168.1.100:8765`）
5. 点击「连接」

## 📖 使用说明

### 键盘模式
- **字母键**：点击对应按钮发送按键
- **修饰键**：点击 Ctrl/Alt/Shift/Win 切换激活状态，再按字母键组合使用
- **方向键**：点击方向键移动光标或在游戏中使用

### 鼠标模式
- **触控板**：在左侧区域滑动控制光标移动
- **左键/右键**：点击对应按钮或在触控板上点击/长按
- **滚轮**：在右侧区域上下滑动滚动页面
- **空中鼠标**：在设置中开启后，倾斜手机控制光标

### 语音输入
- **按住 🎤 按钮**：开始录音，语音实时传输到 PC
- **松开按钮**：停止录音
- **自定义触发键**：在设置中配置语音输入的快捷键组合

### 设置
点击右上角齿轮图标进入设置：
- **服务器连接**：配置 PC 服务器地址
- **语音触发键**：选择语音输入的快捷键组合
- **鼠标功能**：开启/关闭鼠标功能，选择模式，调整灵敏度

## 📁 项目结构

```
phone_keybord/
├── android_app/                  # Android 应用
│   └── app/src/main/java/com/phonemic/keyboard/
│       ├── MainActivity.kt       # 主 Activity 和 ViewModel
│       ├── KeyboardUI.kt         # 键盘和设置 UI 组件
│       ├── SettingsScreen.kt     # 设置数据类和持久化
│       ├── TouchPadMode.kt       # 触控板和滚轮组件
│       ├── ConnectionManager.kt  # WebSocket 连接管理
│       ├── PhoneMicClient.kt     # WebSocket 客户端
│       ├── AudioRecorder.kt      # 音频录制
│       ├── AirMouseService.kt    # 空中鼠标（陀螺仪）
│       └── ConnectionStatus.kt   # 连接状态枚举
├── pc_server/                    # PC 服务端
│   ├── main.py                   # 入口
│   ├── server.py                 # WebSocket 服务器
│   ├── keyboard.py               # 键盘模拟
│   ├── mouse.py                  # 鼠标控制
│   ├── requirements.txt          # Python 依赖
│   ├── 启动PC服务.bat             # Windows 启动脚本
│   └── tools/                    # 工具脚本
├── archive/                      # 临时文件（已忽略）
└── README.md
```

## ❓ 故障排除

| 问题 | 解决方案 |
|------|----------|
| 手机无法连接 | 检查 PC 防火墙是否放行 8765 端口 |
| 语音无声 | 确认 VB-Audio Cable 已安装并设为默认音频设备 |
| 按键无响应 | 检查 PC 服务是否在运行，手机和 PC 是否在同一网络 |
| 连接频繁断开 | 检查网络稳定性，应用内置自动重连机制 |
| 鼠标不移动 | 在设置中开启鼠标功能，选择合适的模式 |
| 语音触发键无效 | 在设置中配置正确的快捷键组合 |

## 🛠️ 开发

### PC 服务端依赖
- `websockets` - WebSocket 服务器
- `pynput` - 键盘模拟
- `pyautogui` - 鼠标控制
- `sounddevice` - 音频设备
- `numpy` - 数值计算

### Android 端
- Kotlin + Jetpack Compose
- Material Design 3
- OkHttp WebSocket

### 构建 exe

```bash
cd pc_server
pip install pyinstaller
pyinstaller --onefile --name "PhoneMicServer" --console main.py
# 生成的 exe 位于 dist/PhoneMicServer.exe
```

## 📄 License

MIT License - 详见 [LICENSE](LICENSE) 文件

## 🙏 致谢

- [VB-Audio Virtual Cable](https://vb-audio.com/Cable/) - 虚拟音频设备
- [pynput](https://pypi.org/project/pynput/) - 键盘鼠标控制
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Android UI 框架



