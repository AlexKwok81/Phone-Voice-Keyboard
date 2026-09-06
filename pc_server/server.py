import json
import asyncio
import websockets
from keyboard import press_key, release_key
import sounddevice as sd
import numpy as np

class WebSocketServer:
    def __init__(self, host, port):
        self.host = host
        self.port = port
        self.clients = set()
        self.server = None
        self.audio_device = None
        self.audio_stream = None
        self._find_audio_device()

    def _find_audio_device(self):
        try:
            devices = sd.query_devices()
            for i, d in enumerate(devices):
                if "cable input" in d["name"].lower() and d["max_output_channels"] > 0:
                    self.audio_device = i
                    print(f"  Audio output: {d['name']} (idx={i})")
                    return
            print("  [!] VB-Audio Cable Input not found!")
        except Exception as e:
            print(f"  [!] Audio device error: {e}")

    def _ensure_audio_stream(self):
        if self.audio_stream is not None:
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
            print("  Audio stream started")
        except Exception as e:
            print(f"  [!] Audio stream error: {e}")

    def _write_audio(self, pcm_bytes):
        if self.audio_device is None:
            print("  [!] No audio device")
            return
        try:
            self._ensure_audio_stream()
            samples = np.frombuffer(pcm_bytes, dtype=np.int16)
            if len(samples) == 0:
                return
            audio = samples.astype(np.float32) / 32768.0
            self.audio_stream.write(audio.reshape(-1, 1))
            if not hasattr(self, '_audio_chunk_count'):
                self._audio_chunk_count = 0
            self._audio_chunk_count += 1
            if self._audio_chunk_count % 50 == 1:
                print(f"  [~] Audio chunks: {self._audio_chunk_count}, samples: {len(samples)}")
        except Exception as e:
            print(f"  [!] Audio write error: {e}")

    async def handler(self, websocket):
        self.clients.add(websocket)
        remote = websocket.remote_address
        print(f"[+] Client connected: {remote[0]}:{remote[1]}")

        # Send welcome message
        await websocket.send(json.dumps({
            "type": "connected",
            "device_id": "pc_server",
            "audio_ready": True
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
                        print(f"  Key: {key} modifiers={modifiers}")

                    elif msg_type == "key_down":
                        key = data.get("key", "")
                        from keyboard import get_key
                        k = get_key(key)
                        if k:
                            try:
                                from pynput.keyboard import Controller
                                kb = Controller()
                                kb.press(k)
                            except Exception as e:
                                print(f"  [!] press failed: {e}")

                    elif msg_type == "key_up":
                        key = data.get("key", "")
                        release_key(key)

                    elif msg_type == "button_down":
                        print("  [+] Button down (audio mode)")

                    elif msg_type == "button_up":
                        print("  [-] Button up (audio mode)")

                    else:
                        print(f"  [?] Unknown message type: {msg_type}")

                except json.JSONDecodeError:
                    print(f"  [!] Bad JSON: {message[:100]}")
                except Exception as e:
                    print(f"  [!] Error: {e}")

        except websockets.exceptions.ConnectionClosed:
            pass
        finally:
            self.clients.discard(websocket)
            if self.audio_stream:
                try:
                    self.audio_stream.stop()
                    self.audio_stream.close()
                except: pass
                self.audio_stream = None
            print(f"[-] Client disconnected: {remote[0]}:{remote[1]}")

    async def start(self):
        self.server = await websockets.serve(
            self.handler,
            self.host,
            self.port,
            ping_interval=20,
            ping_timeout=10,
            max_size=10 * 1024 * 1024,
        )
        print(f"WebSocket server listening on ws://{self.host}:{self.port}")
        await self.server.wait_closed()

    def run(self):
        asyncio.run(self.start())

    def stop(self):
        if self.server:
            self.server.close()
