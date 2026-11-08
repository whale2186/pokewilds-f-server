"""End-to-end checks for the local reconstructed development workspace."""
from pathlib import Path
import os
import signal
import socket
import subprocess
import tempfile
import time
import unittest
import zipfile

ROOT = Path(__file__).resolve().parents[1]


class WorkspaceTest(unittest.TestCase):
    def test_build_recompiles_engine_without_original_engine_bytecode(self):
        script = ROOT / "scripts/dev.sh"
        self.assertTrue(script.is_file(), "The reproducible build command is missing")
        result = subprocess.run(["bash", str(script), "build"], text=True, capture_output=True)
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        with zipfile.ZipFile(ROOT / "vendor/runtime-deps.jar") as deps:
            self.assertFalse(any(name.startswith(("com/pkmngen/game/", "com/pkmngen/pokemon/", "com/pkmngen/updater/")) and name.endswith(".class") for name in deps.namelist()))
        with zipfile.ZipFile(ROOT / "build/pokewilds-engine.jar") as engine:
            self.assertIn("com/pkmngen/game/desktop/DesktopLauncher.class", engine.namelist())
            self.assertIn("com/pkmngen/game/Network.class", engine.namelist())
            self.assertIn("com/pkmngen/game/Player.class", engine.namelist())
    def test_debugger_accepts_jdwp_handshake(self):
        process = subprocess.Popen(["bash", str(ROOT / "scripts/dev.sh"), "debug", "dev"], stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, start_new_session=True)
        try:
            deadline = time.monotonic() + 20
            while True:
                try:
                    connection = socket.create_connection(("127.0.0.1", 5005), timeout=1)
                    break
                except OSError:
                    if process.poll() is not None or time.monotonic() >= deadline:
                        self.fail("Debugger did not become available on loopback port 5005")
                    time.sleep(0.1)
            with connection:
                connection.sendall(b"JDWP-Handshake")
                reply = b""
                while len(reply) < 14:
                    part = connection.recv(14 - len(reply))
                    self.assertTrue(part, "Debugger closed before handshake completed")
                    reply += part
                self.assertEqual(reply, b"JDWP-Handshake")
        finally:
            if process.poll() is None:
                os.killpg(process.pid, signal.SIGTERM)
            try:
                output, _ = process.communicate(timeout=10)
            except subprocess.TimeoutExpired:
                os.killpg(process.pid, signal.SIGKILL)
                output, _ = process.communicate(timeout=5)
        self.assertIn("Listening for transport dt_socket at address: 5005", output)

    def test_headless_behavior_matches_original_release(self):
        jdk = Path(os.environ.get("JAVA_HOME", "/usr/lib/jvm/java-11-openjdk"))
        original = ROOT / "vendor/pokewilds-v0.8.11-otherplatforms/pokewilds.jar"
        rebuilt = str(ROOT / "build/pokewilds-engine.jar") + os.pathsep + str(ROOT / "vendor/runtime-deps.jar")
        with tempfile.TemporaryDirectory(prefix="engine-smoke-", dir=ROOT / "build") as directory:
            compilation = subprocess.run([str(jdk / "bin/javac"), "--release", "8", "-cp", str(original), "-d", directory, str(ROOT / "tests/java/EngineSmoke.java")], text=True, capture_output=True)
            self.assertEqual(compilation.returncode, 0, compilation.stderr)
            outputs = []
            for classpath in (str(original), rebuilt):
                result = subprocess.run([str(jdk / "bin/java"), "-cp", directory + os.pathsep + classpath, "EngineSmoke", directory], text=True, capture_output=True, timeout=30, cwd=directory)
                self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
                self.assertIn("PASS 56 headless checks", result.stdout)
                outputs.append(result.stdout)
            self.assertEqual(outputs[0], outputs[1], "Original and reconstructed headless behavior differs")


if __name__ == "__main__":
    unittest.main()
