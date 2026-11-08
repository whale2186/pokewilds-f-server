#!/usr/bin/env python3
"""Run real three-JVM graphical LAN co-op acceptance, retaining every run."""
from pathlib import Path
import json
import os
import re
import signal
import shutil
import socket
import subprocess
import tempfile
import time

ROOT = Path(__file__).resolve().parents[1]
JAVA_HOME = Path(os.environ.get("JAVA_HOME", "/usr/lib/jvm/java-11-openjdk"))
JAR = ROOT / "build/pokewilds-engine.jar"
DEPS = ROOT / "vendor/runtime-deps.jar"


def main():
    if os.environ.get("PW_E2E_USE_BUILT") != "1":
        subprocess.run([str(ROOT / "scripts/dev.sh"), "build"], check=True)
    classes = ROOT / "build/lan-e2e/classes"
    classes.mkdir(parents=True, exist_ok=True)
    subprocess.run([str(JAVA_HOME / "bin/javac"), "--release", "8", "-cp", f"{JAR}:{DEPS}", "-d", str(classes), str(ROOT / "tests/java/LanGameE2E.java")], check=True)
    run = Path(tempfile.mkdtemp(prefix="lan-e2e-", dir=ROOT / "build"))
    print(f"Graphical evidence: {run}", flush=True)
    with socket.socket() as probe:
        probe.bind(("127.0.0.1", 0))
        port = probe.getsockname()[1]
    processes = {}
    logs = {}
    started = time.monotonic()
    report = {"scope": "real graphical host + guest + late join JVMs, loopback", "run": str(run), "port": port, "success": False}

    def launch(role, options):
        directory = run / role
        directory.mkdir()
        source_world = os.environ.get("PW_E2E_WORLD")
        if role == "host" and source_world:
            shutil.copytree(Path(source_world).resolve(), directory / "lan-test.sav")
        for name in ("cache", "config", "data", "tmp"):
            (directory / name).mkdir()
        (directory / "settings.txt").write_text("muteMusic=true\ndrawOverlays=false\n")
        env = os.environ.copy()
        env.pop("WAYLAND_DISPLAY", None)
        env.update(DISPLAY=env.get("DISPLAY", ":0"), XDG_CACHE_HOME=str(directory / "cache"), XDG_CONFIG_HOME=str(directory / "config"), XDG_DATA_HOME=str(directory / "data"))
        log = open(directory / "console.log", "w")
        logs[role] = log
        command = [str(JAVA_HOME / "bin/java"), "-Xmx1800m", f"-Djava.io.tmpdir={directory / 'tmp'}", f"-De2e.role={role}", "-De2e.width=" + os.environ.get("PW_E2E_WIDTH","480"), "-De2e.height=" + os.environ.get("PW_E2E_HEIGHT","432"), "-cp", f"{classes}:{JAR}:{DEPS}", "com.pkmngen.game.LanGameE2E", *options]
        processes[role] = subprocess.Popen(command, cwd=directory, env=env, stdout=log, stderr=subprocess.STDOUT, start_new_session=True)

    def wait_marker(role, marker, timeout=90):
        deadline = time.monotonic() + timeout
        while time.monotonic() < deadline:
            text = (run / role / "console.log").read_text(errors="replace")
            if marker in text:
                print(f"Verified {role}: {marker}", flush=True)
                return
            if processes[role].poll() is not None:
                raise AssertionError(f"{role} exited {processes[role].returncode} before {marker}\n{text[-10000:]}")
            if "AssertionError" in text or "COOP_DISCONNECTED" in text or "snapshot failed" in text:
                raise AssertionError(f"{role} failed before {marker}\n{text[-10000:]}")
            time.sleep(0.1)
        raise TimeoutError(f"{role}: no {marker} after {timeout}s\n" + (run / role / "console.log").read_text(errors="replace")[-12000:])

    try:
        world_option = "--coop-world=lan-test" if os.environ.get("PW_E2E_WORLD") else "--coop-new=lan-test"
        launch("host", [f"--coop-host=127.0.0.1:{port}", "--coop-name=host", world_option, "--coop-size=30000"])
        wait_marker("host", "WORLD_READY", 240)
        launch("guest", [f"--coop-join=127.0.0.1:{port}", "--coop-name=guest"])
        wait_marker("guest", "WORLD_READY")
        wait_marker("guest", "PEERS_VISIBLE=")
        wait_marker("guest", "HUD_INPUT_OK")
        wait_marker("guest", "CHAT_RECEIVED")
        wait_marker("host", "CHAT_RECEIVED")
        wait_marker("guest", "HOST_EDIT_OBSERVED")
        wait_marker("guest", "LOCAL_MOVE_OK")
        movement = re.search(r"LOCAL_MOVE_OK pos=(\([^\r\n]+\))", (run / "guest/console.log").read_text())
        if movement is None: raise AssertionError("Guest position missing from movement evidence")
        wait_marker("host", "REMOTE_POSE pos=" + movement.group(1))
        wait_marker("host", "GUEST_EDIT_OBSERVED")
        wait_marker("host", "HOST_SAVE_OK")
        launch("late", [f"--coop-join=127.0.0.1:{port}", "--coop-name=late"])
        wait_marker("late", "LATE_JOIN_STATE_OK")
        wait_marker("host", "LATE_JOIN_OBSERVED")
        wait_marker("guest", "PASS_GUEST")
        wait_marker("late", "PASS_LATE")
        wait_marker("host", "DISCONNECTS_CLEANED")
        for role, process in processes.items():
            code = process.wait(timeout=20)
            if code != 0:
                raise AssertionError(f"{role} exit={code}")
        report["success"] = True
        report["assertions"] = ["copied real saved island" if os.environ.get("PW_E2E_WORLD") else "host-generated real island", "guest state equals authority", "remote avatar rendering", "bidirectional authenticated chat", "HUD input suppression and restoration", "HUD player count", "host-to-guest tile edit", "guest-to-host tile edit", "production walking input", "late-join edited state", "host JSON/ZIP save", "guest disconnect removal", "clean JVM exits"]
        print("PASS real graphical LAN co-op acceptance", flush=True)
    finally:
        for role, process in processes.items():
            if process.poll() is None:
                os.killpg(process.pid, signal.SIGTERM)
                try:
                    process.wait(timeout=10)
                except subprocess.TimeoutExpired:
                    os.killpg(process.pid, signal.SIGKILL)
                    process.wait(timeout=5)
            logs[role].close()
        report["elapsed_seconds"] = round(time.monotonic() - started, 3)
        report["exit_codes"] = {role: process.returncode for role, process in processes.items()}
        (run / "report.json").write_text(json.dumps(report, indent=2) + "\n")


if __name__ == "__main__":
    main()
