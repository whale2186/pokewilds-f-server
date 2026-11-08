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
import tarfile
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
    parser.add_argument('--platform', choices=('portable','windows-x64','linux-x64','all'), default='portable')
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
    platforms=('windows-x64','linux-x64') if args.platform=='all' else (args.platform,)
    runtimes=json.loads((ROOT/'distribution/java-runtimes.json').read_text()) if args.platform!='portable' else {}
    for kind,platform in ((kind,platform) for kind in kinds for platform in platforms):
        name = 'pokewilds-f' if kind == 'client' else 'pokewilds-f-server'
        folder=name if platform=='portable' else name+'-'+platform
        package = output/folder
        if package.exists():
            raise SystemExit(f'{package} already exists; select a fresh output/version or remove the previous staging directory explicitly')
        (package/'lib').mkdir(parents=True)
        shutil.copy2(engine, package/'lib/pokewilds-engine.jar')
        shutil.copy2(deps, package/'lib/runtime-deps.jar')
        for file in (ROOT/'distribution'/kind).iterdir():
            if file.is_file() and (platform=='portable' or file.suffix==('.bat' if platform=='windows-x64' else '.sh')):
                shutil.copy2(file, package/file.name)
        if platform!='portable':
            runtime=ROOT/'downloads/platform-jre/extracted'/platform/'jre'
            if not runtime.exists(): raise SystemExit('Run python3 scripts/fetch-java-runtimes.py first')
            shutil.copytree(runtime,package/'jre',symlinks=True)
            if platform=='windows-x64':
                binary='pokewilds-f.exe' if kind=='client' else 'pokewilds-f-server.exe'
                compile_command=['x86_64-w64-mingw32-gcc','-O2','-Wall','-Wextra','-Werror','-municode','-static']
                compile_command+=['-mwindows'] if kind=='client' else ['-DSERVER']
                compile_command += [str(ROOT/'distribution/windows/launcher.c'),'-lshell32','-o',str(package/binary)]
                subprocess.run(compile_command,check=True)
            launch=('pokewilds-f.exe' if kind=='client' else 'pokewilds-f-server.exe') if platform=='windows-x64' else 'start-'+kind+'.sh'
            (package/'START-HERE.txt').write_text('PokeWilds-F '+args.version+' ('+platform+')\n\nExtract the entire archive and run '+launch+'.\nJava 11 is bundled under jre/. Keep jre/, lib/ and the launcher together.\nSettings and saves go in runtime/; preserve it when upgrading.\nSee README.md and CREDITS.md.\n',encoding='utf-8')
        shutil.copy2(ROOT/('README.md' if kind == 'client' else 'docs/SERVER.md'), package/'README.md')
        for file in ('CREDITS.md','THIRD-PARTY-NOTICES.md'):
            shutil.copy2(ROOT/file, package/file)
        (package/'docs').mkdir()
        if (ROOT/'docs/screenshots').is_dir(): shutil.copytree(ROOT/'docs/screenshots',package/'docs/screenshots')
        shutil.copy2(ROOT/'docs/MULTIPLAYER.md',package/'docs/MULTIPLAYER.md')
        if kind == 'client':
            (package/'config').mkdir()
            settings = ROOT/'vendor/pokewilds-v0.8.11-otherplatforms/settings.txt'
            if settings.exists(): shutil.copy2(settings, package/'config/settings.txt')
            else:
                (package/'config/settings.txt').write_text('keyboard-A=Z\nkeyboard-B=X\nkeyboard-Left=Left\nkeyboard-Right=Right\nkeyboard-Up=Up\nkeyboard-Down=Down\nkeyboard-Start=Enter\nkeyboard-L=C\nkeyboard-R=V\nmuteMusic=false\nzoom=1.0\n',encoding='utf-8')
        manifest = {'project':name,'version':args.version,'base_version':'PokeWilds v0.8.11',
                    'source_commit':revision,'upstream':'https://github.com/SheerSt/pokewilds',
                    'java':'11','platform':platform,'bundled_java':runtimes.get(platform),'engine_sha256':sha(engine),'runtime_sha256':sha(deps)}
        (package/'release.json').write_text(json.dumps(manifest,indent=2)+'\n',encoding='utf-8')
        suffix='.tar.gz' if platform=='linux-x64' else '.zip'
        archive=output/(name+'-'+args.version+('' if platform=='portable' else '-'+platform)+suffix)
        if suffix=='.tar.gz':
            with tarfile.open(archive,'w:gz',compresslevel=6) as bundle: bundle.add(package,arcname=folder)
        else:
            with zipfile.ZipFile(archive,'w',compression=zipfile.ZIP_DEFLATED,compresslevel=6) as bundle:
                for file in sorted(package.rglob('*')):
                    if file.is_file(): bundle.write(file,file.relative_to(output))
        checksum=Path(str(archive)+'.sha256')
        checksum.write_text(sha(archive)+'  '+archive.name+'\n',encoding='ascii')
        print(f'{archive} ({archive.stat().st_size:,} bytes)',flush=True)

if __name__ == '__main__': main()
