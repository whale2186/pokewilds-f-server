#!/usr/bin/env python3
"""Native guest provocation and ghost encounters against a copied dedicated world."""
from pathlib import Path
import json,os,shutil,socket,subprocess,tempfile,time,signal,zipfile
ROOT=Path(__file__).resolve().parents[1]
JDK=Path(os.environ.get('JAVA_HOME','/usr/lib/jvm/java-11-openjdk'))
def main():
    subprocess.run([str(ROOT/'scripts/dev.sh'),'build'],check=True)
    run=Path(tempfile.mkdtemp(prefix='hostility-e2e-',dir=ROOT/'build'));print('Evidence:',run,flush=True)
    classes=run/'classes';classes.mkdir();cp=os.pathsep.join(map(str,(classes,ROOT/'build/pokewilds-engine.jar',ROOT/'vendor/runtime-deps.jar')))
    subprocess.run([str(JDK/'bin/javac'),'--release','8','-cp',cp,'-d',str(classes),str(ROOT/'tests/java/LanHostilityE2E.java')],check=True)
    source=Path(os.environ.get('PW_E2E_WORLD',str(ROOT/'runtime/asd.sav')))
    world=run/'server/fixture.sav';world.mkdir(parents=True)
    shutil.copy2(source/'coop-world-id.txt',world/'coop-world-id.txt')
    with zipfile.ZipFile(source/'dedicated-state.json.zip') as z:
        name=z.namelist()[0];state=json.loads(z.read(name))
    resources=state['resources'];sample=json.loads(next(v for k,v in resources.items() if k.startswith('pokemon:')))
    terrain=json.loads(next(v for k,v in resources.items() if k.startswith('tile:-1:')))
    for k in list(resources):
        if k.startswith('pokemon:'):del resources[k]
    def packed(x,y):
        n=((x&65535)<<16)|(y&65535)
        return n if n<2147483648 else n-4294967296
    state['world'].update(spawn=0,timeOfDay='night',dayTimer=18000)
    resources['clock']=json.dumps(dict(timeOfDay='night',dayTimer=18000))
    for x in range(-96,145,16):
        for y in range(-96,97,16):
            t=dict(terrain,pos=packed(x,y),tileName='desert1',tileNameUpper='',biome='deep_forest',items=None,hasItem=None)
            resources[f'tile:-1:{x}:{y}']=json.dumps(t)
    sample['data'].update(name='tauros',nickname='tauros',level=20,hp=60,position=packed(16,0),aggroPlayer=False,harvestTimer=0,lanOwnerId=None,previousOwnerName=None)
    sample.update(layer=-1,harvestable=None,encounterOwner=None,provokedBy=None,pursuitTarget=None)
    resources['pokemon:fixture-tauros']=json.dumps(sample)
    with zipfile.ZipFile(world/'dedicated-state.json.zip','w',zipfile.ZIP_DEFLATED) as z:z.writestr(name,json.dumps(state))
    with socket.socket() as sock:sock.bind(('127.0.0.1',0));port=sock.getsockname()[1]
    env=os.environ.copy();env.pop('WAYLAND_DISPLAY',None);env.setdefault('DISPLAY',':0')
    processes={};logs={};report={'success':False}
    def launch(role,command,cwd):
        logs[role]=(run/(role+'.log')).open('w');processes[role]=subprocess.Popen(command,cwd=cwd,env=env,stdin=subprocess.PIPE,stdout=logs[role],stderr=subprocess.STDOUT,text=True,start_new_session=True)
    def wait(role,marker):
        deadline=time.monotonic()+90
        while time.monotonic()<deadline:
            text=(run/(role+'.log')).read_text(errors='replace')
            if marker in text:return
            if processes[role].poll() is not None or 'AssertionError' in text:raise AssertionError(text[-5000:])
            time.sleep(.1)
        raise TimeoutError(text[-5000:])
    try:
        for role in ('ghosts','pursuit'):
            with zipfile.ZipFile(world/'dedicated-state.json.zip','w',zipfile.ZIP_DEFLATED) as z:z.writestr(name,json.dumps(state))
            server_role='server-'+role
            launch(server_role,[str(JDK/'bin/java'),'-Xmx1g','-Djava.awt.headless=true','-cp',cp,'com.pkmngen.game.DedicatedServer','fixture',str(port)],world.parent);wait(server_role,'DEDICATED_READY')
            client=run/role;client.mkdir();(client/'tmp').mkdir();(client/'settings.txt').write_text('muteMusic=true\ndrawOverlays=false\n')
            launch(role,[str(JDK/'bin/java'),'-Xmx1400m','-Djava.io.tmpdir='+str(client/'tmp'),'-De2e.ghosts='+str(role=='ghosts').lower(),'-cp',cp,'com.pkmngen.game.LanHostilityE2E','--coop-join=127.0.0.1:'+str(port),'--coop-name='+role],client)
            wait(role,'PASS');assert processes[role].wait(timeout=15)==0
            print((run/(role+'.log')).read_text().split('PASS')[-1].strip(),flush=True)
            processes[server_role].stdin.write('stop\n');processes[server_role].stdin.flush();assert processes[server_role].wait(timeout=20)==0
        report['success']=True;print('PASS hostility graphical checks',flush=True)
    finally:
        for role,p in processes.items():
            if p.poll() is None:
                os.killpg(p.pid,signal.SIGTERM)
                try:p.wait(timeout=10)
                except subprocess.TimeoutExpired:os.killpg(p.pid,signal.SIGKILL);p.wait(timeout=5)
            logs[role].close()
        (run/'report.json').write_text(json.dumps(report,indent=2)+'\n')
if __name__=='__main__':main()
