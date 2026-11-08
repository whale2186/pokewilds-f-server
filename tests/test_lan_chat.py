"""Chat uses real socket identities without touching persistent world resources."""
import os
from pathlib import Path
import subprocess
import tempfile
import unittest
ROOT = Path(__file__).resolve().parents[1]
class LanChatTest(unittest.TestCase):
    def test_chat_routing_and_bounds(self):
        jdk = Path(os.environ.get('JAVA_HOME', '/usr/lib/jvm/java-11-openjdk'))
        subprocess.run([str(ROOT/'scripts/dev.sh'), 'build'], check=True, capture_output=True)
        with tempfile.TemporaryDirectory(dir=ROOT/'build', prefix='chat-test-') as temp:
            cp = os.pathsep.join([temp, str(ROOT/'build/pokewilds-engine.jar'), str(ROOT/'vendor/runtime-deps.jar')])
            result = subprocess.run([str(jdk/'bin/javac'), '--release', '8', '-cp', cp, '-d', temp, str(ROOT/'tests/java/LanChatTest.java')], capture_output=True, text=True)
            self.assertEqual(result.returncode, 0, result.stdout+result.stderr)
            result = subprocess.run([str(jdk/'bin/java'), '-cp', cp, 'com.pkmngen.game.LanChatTest'], capture_output=True, text=True, timeout=20)
            self.assertEqual(result.returncode, 0, result.stdout+result.stderr)
            self.assertIn('PASS', result.stdout)
