import sounddevice as sd
import subprocess

# Check current default input device
default = sd.default.device[0]
devices = sd.query_devices()
print(f"Current default input device: {devices[default]['name']} (idx={default})")

# List all input devices
print("\nAll input devices:")
for i, d in enumerate(devices):
    if d['max_input_channels'] > 0:
        marker = " <-- DEFAULT" if i == default else ""
        print(f"  {i}: {d['name']} ch={d['max_input_channels']}{marker}")
