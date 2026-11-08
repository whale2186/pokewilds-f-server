#!/usr/bin/env python3
"""Graphical Exit/save/main-menu checks using copied worlds and hidden windows."""
from pathlib import Path
import json,os,shutil,socket,subprocess,tempfile,signal,time
ROOT=Path(__file__).resolve().parents[1]
JDK=Path(os.environ.get('JAVA_HOME','/usr/lib/jvm/java-11-openjdk'))
def main():
    subprocess.run([str(ROOT/'scripts/dev.sh'),'build'],check=True)
    run=Path(tempfile.mkdtemp(prefix='main-menu-e2e-',dir=ROOT/'build'));print('Evidence:',run,flush=True)
    classes=run/'classes';classes.mkdir()
    cp=os.pathsep.join(map(str,(classes,ROOT/'build/pokewilds-engine.jar',ROOT/'vendor/runtime-deps.jar')))
    subprocess.run([str(JDK/'bin/javac'),'--release','8','-cp',cp,'-d',str(classes),str(ROOT/'tests/java/MainMenuReturnE2E.java')],check=True)
    source=Path(os.environ.get('PW_E2E_WORLD',str(ROOT/'runtime/asd.sav')))
    env=os.environ.copy();env.pop('WAYLAND_DISPLAY',None);env.setdefault('DISPLAY',':0')
    server=None;server_log=None;report={'success':False,'run':str(run),'modes':[]}
    def port():
        with socket.socket() as sock:sock.bind(('127.0.0.1',0));return sock.getsockname()[1]
    try:
        for mode in ('single','host','guest'):
            directory=run/mode;directory.mkdir();(directory/'tmp').mkdir()
            (directory/'settings.txt').write_text('muteMusic=true\ndrawOverlays=false\n')
            number=port();endpoint='127.0.0.1:'+str(number)
            args=[]
            if mode in ('single','host'):shutil.copytree(source,directory/'fixture.sav')
            if mode=='host':args=['--coop-host='+endpoint,'--coop-world=fixture','--coop-name=host']
            if mode=='guest':
                authority=run/'server';authority.mkdir();world=authority/'fixture.sav';world.mkdir()
                for file in ('dedicated-state.json.zip','coop-world-id.txt'):shutil.copy2(source/file,world/file)
                server_log=(authority/'console.log').open('w')
                server=subprocess.Popen([str(JDK/'bin/java'),'-Xmx1g','-Djava.awt.headless=true','-cp',cp,'com.pkmngen.game.DedicatedServer','fixture',str(number)],cwd=authority,env=env,stdin=subprocess.PIPE,stdout=server_log,stderr=subprocess.STDOUT,text=True,start_new_session=True)
                deadline=time.monotonic()+60
                while 'DEDICATED_READY' not in (authority/'console.log').read_text(errors='replace'):
                    if server.poll() is not None or time.monotonic()>deadline:raise AssertionError((authority/'console.log').read_text())
                    time.sleep(.1)
                args=['--coop-join='+endpoint,'--coop-name=guest']
            command=[str(JDK/'bin/java'),'-Xmx1800m','-Djava.io.tmpdir='+str(directory/'tmp'),'-De2e.mode='+mode,'-De2e.endpoint='+endpoint,'-cp',cp,'com.pkmngen.game.MainMenuReturnE2E',*args]
            with (directory/'console.log').open('w') as output:
                result=subprocess.run(command,cwd=directory,env=env,stdout=output,stderr=subprocess.STDOUT,timeout=120)
            text=(directory/'console.log').read_text(errors='replace')
            if result.returncode!=0 or 'PASS_MAIN_MENU '+mode not in text:raise AssertionError(text[-8000:])
            print('Verified Exit saves and returns to main menu:',mode,flush=True);report['modes'].append(mode)
        server.stdin.write('stop\n');server.stdin.flush();assert server.wait(timeout=20)==0
        report['success']=True;print('PASS single-player, graphical host, guest, title navigation and same-window reconnect',flush=True)
    finally:
        if server is not None and server.poll() is None:
            os.killpg(server.pid,signal.SIGTERM)
            try:server.wait(timeout=10)
            except subprocess.TimeoutExpired:os.killpg(server.pid,signal.SIGKILL);server.wait(timeout=5)
        if server_log is not None:server_log.close()
        (run/'report.json').write_text(json.dumps(report,indent=2)+'\n')
if __name__=='__main__':main()
