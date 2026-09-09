# PhoneMic Keyboard

把手机变成 PC 的无线键盘和麦克风。手机通过 WiFi 连接 PC，发送按键指令和语音音频。

## 功能

- 🎹 **无线键盘**：手机充当 PC 键盘，支持全键盘、功能键、方向键、修饰键（Ctrl/Alt/Shift）
- 🎤 **语音输入**：按住手机麦克风按钮，语音实时传输到 PC 音频输入
- 🔄 **自动重连**：连接断开后自动尝试恢复
- 📱 **简洁界面**：基于 Jetpack Compose 的 Material 3 界面

## 架构

`
手机 (Android App)  <--WebSocket-->  PC (Python Server)
     |                                     |
     |-- 发送按键指令 -->                   |-- pynput 模拟按键
     |-- 发送音频数据 -->                   |-- VB-Audio Cable 播放
`

## 前置条件

### PC 端
- Python 3.8+
- [VB-Audio Virtual Cable](https://vb-audio.com/Cable/)（用于语音输入功能）

### 手机端
- Android 8.0+ (API 26)

## 安装与运行

### 1. 启动 PC 服务

`ash
cd pc_server
pip install -r ../requirements.txt
python main.py
`

服务启动后会显示 PC 的局域网 IP 地址，例如：

`
PhoneMic Keyboard Server
PC IP: 192.168.1.100
Port: 8765
Connect from phone: ws://192.168.1.100:8765
`

也可以直接双击 启动PC服务.bat 启动。

### 2. 构建并安装 Android 应用

`ash
cd android_app
./gradlew assembleDebug
# APK 位于 app/build/outputs/apk/debug/
`

### 3. 连接

1. 确保手机和 PC 在同一局域网
2. 打开 Android 应用
3. 输入 PC 显示的地址（如 192.168.1.100:8765）
4. 点击连接

## 使用说明

### 键盘模式
- **字母键**：点击对应按钮发送按键
- **修饰键**：点击 Ctrl/Alt/Shift/Win 切换激活状态，再按字母键组合使用
- **长按修饰键**：按住修饰键按钮，点击字母键，松开后自动取消修饰

### 语音模式
- **按住 🎤 按钮**：开始录音，语音实时传输到 PC
- **松开按钮**：停止录音
- 需要 PC 安装 VB-Audio Cable 才能使用语音功能

## 项目结构

`
phone_keybord/
├── android_app/              # Android 应用
│   └── app/src/main/java/com/phonemic/keyboard/
│       ├── MainActivity.kt       # 主界面和 ViewModel
│       ├── PhoneMicClient.kt     # WebSocket 客户端
│       ├── KeyboardUI.kt         # 键盘 UI 组件
│       └── ConnectionStatus.kt   # 连接状态枚举
├── pc_server/                # PC 服务端
│   ├── main.py               # 入口
│   ├── server.py             # WebSocket 服务器
│   └── keyboard.py           # 按键模拟
├── requirements.txt          # Python 依赖
├── 启动PC服务.bat            # Windows 启动脚本
└── README.md
`

## 故障排除

| 问题 | 解决方案 |
|------|----------|
| 手机无法连接 | 检查 PC 防火墙是否放行 8765 端口 |
| 语音无声 | 确认 VB-Audio Cable 已安装并设为默认音频设备 |
| 按键无响应 | 检查 PC 服务是否在运行，手机和 PC 是否在同一网络 |
| 连接频繁断开 | 检查网络稳定性，应用内置自动重连机制 |

## License

MIT
