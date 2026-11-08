#!/usr/bin/env python3
"""Fetch checksum-pinned Temurin JREs for the platform release packages."""
from pathlib import Path
import hashlib
import json
import shutil
import tarfile
import urllib.request
import zipfile
ROOT=Path(__file__).resolve().parents[1]
for platform,info in json.loads((ROOT/'distribution/java-runtimes.json').read_text()).items():
    cache=ROOT/'downloads/platform-jre'; cache.mkdir(parents=True,exist_ok=True)
    archive=cache/info['archive']
    if not archive.exists():
        print('Downloading '+info['archive'],flush=True)
        request=urllib.request.Request(info['url'],headers={'User-Agent':'PokeWilds-F-release-builder'})
        with urllib.request.urlopen(request,timeout=120) as response, archive.open('wb') as output:
            shutil.copyfileobj(response,output)
    digest=hashlib.sha256(archive.read_bytes()).hexdigest()
    if digest!=info['sha256']: raise SystemExit('Checksum mismatch: '+str(archive))
    directory=cache/'extracted'/platform; runtime=directory/'jre'
    if runtime.exists(): continue
    directory.mkdir(parents=True,exist_ok=True)
    if archive.suffix=='.zip':
        with zipfile.ZipFile(archive) as bundle: bundle.extractall(directory)
    else:
        with tarfile.open(archive) as bundle:
            # The archive is checksum-pinned; reject traversal even on older Python.
            for entry in bundle.getmembers():
                target=(directory/entry.name).resolve()
                if directory.resolve() not in target.parents: raise SystemExit('Unsafe runtime path')
            bundle.extractall(directory)
    extracted=next(p for p in directory.iterdir() if p.is_dir() and p.name.startswith('jdk-'))
    extracted.rename(runtime)
    print('Prepared '+platform,flush=True)
