import pyautogui
import logging
import time

log = logging.getLogger("mouse")

pyautogui.FAILSAFE = True
pyautogui.PAUSE = 0

SMOOTH_ALPHA = 0.4
DEAD_ZONE = 0.12
SENSITIVITY_BASE = 30.0
ACCEL_THRESHOLD = 0.5

_prev_dx = 0.0
_prev_dy = 0.0
_calibration_dx = 0.0
_calibration_dy = 0.0
_calibration_samples = []
_calibrating = False
_calibration_start = 0.0


def set_sensitivity(percent: int):
    global SENSITIVITY_BASE
    SENSITIVITY_BASE = 100.0 + percent * 8.0
    log.info("Sensitivity set to %d (base=%.1f)", percent, SENSITIVITY_BASE)


def start_calibration():
    global _calibrating, _calibration_samples, _calibration_start
    _calibrating = True
    _calibration_samples = []
    _calibration_start = time.time()
    log.info("Calibration started")


def _update_calibration(dx: float, dy: float) -> bool:
    global _calibrating, _calibration_dx, _calibration_dy, _calibration_samples
    if not _calibrating:
        return False
    _calibration_samples.append((dx, dy))
    if time.time() - _calibration_start > 0.5 and len(_calibration_samples) >= 10:
        n = len(_calibration_samples)
        _calibration_dx = sum(s[0] for s in _calibration_samples) / n
        _calibration_dy = sum(s[1] for s in _calibration_samples) / n
        _calibrating = False
        log.info("Calibration done: offset=(%.4f, %.4f)", _calibration_dx, _calibration_dy)
        return True
    return False


def handle_air_mouse(dx: float, dy: float):
    global _prev_dx, _prev_dy

    if _update_calibration(dx, dy):
        return

    dx -= _calibration_dx
    dy -= _calibration_dy

    if abs(dx) < DEAD_ZONE and abs(dy) < DEAD_ZONE:
        return

    speed = (dx * dx + dy * dy) ** 0.5
    if speed > ACCEL_THRESHOLD:
        factor = SENSITIVITY_BASE * (1.0 + (speed - ACCEL_THRESHOLD) * 2.0)
    else:
        factor = SENSITIVITY_BASE

    raw_x = dx * factor
    raw_y = dy * factor

    smooth_x = SMOOTH_ALPHA * raw_x + (1 - SMOOTH_ALPHA) * _prev_dx
    smooth_y = SMOOTH_ALPHA * raw_y + (1 - SMOOTH_ALPHA) * _prev_dy
    _prev_dx = smooth_x
    _prev_dy = smooth_y

    try:
        pyautogui.move(int(smooth_x), int(smooth_y), _pause=False)
    except Exception as e:
        log.error("move failed: %s", e)


def handle_mouse_move(dx: int, dy: int):
    try:
        pyautogui.move(dx, dy, _pause=False)
    except Exception as e:
        log.error("mouse_move failed: %s", e)


def handle_mouse_click(button: str = "left"):
    btn = pyautogui.LEFT if button == "left" else pyautogui.RIGHT
    try:
        pyautogui.click(button=btn)
    except Exception as e:
        log.error("click failed: %s", e)


def handle_mouse_scroll(dy: int):
    try:
        pyautogui.scroll(dy)
    except Exception as e:
        log.error("scroll failed: %s", e)
