"""Real loopback protocol regressions; not a replacement for graphical E2E."""
from pathlib import Path
import os
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]


class LanTransportTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.jdk = Path(os.environ.get("JAVA_HOME", "/usr/lib/jvm/java-11-openjdk"))
        build = subprocess.run([str(ROOT / "scripts/dev.sh"), "build"], capture_output=True, text=True)
        if build.returncode:
            raise AssertionError(build.stdout + build.stderr)
        cls.output = tempfile.TemporaryDirectory(prefix="lan-protocol-", dir=ROOT / "build")
        cls.addClassCleanup(cls.output.cleanup)
        cls.classpath = os.pathsep.join((cls.output.name, str(ROOT / "build/pokewilds-engine.jar"), str(ROOT / "vendor/runtime-deps.jar")))
        sources = sorted((ROOT / "tests/java/multiplayer").glob("*.java"))
        result = subprocess.run([str(cls.jdk / "bin/javac"), "--release", "8", "-cp", cls.classpath, "-d", cls.output.name, *map(str, sources)], capture_output=True, text=True)
        if result.returncode:
            raise AssertionError(result.stdout + result.stderr)

    def run_fixture(self, name):
        result = subprocess.run([str(self.jdk / "bin/java"), "-Xmx768m", "-Djava.io.tmpdir=" + self.output.name, "-cp", self.classpath, "multiplayer." + name], capture_output=True, text=True, timeout=120)
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        self.assertIn("PASS", result.stdout)

    def test_interaction_routing_and_durable_decisions(self): self.run_fixture("LanInteractionTest")

    def test_snapshot_pose_cas_and_capacity(self): self.run_fixture("LanSessionTest")
    def test_untrusted_packets_and_identity(self): self.run_fixture("LanSecurityTest")
    def test_disconnect_and_worker_lifecycle(self): self.run_fixture("LanLifecycleTest")
    def test_roster_overflow_and_invalid_leave_regressions(self): self.run_fixture("LanDisconnectRegressionTest")
    def test_atomic_transactions_and_late_join(self): self.run_fixture("LanTransactionTest")
    def test_transaction_ack_validation(self): self.run_fixture("LanTransactionSecurityTest")
    def test_transaction_queue_memory_and_revision_bounds(self): self.run_fixture("LanTransactionBoundsTest")


if __name__ == "__main__": unittest.main()
