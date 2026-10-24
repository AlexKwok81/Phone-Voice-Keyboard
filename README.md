# Phone Voice Keyboard

Turn an Android phone into a wireless keyboard, mouse, and microphone for a Windows PC over your local Wi-Fi network.

![Android](https://img.shields.io/badge/Android-8.0%2B-green)
![Python](https://img.shields.io/badge/Python-3.8%2B-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

## Features

- Full keyboard input, function keys, arrows, and modifier combinations.
- Touchpad mouse control, scrolling, and optional motion based air mouse.
- Live microphone audio streaming to a PC audio device.
- Configurable voice shortcut and optional Enter key after releasing the microphone button.
- Automatic reconnect and persistent app settings.

## Requirements

- Android 8.0 (API 26) or newer.
- Windows PC on the same local network as the phone.
- Python 3.8 or newer for running the PC server from source.
- VB-Audio Virtual Cable for routing microphone audio into PC applications. Keyboard and mouse features do not require it.

## Quick start

### 1. Start the PC server

Install dependencies and run the server:

```powershell
cd pc_server
python -m venv .venv
.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python main.py
```

On Windows, you can also double-click `pc_server/启动PC服务.bat` after installing the dependencies. The server listens on port `8765` and prints the PC's local IP address. Allow Python or port 8765 through Windows Firewall when prompted.

### 2. Build and install the Android app

Open `android_app` in Android Studio, let Gradle sync, then build and install the app. With the Android SDK and Java configured, you can also run:

```powershell
cd android_app
.\gradlew.bat assembleDebug
```

The debug APK is written to `android_app/app/build/outputs/apk/debug/app-debug.apk`.

### 3. Connect

Connect both devices to the same Wi-Fi network. Open the app, go to Settings, enter the PC address shown by the server (for example `192.168.1.100:8765`), then tap **Connect**.

## Usage

- Tap keyboard keys to send input. Toggle Ctrl, Alt, Shift, or Win before a key to send a combination.
- Slide on the touchpad to move the pointer. Use the mouse buttons or touchpad gestures to click; the right-side area scrolls.
- Enable Air Mouse in Settings to control the pointer by tilting the phone.
- Press and hold the microphone button to stream audio; release it to stop. Configure the voice trigger shortcut and optional automatic Enter behavior in Settings.

## Troubleshooting

| Problem | Things to check |
| --- | --- |
| Phone cannot connect | Confirm both devices are on the same network, the server is running, and Windows Firewall allows port 8765. |
| No audio in PC apps | Install VB-Audio Virtual Cable and select the cable as the input device in the target app. |
| Keyboard or mouse does not respond | Keep the server window open and reconnect from the phone. |
| Connection drops | Check Wi-Fi stability and keep the app connected; the client retries automatically. |

## Project layout

```text
android_app/   Android app (Kotlin, Jetpack Compose)
pc_server/    Python WebSocket server and input/audio handling
screenshot/   Project screenshots
```

## Development

See [CONTRIBUTING.md](CONTRIBUTING.md) for development setup and pull request guidelines. The PC server dependencies are listed in `pc_server/requirements.txt`.

## License

This project is licensed under the [MIT License](LICENSE).

## Acknowledgements

- [VB-Audio Virtual Cable](https://vb-audio.com/Cable/)
- [pynput](https://pypi.org/project/pynput/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
