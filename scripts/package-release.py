#!/usr/bin/env python3
"""Build portable client/server ZIPs using explicit inputs, never a workspace snapshot."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[1]

def sha(path):
    with path.open('rb') as stream:
        digest = hashlib.sha256()
        for block in iter(lambda: stream.read(1024*1024), b''): digest.update(block)
        return digest.hexdigest()

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--version', required=True)
    parser.add_argument('--kind', choices=('client', 'server', 'both'), default='both')
    parser.add_argument('--skip-build', action='store_true')
    args = parser.parse_args()
    if not re.fullmatch(r'v\d+\.\d+\.\d+(?:-[A-Za-z0-9.-]+)?', args.version):
        parser.error('Expected version such as v0.1.0')
    if not args.skip_build:
        subprocess.run(['bash', str(ROOT/'scripts/dev.sh'), 'build'], check=True)
    deps = ROOT/'vendor/runtime-deps.jar'
    if not deps.is_file(): deps = ROOT/'lib/runtime-deps.jar'
    engine = ROOT/'build/pokewilds-engine.jar'
    if not deps.is_file() or not engine.is_file():
        raise SystemExit('Missing engine/runtime JAR; obtain matching runtime dependencies and build first')
    revision = subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=ROOT, text=True).strip()
    output = ROOT/'build/releases'/args.version
    output.mkdir(parents=True, exist_ok=True)
    kinds = ('client', 'server') if args.kind == 'both' else (args.kind,)
    for kind in kinds:
        name = 'pokewilds-f' if kind == 'client' else 'pokewilds-f-server'
        package = output/name
        if package.exists():
            raise SystemExit(f'{package} already exists; select a fresh output/version or remove the previous staging directory explicitly')
        (package/'lib').mkdir(parents=True)
        shutil.copy2(engine, package/'lib/pokewilds-engine.jar')
        shutil.copy2(deps, package/'lib/runtime-deps.jar')
        for file in (ROOT/'distribution'/kind).iterdir():
            if file.is_file(): shutil.copy2(file, package/file.name)
        shutil.copy2(ROOT/('README.md' if kind == 'client' else 'docs/SERVER.md'), package/'README.md')
        for file in ('CREDITS.md','THIRD-PARTY-NOTICES.md'):
            shutil.copy2(ROOT/file, package/file)
        (package/'docs').mkdir()
        shutil.copy2(ROOT/'docs/MULTIPLAYER.md',package/'docs/MULTIPLAYER.md')
        if kind == 'client':
            (package/'config').mkdir()
            settings = ROOT/'vendor/pokewilds-v0.8.11-otherplatforms/settings.txt'
            if settings.exists(): shutil.copy2(settings, package/'config/settings.txt')
            else:
                (package/'config/settings.txt').write_text('keyboard-A=Z\nkeyboard-B=X\nkeyboard-Left=Left\nkeyboard-Right=Right\nkeyboard-Up=Up\nkeyboard-Down=Down\nkeyboard-Start=Enter\nkeyboard-L=C\nkeyboard-R=V\nmuteMusic=false\nzoom=1.0\n',encoding='utf-8')
        manifest = {'project':name,'version':args.version,'base_version':'PokeWilds v0.8.11',
                    'source_commit':revision,'upstream':'https://github.com/SheerSt/pokewilds',
                    'java':'11','engine_sha256':sha(engine),'runtime_sha256':sha(deps)}
        (package/'release.json').write_text(json.dumps(manifest,indent=2)+'\n',encoding='utf-8')
        archive = output/f'{name}-{args.version}.zip'
        with zipfile.ZipFile(archive,'w',compression=zipfile.ZIP_DEFLATED,compresslevel=6) as bundle:
            for file in sorted(package.rglob('*')):
                if file.is_file(): bundle.write(file,file.relative_to(output))
        checksum = archive.with_suffix('.zip.sha256')
        checksum.write_text(sha(archive)+'  '+archive.name+'\n',encoding='ascii')
        print(f'{archive} ({archive.stat().st_size:,} bytes)',flush=True)

if __name__ == '__main__': main()
