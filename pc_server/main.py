import platform
import sys
import keyboard as kb

from server import WebSocketServer

HOST = "0.0.0.0"
PORT = 8765

def main():
    local_ip = kb.get_local_ip()
    print(f"PhoneMic Keyboard Server")
    print(f"PC IP: {local_ip}")
    print(f"Port: {PORT}")
    print(f"Connect from phone: ws://{local_ip}:{PORT}")
    print("-" * 50)

    server = WebSocketServer(HOST, PORT)
    try:
        server.run()
    except KeyboardInterrupt:
        print("\nShutting down...")
        server.stop()
        sys.exit(0)

if __name__ == "__main__":
    main()
