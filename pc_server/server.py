import json
import logging
import asyncio
import websockets
from keyboard import press_key, release_key, get_key, keyboard
from mouse import handle_air_mouse, handle_mouse_move, handle_mouse_click, handle_mouse_scroll
import sounddevice as sd
import numpy as np

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%H:%M:%S",
)
log = logging.getLogger("server")


class WebSocketServer:
    def __init__(self, host: str, port: int):
        self.host = host
        self.port = port
        self.clients: set = set()
        self.server = None
        self.audio_device = None
        self.audio_stream = None
        self._audio_chunk_count = 0
        self._find_audio_device()

    def _find_audio_device(self):
        try:
            devices = sd.query_devices()
            for i, d in enumerate(devices):
                if "cable input" in d["name"].lower() and d["max_output_channels"] > 0:
                    self.audio_device = i
                    log.info("Audio output: %s (idx=%d)", d["name"], i)
                    return
            log.warning("VB-Audio Cable Input not found - audio mode disabled")
        except Exception as e:
            log.error("Audio device error: %s", e)

    def _ensure_audio_stream(self):
        if self.audio_stream is not None:
            return
        if self.audio_device is None:
            return
        try:
            self.audio_stream = sd.OutputStream(
                samplerate=16000,
                channels=1,
                dtype="float32",
                device=self.audio_device,
                blocksize=320,
            )
            self.audio_stream.start()
            log.info("Audio stream started")
        except Exception as e:
            log.error("Audio stream error: %s", e)

    def _close_audio_stream(self):
        if self.audio_stream is None:
            return
        try:
            self.audio_stream.stop()
            self.audio_stream.close()
        except Exception:
            pass
        self.audio_stream = None
        log.info("Audio stream closed")

    def _write_audio(self, pcm_bytes: bytes):
        if self.audio_device is None:
            return
        try:
            self._ensure_audio_stream()
            if self.audio_stream is None:
                return
            samples = np.frombuffer(pcm_bytes, dtype=np.int16)
            if len(samples) == 0:
                return
            audio = samples.astype(np.float32) / 32768.0
            self.audio_stream.write(audio.reshape(-1, 1))
            self._audio_chunk_count += 1
            if self._audio_chunk_count % 50 == 1:
                log.debug("Audio chunks: %d, samples: %d", self._audio_chunk_count, len(samples))
        except Exception as e:
            log.error("Audio write error: %s", e)

    def _handle_key_down(self, key_name: str):
        key = get_key(key_name)
        if key:
            try:
                keyboard.press(key)
            except Exception as e:
                log.error("key_down failed for %s: %s", key_name, e)

    async def handler(self, websocket):
        self.clients.add(websocket)
        remote = websocket.remote_address
        log.info("Client connected: %s:%s", remote[0], remote[1])

        await websocket.send(json.dumps({
            "type": "connected",
            "device_id": "pc_server",
            "audio_ready": True,
        }))

        try:
            async for message in websocket:
                try:
                    if isinstance(message, bytes):
                        self._write_audio(message)
                        continue

                    data = json.loads(message)
                    msg_type = data.get("type", "")

                    if msg_type == "key":
                        key = data.get("key", "")
                        modifiers = data.get("modifiers", [])
                        press_key(key, modifiers)
                        log.info("Key: %s modifiers=%s", key, modifiers)

                    elif msg_type == "key_down":
                        self._handle_key_down(data.get("key", ""))

                    elif msg_type == "key_up":
                        release_key(data.get("key", ""))

                    elif msg_type == "air_mouse":
                        handle_air_mouse(data.get("dx", 0.0), data.get("dy", 0.0))

                    elif msg_type == "mouse_move":
                        handle_mouse_move(data.get("dx", 0), data.get("dy", 0))

                    elif msg_type == "mouse_click":
                        handle_mouse_click(data.get("button", "left"))

                    elif msg_type == "mouse_scroll":
                        handle_mouse_scroll(data.get("dy", 0))

                    elif msg_type in ("button_down", "button_up"):
                        log.info("Button %s (audio mode)", msg_type.split("_")[1])

                    else:
                        log.warning("Unknown message type: %s", msg_type)

                except json.JSONDecodeError:
                    log.error("Bad JSON: %s", str(message)[:100])
                except Exception as e:
                    log.error("Error: %s", e)

        except websockets.exceptions.ConnectionClosed:
            pass
        finally:
            self.clients.discard(websocket)
            if not self.clients:
                self._close_audio_stream()
            log.info("Client disconnected: %s:%s", remote[0], remote[1])

    async def start(self):
        self.server = await websockets.serve(
            self.handler,
            self.host,
            self.port,
            ping_interval=20,
            ping_timeout=10,
            max_size=10 * 1024 * 1024,
        )
        log.info("WebSocket server listening on ws://%s:%d", self.host, self.port)
        await self.server.wait_closed()

    def run(self):
        asyncio.run(self.start())

    def stop(self):
        if self.server:
            self.server.close()
