import logging
import sys

from keyboard import get_local_ip
from server import WebSocketServer

HOST = "0.0.0.0"
PORT = 8765

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%H:%M:%S",
)
log = logging.getLogger("main")


def main():
    local_ip = get_local_ip()
    log.info("PhoneMic Keyboard Server")
    log.info("PC IP: %s", local_ip)
    log.info("Port: %d", PORT)
    log.info("Connect from phone: ws://%s:%d", local_ip, PORT)

    server = WebSocketServer(HOST, PORT)
    try:
        server.run()
    except KeyboardInterrupt:
        log.info("Shutting down...")
        server.stop()
        sys.exit(0)


if __name__ == "__main__":
    main()
