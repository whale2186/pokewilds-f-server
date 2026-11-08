#!/usr/bin/env python3
"""Graphical contested gameplay; source world is copied, never modified."""
from pathlib import Path
import json
import os
import shutil
import signal
import socket
import subprocess
import sys
import tempfile
import time
import zipfile

ROOT = Path(__file__).resolve().parents[1]
JDK = Path(os.environ.get("JAVA_HOME", "/usr/lib/jvm/java-11-openjdk"))


def main():
    if len(sys.argv) != 2:
        raise SystemExit("Usage: test_gameplay_conflicts.py /absolute/path/to/existing-world.sav")
    source = Path(sys.argv[1]).resolve()
    if not (source / "game.json.zip").is_file():
        raise SystemExit("Source must be an existing game save directory; it will be copied")
    subprocess.run([str(ROOT / "scripts/dev.sh"), "build"], check=True)
    run = Path(tempfile.mkdtemp(prefix="lan-safety-", dir=ROOT / "build"))
    print(f"Gameplay safety evidence: {run}", flush=True)
    classes = run / "classes"; classes.mkdir()
    cp = f"{ROOT / 'build/pokewilds-engine.jar'}:{ROOT / 'vendor/runtime-deps.jar'}"
    subprocess.run([str(JDK / "bin/javac"), "--release", "8", "-cp", cp, "-d", str(classes), str(ROOT / "tests/java/LanSafetyE2E.java")], check=True)
    with socket.socket() as probe:
        probe.bind(("127.0.0.1", 0)); port = probe.getsockname()[1]
    processes, logs = {}, {}
    report = {"run": str(run), "scope": "three real graphical JVMs; contested Pokemon/item transfer and denied build", "success": False}
    started = time.monotonic()

    def launch(role):
        directory = run / role; directory.mkdir()
        if role == "host": shutil.copytree(source, directory / "lan-test.sav")
        if role == "reload":
            shutil.copytree(run / "host/lan-test.sav", directory / "lan-test.sav")
            shutil.copy2(run / "host/coop-profile-host.txt", directory / "coop-profile-host.txt")
        if role.startswith("restore-"):
            previous = run / role.removeprefix("restore-")
            for path in previous.glob("coop-*"):
                if path.is_file(): shutil.copy2(path, directory / path.name)
            shutil.copy2(previous / "result.json", directory / "result.json")
        (directory / "tmp").mkdir()
        (directory / "settings.txt").write_text("muteMusic=true\ndrawOverlays=false\n")
        env = os.environ.copy(); env.pop("WAYLAND_DISPLAY", None)
        env.update(DISPLAY=env.get("DISPLAY", ":0"), XDG_CACHE_HOME=str(directory / "cache"), XDG_CONFIG_HOME=str(directory / "config"), XDG_DATA_HOME=str(directory / "data"))
        host_mode = role in ("host", "reload")
        args = [f"--coop-host=127.0.0.1:{port}", "--coop-world=lan-test"] if host_mode else [f"--coop-join=127.0.0.1:{port}"]
        name = "host" if host_mode else role.removeprefix("restore-")
        logs[role] = open(directory / "console.log", "w")
        command = [str(JDK / "bin/java"), "-Xmx1800m", f"-Djava.io.tmpdir={directory / 'tmp'}", f"-De2e.role={role}", f"-De2e.barrier={run}", "-cp", f"{classes}:{cp}", "com.pkmngen.game.LanSafetyE2E", *args, f"--coop-name={name}"]
        processes[role] = subprocess.Popen(command, cwd=directory, env=env, stdout=logs[role], stderr=subprocess.STDOUT, start_new_session=True)

    def wait_marker(role, marker, timeout=90):
        deadline = time.monotonic() + timeout
        while time.monotonic() < deadline:
            text = (run / role / "console.log").read_text(errors="replace")
            if marker in text:
                print(f"Verified {role}: {marker}", flush=True); return
            for other, process in processes.items():
                if process.poll() not in (None, 0):
                    detail = (run / other / "console.log").read_text(errors="replace")
                    raise AssertionError(f"{other} exit={process.returncode}\n{detail[-7000:]}")
            if "AssertionError" in text: raise AssertionError(text[-7000:])
            time.sleep(.1)
        raise TimeoutError(f"{role} did not reach {marker}\n{text[-7000:]}")

    try:
        launch("host"); wait_marker("host", "OFFER_READY")
        launch("a"); launch("b")
        wait_marker("a", "RACE_READY"); wait_marker("b", "RACE_READY")
        (run / "go").write_text("simultaneous take barrier\n")
        for role in ("a", "b"): wait_marker(role, "REJECTED_CREATE_ROLLED_BACK")
        results = {role: json.loads((run / role / "result.json").read_text()) for role in ("a", "b")}
        assert sum(value["won"] for value in results.values()) == 1, results
        assert sum(value["party"] - value["initial_party"] for value in results.values()) == 1, results
        assert sum(value["stones"] - value["initial_stones"] for value in results.values()) == 2, results
        assert sum(value["names"].count("LANRACE") for value in results.values()) == 1, results
        report["results"] = results
        wait_marker("host", "HOST_COMMITTED_SAVE_OK")
        with zipfile.ZipFile(run / "host/lan-test.sav/map0,0.json.zip") as archive:
            saved = json.loads(archive.read("data.json"))
        assert not any(value.get("nickname") == "LANRACE" for value in saved["overworldPokemon"].values()), "Taken Pokemon persisted on host"
        (run / "finish").write_text("verified; close normally\n")
        for role in ("a", "b"): wait_marker(role, "SAVE_FAILURE_FAILS_CLOSED")
        wait_marker("host", "HOST_CLEAN_EXIT")
        for role, process in processes.items():
            assert process.wait(timeout=20) == 0, role
        with socket.socket() as probe:
            probe.bind(("127.0.0.1", 0)); port = probe.getsockname()[1]
        launch("reload"); wait_marker("reload", "HOST_WORLD_RELOAD_OK")
        launch("restore-a"); launch("restore-b")
        for role in ("restore-a", "restore-b"): wait_marker(role, "PRIVATE_RESTORE_OK")
        wait_marker("reload", "HOST_RELOAD_CLEAN")
        for role, process in processes.items(): assert process.wait(timeout=20) == 0, role
        report["success"] = True
        print("PASS graphical contested pickup/harvest, stale-action rejection, absent-create rollback, save failure and full host/guest restart", flush=True)
    except BaseException as error:
        report["error"] = str(error); raise
    finally:
        for role, process in processes.items():
            if process.poll() is None:
                os.killpg(process.pid, signal.SIGTERM)
                try: process.wait(timeout=10)
                except subprocess.TimeoutExpired:
                    os.killpg(process.pid, signal.SIGKILL); process.wait(timeout=5)
            logs[role].close()
        report["exit_codes"] = {role: p.returncode for role, p in processes.items()}
        report["elapsed_seconds"] = round(time.monotonic() - started, 3)
        (run / "report.json").write_text(json.dumps(report, indent=2) + "\n")


if __name__ == "__main__": main()
