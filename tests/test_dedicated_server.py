"""Dedicated process startup, client edits, bookmarks, and restart without DISPLAY."""
from pathlib import Path
import json
import os
import shutil
import selectors
import socket
import subprocess
import tempfile
import unittest
import zipfile
import time

ROOT = Path(__file__).resolve().parents[1]


class DedicatedServerTest(unittest.TestCase):
    def test_authoritative_creature_mechanics(self):
        subprocess.run([str(ROOT / "scripts/dev.sh"), "build"], check=True, capture_output=True)
        jdk = Path(os.environ.get("JAVA_HOME", "/usr/lib/jvm/java-11-openjdk"))
        with tempfile.TemporaryDirectory(prefix="dedicated-ai-", dir=ROOT / "build") as tmp:
            cp = os.pathsep.join(map(str, (tmp, ROOT / "build/pokewilds-engine.jar", ROOT / "vendor/runtime-deps.jar")))
            subprocess.run([str(jdk / "bin/javac"), "--release", "8", "-cp", cp, "-d", tmp, str(ROOT / "tests/java/DedicatedMechanicsTest.java")], check=True, capture_output=True)
            result = subprocess.run([str(jdk / "bin/java"), "-Djava.awt.headless=true", "-cp", cp, "com.pkmngen.game.DedicatedMechanicsTest"], capture_output=True, text=True, timeout=180)
            self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
            self.assertIn("PASS", result.stdout)

    def test_headless_join_edit_and_restart(self):
        self.run_world("fixture")

    def test_fresh_world_join_and_restart(self):
        self.run_world("fresh")

    def test_existing_asd_world_stays_running(self):
        if not (ROOT / "runtime/asd.sav/game.json.zip").exists():
            self.skipTest("Local asd save is unavailable")
        self.run_world("asd")

    def run_world(self, source):
        subprocess.run([str(ROOT / "scripts/dev.sh"), "build"], check=True, capture_output=True)
        jdk = Path(os.environ.get("JAVA_HOME", "/usr/lib/jvm/java-11-openjdk"))
        with tempfile.TemporaryDirectory(prefix="dedicated-", dir=ROOT / "build") as tmp:
            tmp = Path(tmp)
            cp = os.pathsep.join(map(str, (tmp, ROOT / "build/pokewilds-engine.jar", ROOT / "vendor/runtime-deps.jar")))
            subprocess.run([str(jdk / "bin/javac"), "--release", "8", "-cp", cp, "-d", str(tmp), str(ROOT / "tests/java/DedicatedServerTest.java")], check=True, capture_output=True)
            world = tmp / "test.sav"
            if source == "asd":
                shutil.copytree(ROOT / "runtime/asd.sav", world)
            elif source == "fixture":
                world.mkdir()
                def write(name, data):
                    with zipfile.ZipFile(world / (name + ".json.zip"), "w") as archive:
                        archive.writestr("data.json", json.dumps(data))
                write("game", {"currMapId": "0,0", "timeOfDay": "day", "dayTimer": 18000})
                write("map0,0", {"mapTiles": {"tiles": [{"pos": 0, "tileName": "green1", "tileNameUpper": "", "interiorIndex": 0}, {"pos": 1048592, "tileName": "green1", "tileNameUpper": "", "interiorIndex": 0}], "edges": [0], "routes": {}, "interiorTiles": [], "interiorTilesIndex": 0}, "overworldPokemon": {}})
            with socket.socket() as sock:
                sock.bind(("127.0.0.1", 0))
                port = str(sock.getsockname()[1])
            if source == "fresh":
                # A failed bind must leave the new world safe to create on retry.
                with socket.socket() as occupied:
                    occupied.bind(("127.0.0.1", int(port)))
                    occupied.listen()
                    failed = subprocess.run([str(jdk / "bin/java"), "-cp", cp, "com.pkmngen.game.DedicatedServer", "test", port], cwd=tmp, capture_output=True, text=True, timeout=10)
                    self.assertEqual(failed.returncode, 1)
                    self.assertFalse((world / "dedicated-state.json.zip").exists())
            env = os.environ.copy()
            env.pop("DISPLAY", None)
            env.pop("WAYLAND_DISPLAY", None)
            first_uid = None
            for mode in (("probe", "probe") if source == "asd" else ("edit", "verify")):
                proc = subprocess.Popen([str(jdk / "bin/java"), "-Djava.awt.headless=true", "-cp", cp, "com.pkmngen.game.desktop.DesktopLauncher", "--server", "test", port], cwd=tmp, env=env, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
                try:
                    startup = []
                    deadline = time.monotonic() + 180
                    while time.monotonic() < deadline:
                        ready = proc.stdout.readline()
                        startup.append(ready)
                        if "DEDICATED_READY" in ready or proc.poll() is not None:
                            break
                    self.assertIn("DEDICATED_READY", ready, "".join(startup[-40:]))
                    self.assertIn("DEDICATED_READY", ready)
                    result = subprocess.run([str(jdk / "bin/java"), "-cp", cp, "com.pkmngen.game.DedicatedServerTest", port, mode], cwd=tmp, env=env, capture_output=True, text=True, timeout=90)
                    self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
                    self.assertIn("PASS", result.stdout)
                    self.assertIsNone(proc.poll(), "Server exited after client disconnect")
                    with socket.create_connection(("127.0.0.1", int(port)), timeout=2):
                        pass
                finally:
                    if proc.poll() is None:
                        proc.stdin.write("help\nplayers\ntime night\nsave\nstop\n")
                        proc.stdin.flush()
                    output = proc.communicate(timeout=60)[0]
                self.assertIn("Commands: help", output)
                self.assertIn("players online", output)
                self.assertIn("Time set to night", output)
                self.assertIn("World saved", output)
                self.assertIn("Stopping server", output)
                self.assertEqual(proc.returncode, 0, output)
                self.assertNotIn("World save failed", output)
                self.assertNotIn("Exception", output)
                self.assertTrue((world / "dedicated-state.json.zip").exists())
                with zipfile.ZipFile(world / "dedicated-state.json.zip") as archive:
                    saved = json.loads(archive.read("data.json"))
                if first_uid is None:
                    first_uid = saved["world"]["worldUid"]
                self.assertEqual(saved["world"]["worldUid"], first_uid)
                self.assertEqual(json.loads(saved["resources"]["clock"])["timeOfDay"], "night")
                if source == "fresh":
                    terrain = {key: value for key, value in saved["resources"].items() if key.startswith("tile:")}
                    # Original GenIsland1 produces irregular islands, not a fixed synthetic square.
                    self.assertGreater(sum(key.startswith("tile:-1:") for key in terrain), 20000)
                    self.assertGreater(sum(not key.startswith("tile:-1:") for key in terrain), 200)
                    tiles = [json.loads(value) for value in terrain.values()]
                    route_names = {route["name"] for route in saved["world"]["routes"].values()}
                    self.assertIn("forest1", route_names)
                    self.assertIn("desert1", route_names)
                    self.assertGreater(len(route_names), 5)
                    creatures = [json.loads(value) for key, value in saved["resources"].items() if key.startswith("pokemon:")]
                    self.assertGreater(len(creatures), 50)
                    self.assertTrue(all(creature["data"].get("nickname") and creature["data"]["hp"] > 0 for creature in creatures))
                    self.assertIn("worldUid", saved["world"])

    def test_incomplete_world_reports_error_without_replacement(self):
        jdk = Path(os.environ.get("JAVA_HOME", "/usr/lib/jvm/java-11-openjdk"))
        cp = os.pathsep.join(map(str, (ROOT / "build/pokewilds-engine.jar", ROOT / "vendor/runtime-deps.jar")))
        with tempfile.TemporaryDirectory(prefix="dedicated-invalid-", dir=ROOT / "build") as tmp:
            world = Path(tmp) / "broken.sav"
            world.mkdir()
            marker = world / "map0,0.json.zip"
            marker.write_bytes(b"incomplete")
            result = subprocess.run([str(jdk / "bin/java"), "-cp", cp, "com.pkmngen.game.DedicatedServer", "broken"], cwd=tmp, capture_output=True, text=True, timeout=10)
            self.assertEqual(result.returncode, 1)
            self.assertIn("Incomplete world", result.stderr)
            self.assertEqual(marker.read_bytes(), b"incomplete")
            self.assertFalse((world / "dedicated-state.json.zip").exists())
            with zipfile.ZipFile(world / "game.json.zip", "w") as archive:
                archive.writestr("wrong.json", "{}")
            result = subprocess.run([str(jdk / "bin/java"), "-cp", cp, "com.pkmngen.game.DedicatedServer", "broken"], cwd=tmp, capture_output=True, text=True, timeout=10)
            self.assertEqual(result.returncode, 1)
            self.assertIn("Missing data.json", result.stderr)
            self.assertFalse((world / "dedicated-state.json.zip").exists())
