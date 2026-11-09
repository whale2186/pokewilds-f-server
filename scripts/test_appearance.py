#!/usr/bin/env python3
"""Persist appearance across JVMs, then compare real rendered night/day frames."""
from pathlib import Path
import json,os,shutil,socket,subprocess,tempfile,time,signal
ROOT=Path(__file__).resolve().parents[1]
JDK=Path(os.environ.get('JAVA_HOME','/usr/lib/jvm/java-11-openjdk'))
def main():
    subprocess.run([str(ROOT/'scripts/dev.sh'),'build'],check=True)
    run=Path(tempfile.mkdtemp(prefix='appearance-e2e-',dir=ROOT/'build'));print('Evidence:',run,flush=True)
    classes=run/'classes';classes.mkdir();cp=os.pathsep.join(map(str,(classes,ROOT/'build/pokewilds-engine.jar',ROOT/'vendor/runtime-deps.jar')))
    subprocess.run([str(JDK/'bin/javac'),'--release','8','-cp',cp,'-d',str(classes),str(ROOT/'tests/java/LanAppearanceE2E.java')],check=True)
    env=os.environ.copy();env.pop('WAYLAND_DISPLAY',None);env.setdefault('DISPLAY',':0')
    client=run/'client';client.mkdir();(client/'tmp').mkdir();(client/'settings.txt').write_text('muteMusic=true\ndrawOverlays=false\n')
    command=[str(JDK/'bin/java'),'-Xmx1800m','-Djava.io.tmpdir='+str(client/'tmp'),'-cp',cp]
    with (run/'writer.log').open('w') as log:
        result=subprocess.run([*command,'-De2e.writer=true','com.pkmngen.game.LanAppearanceE2E'],cwd=client,env=env,stdout=log,stderr=subprocess.STDOUT,timeout=60)
    assert result.returncode==0 and 'PROFILE_WRITTEN' in (run/'writer.log').read_text(),(run/'writer.log').read_text()
    source=Path(os.environ.get('PW_E2E_WORLD',str(ROOT/'runtime/asd.sav')))
    server_dir=run/'server';world=server_dir/'fixture.sav';world.mkdir(parents=True)
    for file in ('dedicated-state.json.zip','coop-world-id.txt'):shutil.copy2(source/file,world/file)
    with socket.socket() as sock:sock.bind(('127.0.0.1',0));port=sock.getsockname()[1]
    processes={};logs={};report={'success':False,'run':str(run)}
    def launch(role,command,cwd):
        logs[role]=(run/(role+'.log')).open('w');processes[role]=subprocess.Popen(command,cwd=cwd,env=env,stdin=subprocess.PIPE,stdout=logs[role],stderr=subprocess.STDOUT,text=True,start_new_session=True)
    def wait(role,marker):
        deadline=time.monotonic()+90
        while time.monotonic()<deadline:
            text=(run/(role+'.log')).read_text(errors='replace')
            if marker in text:return
            if processes[role].poll() is not None or 'AssertionError' in text:raise AssertionError(text[-8000:])
            time.sleep(.1)
        raise TimeoutError(text[-8000:])
    try:
        launch('server',[str(JDK/'bin/java'),'-Xmx1g','-Djava.awt.headless=true','-cp',cp,'com.pkmngen.game.DedicatedServer','fixture',str(port)],server_dir);wait('server','DEDICATED_READY')
        processes['server'].stdin.write('time night\n');processes['server'].stdin.flush();time.sleep(1.5)
        launch('reader',[*command,'com.pkmngen.game.LanAppearanceE2E','--coop-join=127.0.0.1:'+str(port)],client);wait('reader','NIGHT_READY')
        processes['server'].stdin.write('time day\n');processes['server'].stdin.flush();wait('reader','PASS appearance')
        assert processes['reader'].wait(timeout=15)==0
        processes['server'].stdin.write('stop\n');processes['server'].stdin.flush();assert processes['server'].wait(timeout=20)==0
        report['success']=True;print('PASS persistent profile across JVMs, default LAN name and rendered night/day contrast',flush=True)
    finally:
        for role,p in processes.items():
            if p.poll() is None:
                os.killpg(p.pid,signal.SIGTERM)
                try:p.wait(timeout=10)
                except subprocess.TimeoutExpired:os.killpg(p.pid,signal.SIGKILL);p.wait(timeout=5)
            logs[role].close()
        (run/'report.json').write_text(json.dumps(report,indent=2)+'\n')
if __name__=='__main__':main()
