import sounddevice as sd
devices = sd.query_devices()
print("=== Audio Devices ===")
for i, d in enumerate(devices):
    ch = f"in={d['max_input_channels']} out={d['max_output_channels']}"
    print(f"{i}: {d['name']}  {ch}")
print("\n=== Looking for VB-Audio Cable ===")
found = False
for i, d in enumerate(devices):
    name = d['name'].lower()
    if 'cable' in name or 'vb-audio' in name or 'vb-audio' in name:
        print(f"  FOUND: {d['name']} (idx={i})")
        found = True
if not found:
    print("  NOT FOUND - VB-Audio Cable is not installed!")
