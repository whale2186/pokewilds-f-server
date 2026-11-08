"""Fast adapter input/security checks; graphical acceptance is a separate command."""
from pathlib import Path
import os
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]


class LanAdapterTest(unittest.TestCase):
    def test_options_and_untrusted_bootstrap_boundaries(self):
        jdk = Path(os.environ.get("JAVA_HOME", "/usr/lib/jvm/java-11-openjdk"))
        build = subprocess.run([str(ROOT / "scripts/dev.sh"), "build"], text=True, capture_output=True)
        self.assertEqual(build.returncode, 0, build.stdout + build.stderr)
        classpath = str(ROOT / "build/pokewilds-engine.jar") + os.pathsep + str(ROOT / "vendor/runtime-deps.jar")
        with tempfile.TemporaryDirectory(prefix="lan-adapter-", dir=ROOT / "build") as output:
            names = ("MenuInputTest", "TitleMenuTest", "LanOptionsTest", "LanMetadataTest", "LanIdentityTest", "LanInventoryCheckpointTest", "LanTileCacheTest", "LanSmoothingAndRebaseTest", "LanBattleStateTest")
            result = subprocess.run([str(jdk / "bin/javac"), "--release", "8", "-cp", classpath, "-d", output, *(str(ROOT / "tests/java" / (name + ".java")) for name in names)], capture_output=True, text=True)
            self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
            for name in names:
                with self.subTest(fixture=name):
                    result = subprocess.run([str(jdk / "bin/java"), "-Djava.io.tmpdir=" + output, "-cp", output + os.pathsep + classpath, "com.pkmngen.game." + name, output], capture_output=True, text=True, timeout=15, cwd=output if name in ("MenuInputTest", "TitleMenuTest") else None)
                    self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
                    self.assertIn("PASS", result.stdout)

    def test_invalid_lan_options_fail_before_graphics_initialization(self):
        env = os.environ.copy()
        env.pop("DISPLAY", None)
        jdk = Path(env.get("JAVA_HOME", "/usr/lib/jvm/java-11-openjdk"))
        classpath = str(ROOT / "build/pokewilds-engine.jar") + os.pathsep + str(ROOT / "vendor/runtime-deps.jar")
        result = subprocess.run([str(jdk / "bin/java"), "-cp", classpath, "com.pkmngen.game.desktop.DesktopLauncher", "--coop-host=localhost:0"], env=env, capture_output=True, text=True, timeout=10)
        self.assertEqual(result.returncode, 2, result.stdout + result.stderr)
        self.assertIn("Invalid LAN endpoint", result.stderr)


if __name__ == "__main__":
    unittest.main()
