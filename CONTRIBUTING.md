# Contributing

Thanks for considering a contribution to PhoneMic Keyboard.

## Before you start

- Search existing issues to avoid duplicate reports.
- For substantial changes, open an issue first to agree on the scope.
- Keep changes focused and describe user-visible behavior in the pull request.

## Development

### PC server

```powershell
cd pc_server
python -m venv .venv
.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python main.py
```

### Android app

Open `android_app` in Android Studio, or build from the command line on a machine with the Android SDK installed:

```powershell
cd android_app
.\gradlew.bat assembleDebug
```

## Pull requests

- Explain the problem and the chosen approach.
- Include steps used to verify the change and any relevant screenshots.
- Keep generated files, local configuration, and build output out of commits.
- Update the README when setup or user-facing behavior changes.
