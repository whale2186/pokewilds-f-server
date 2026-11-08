#!/usr/bin/env python3
"""Optional graphical spectator check; operates only on a copied dedicated world."""
from pathlib import Path
import json,os,shutil,socket,subprocess,tempfile,time,signal
ROOT=Path(__file__).resolve().parents[1]
JDK=Path(os.environ.get('JAVA_HOME','/usr/lib/jvm/java-11-openjdk'))
def main():
    subprocess.run([str(ROOT/'scripts/dev.sh'),'build'],check=True)
    run=Path(tempfile.mkdtemp(prefix='spectator-e2e-',dir=ROOT/'build'));print('Evidence:',run,flush=True)
    classes=run/'classes';classes.mkdir();cp=os.pathsep.join(map(str,(classes,ROOT/'build/pokewilds-engine.jar',ROOT/'vendor/runtime-deps.jar')))
    subprocess.run([str(JDK/'bin/javac'),'--release','8','-cp',cp,'-d',str(classes),str(ROOT/'tests/java/LanSpectatorE2E.java')],check=True)
    source=Path(os.environ.get('PW_E2E_WORLD',str(ROOT/'runtime/asd.sav')))
    world=run/'server/fixture.sav';world.mkdir(parents=True)
    for file in ('dedicated-state.json.zip','coop-world-id.txt'):shutil.copy2(source/file,world/file)
    with socket.socket() as sock:sock.bind(('127.0.0.1',0));port=sock.getsockname()[1]
    processes={};logs={};report={'success':False,'run':str(run),'scope':'headless authority + battler + read-only watcher; PvP presentation uses interaction copies'}
    def launch(role,command,cwd,env):
        logs[role]=(run/(role+'.log')).open('w')
        processes[role]=subprocess.Popen(command,cwd=cwd,env=env,stdin=subprocess.PIPE,stdout=logs[role],stderr=subprocess.STDOUT,text=True,start_new_session=True)
    def wait(role,marker,timeout=100):
        deadline=time.monotonic()+timeout
        while time.monotonic()<deadline:
            text=(run/(role+'.log')).read_text(errors='replace')
            if marker in text:print('Verified:',marker,flush=True);return
            if processes[role].poll() is not None or 'AssertionError' in text:raise AssertionError(text[-9000:])
            time.sleep(.1)
        raise TimeoutError(marker+'\n'+text[-9000:])
    env=os.environ.copy();env.pop('WAYLAND_DISPLAY',None);env.setdefault('DISPLAY',':0')
    try:
        launch('server',[str(JDK/'bin/java'),'-Xmx1g','-Djava.awt.headless=true','-cp',cp,'com.pkmngen.game.DedicatedServer','fixture',str(port)],world.parent,env)
        wait('server','DEDICATED_READY',60)
        for role in ('battler','viewer'):
            directory=run/role;directory.mkdir();(directory/'tmp').mkdir();(directory/'settings.txt').write_text('muteMusic=true\ndrawOverlays=false\n')
            launch(role,[str(JDK/'bin/java'),'-Xmx1800m','-Djava.io.tmpdir='+str(directory/'tmp'),'-De2e.source='+str(role=='battler').lower(),'-De2e.barrier='+str(run),'-cp',cp,'com.pkmngen.game.LanSpectatorE2E','--coop-join=127.0.0.1:'+str(port),'--coop-name='+role],directory,env)
            if role=='battler':wait(role,'source-ready')
        wait('viewer','PASS-viewer',120)
        for role in ('battler','viewer'):assert processes[role].wait(timeout=20)==0
        processes['server'].stdin.write('stop\n');processes['server'].stdin.flush();assert processes['server'].wait(timeout=20)==0
        report['success']=True;print('PASS battle icon, spectate menu, HP/turn updates, read-only input, leave/rejoin, battle end, PvP presentation and disconnect',flush=True)
    finally:
        for role,p in processes.items():
            if p.poll() is None:
                os.killpg(p.pid,signal.SIGTERM)
                try:p.wait(timeout=10)
                except subprocess.TimeoutExpired:os.killpg(p.pid,signal.SIGKILL);p.wait(timeout=5)
            logs[role].close()
        report['exit_codes']={r:p.returncode for r,p in processes.items()};(run/'report.json').write_text(json.dumps(report,indent=2)+'\n')
if __name__=='__main__':main()
