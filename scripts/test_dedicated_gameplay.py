#!/usr/bin/env python3
"""Optional real graphical client smoke against a new headless world (requires an X display)."""
from pathlib import Path
import os
import socket
import subprocess
import tempfile
import time

ROOT = Path(__file__).resolve().parents[1]
JDK = Path(os.environ.get("JAVA_HOME", "/usr/lib/jvm/java-11-openjdk"))
subprocess.run([str(ROOT / "scripts/dev.sh"), "build"], check=True)
with tempfile.TemporaryDirectory(prefix="dedicated-graphical-", dir=ROOT / "build") as temp:
    run = Path(temp)
    cp = os.pathsep.join(map(str, (run, ROOT / "build/pokewilds-engine.jar", ROOT / "vendor/runtime-deps.jar")))
    subprocess.run([str(JDK / "bin/javac"), "--release", "8", "-cp", cp, "-d", str(run), str(ROOT / "tests/java/DedicatedClientE2E.java")], check=True)
    with socket.socket() as sock:
        sock.bind(("127.0.0.1", 0)); port = sock.getsockname()[1]
    server_dir = run / "server"; server_dir.mkdir()
    client_dir = run / "client"; client_dir.mkdir()
    (client_dir / "settings.txt").write_text("muteMusic=true\ndrawOverlays=false\n")
    with (run / "server.log").open("w+") as log:
        server = subprocess.Popen([str(JDK / "bin/java"), "-Djava.awt.headless=true", "-cp", cp, "com.pkmngen.game.DedicatedServer", "fresh", str(port)], cwd=server_dir, stdout=log, stderr=subprocess.STDOUT)
        try:
            deadline = time.monotonic() + 20
            while time.monotonic() < deadline:
                log.seek(0)
                output = log.read()
                if "DEDICATED_READY" in output:
                    print(output, flush=True); break
                if server.poll() is not None: raise AssertionError(output)
                time.sleep(.1)
            else: raise TimeoutError("Server startup")
            env = os.environ.copy(); env.pop("WAYLAND_DISPLAY", None); env.setdefault("DISPLAY", ":0")
            result = subprocess.run([str(JDK / "bin/java"), "-Xmx1800m", "-cp", cp, "com.pkmngen.game.DedicatedClientE2E", f"--coop-join=127.0.0.1:{port}", "--coop-name=smoke"], cwd=client_dir, env=env, capture_output=True, text=True, timeout=75)
            print(result.stdout[-8000:]); print(result.stderr[-8000:])
            assert result.returncode == 0 and "DEDICATED_CLIENT PASS" in result.stdout
            assert "Exception" not in result.stdout + result.stderr, result.stderr
            assert "COOP_CONFLICT" not in result.stdout + result.stderr, result.stderr
        finally:
            server.terminate(); server.wait(timeout=15)
