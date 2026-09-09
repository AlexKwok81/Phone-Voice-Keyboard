import socket
import time
from pynput.keyboard import Key, Controller

keyboard = Controller()

# Map string names to pynput Key objects
MODIFIER_MAP = {
    "ctrl": Key.ctrl_l,
    "alt": Key.alt_l,
    "shift": Key.shift_l,
    "win": Key.cmd_l,
    "command": Key.cmd_l,
    "meta": Key.cmd_l,
    "ctrl_l": Key.ctrl_l,
    "ctrl_r": Key.ctrl_r,
    "alt_l": Key.alt_l,
    "alt_r": Key.alt_r,
    "shift_l": Key.shift_l,
    "shift_r": Key.shift_r,
    "cmd_l": Key.cmd_l,
    "cmd_r": Key.cmd_r,
}

SPECIAL_KEY_MAP = {
    "enter": Key.enter,
    "return": Key.enter,
    "tab": Key.tab,
    "space": Key.space,
    "backspace": Key.backspace,
    "delete": Key.delete,
    "escape": Key.esc,
    "esc": Key.esc,
    "up": Key.up,
    "down": Key.down,
    "left": Key.left,
    "right": Key.right,
    "home": Key.home,
    "end": Key.end,
    "pageup": Key.page_up,
    "pagedown": Key.page_down,
    "f1": Key.f1, "f2": Key.f2, "f3": Key.f3, "f4": Key.f4,
    "f5": Key.f5, "f6": Key.f6, "f7": Key.f7, "f8": Key.f8,
    "f9": Key.f9, "f10": Key.f10, "f11": Key.f11, "f12": Key.f12,
    "capslock": Key.caps_lock,
    "printscreen": Key.print_screen,
    "scrolllock": Key.scroll_lock,
    "pause": Key.pause,
    "insert": Key.insert,
}


def get_key(name: str):
    name_lower = name.lower()
    if name_lower in MODIFIER_MAP:
        return MODIFIER_MAP[name_lower]
    if name_lower in SPECIAL_KEY_MAP:
        return SPECIAL_KEY_MAP[name_lower]
    if len(name) == 1:
        return name
    return None


def press_key(key_name: str, modifiers: list = None):
    key = get_key(key_name)
    if key is None:
        print(f"  [!] Unknown key: {key_name}")
        return

    held_modifiers = []
    if modifiers:
        for mod in modifiers:
            mod_key = get_key(mod)
            if mod_key:
                keyboard.press(mod_key)
                held_modifiers.append(mod_key)
                time.sleep(0.02)

    keyboard.press(key)
    time.sleep(0.02)
    keyboard.release(key)

    for mod_key in reversed(held_modifiers):
        time.sleep(0.02)
        keyboard.release(mod_key)


def release_key(key_name: str):
    key = get_key(key_name)
    if key:
        try:
            keyboard.release(key)
        except Exception:
            pass


def get_local_ip():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        return "127.0.0.1"
