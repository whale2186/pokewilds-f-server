package com.pkmngen.game;

import com.pkmngen.game.multiplayer.LanSession;
import java.util.*;

public final class LanChatTest {
    public static void main(String[] args) throws Exception {
        LanHud hud = new LanHud(null,null);
        check(!hud.visible() && !hud.chatHistoryVisible(), "HUD and chat history start hidden");
        check(LanChat.opacity(0, 1_000_000_000L, false)==1, "Message holds for one second");
        check(Math.abs(LanChat.opacity(0,1_250_000_000L,false)-.5f)<.001f, "Message fades gradually");
        check(LanChat.opacity(0,1_500_000_000L,false)==0, "Message finishes fading");
        hud.chat.add("retained message", 0);
        check(!hud.chat.hasVisibleLines(2_000_000_000L,false), "Expired popup hidden");
        hud.toggleVisibility();
        check(hud.visible() && hud.chatHistoryVisible()
                && hud.chat.hasVisibleLines(2_000_000_000L,hud.chatHistoryVisible()), "F1 reveals expired chat history");
        hud.toggleVisibility();
        check(!hud.visible() && hud.chat.lines.size()==1, "Hiding preserves history");
        for (int[] size : new int[][]{{1920,1080},{3440,1440},{480,900},{480,432}}) {
            com.badlogic.gdx.math.Vector2 canvas = LanHud.canvasSize(size[0],size[1]);
            check(Math.abs(canvas.x/canvas.y - size[0]/(float)size[1]) < .001f, "HUD preserves aspect ratio");
            check(canvas.x >= 319.9f && canvas.y >= 239.9f, "HUD readable canvas bounds");
        }
        LanHudLayout narrow=new LanHudLayout(320,240), wide=new LanHudLayout(1920,1080);
        check(!narrow.columns && wide.columns, "Panels stack in small windows and separate in wide windows");
        check(wide.panelWidth>narrow.panelWidth, "Panels expand with window width");
        for(int[] size:new int[][]{{320,240},{640,480},{1280,720},{1920,1080},{3440,1440},{480,900}}) {
            LanHudLayout box=new LanHudLayout(size[0],size[1]);
            check(box.rosterX()+box.panelWidth<=box.width-box.margin+.01f,"Roster inside horizontal bounds");
            check(box.rosterTop(true)-box.headerHeight-box.rosterRows(true)*22>=box.margin+box.footerHeight,"Roster avoids footer");
            check(box.chatBottom(true,true)+box.chatRows(true,true,true,false,true)*box.lineHeight
                    <=box.top()-box.headerHeight-44,"Chat avoids status panel");
        }
        java.util.List<String> rows=LanHudLayout.wrap("one two abcdefghijkl",6,s -> s.length());
        check(rows.equals(Arrays.asList("one", "two", "abcdef", "ghijkl")),"Word wrapping and long words");
        check(LanHudLayout.tail("abcdefghij",4,s -> s.length()).equals("ghij"),"Input tail stays within width");
        check(LanChat.clean("\nhello\t\u0000 world").equals("hello world"), "control characters removed");
        check(LanChat.clean(String.join("", Collections.nCopies(200,"a"))).length()==160, "input bound");
        try (LanSession host=LanSession.host("127.0.0.1",0,"host","test",Collections.emptyMap(),null);
             LanSession a=LanSession.join("127.0.0.1",host.port(),"same");
             LanSession b=LanSession.join("127.0.0.1",host.port(),"same")) {
            long deadline=System.nanoTime()+5_000_000_000L;
            while(a.peers().size()!=3 && System.nanoTime()<deadline) Thread.sleep(5);
            check(LanHud.players(a,false).size()==3,"graphical host included in roster");
            check(LanHud.players(a,true).size()==2 && !LanHud.players(a,true).containsKey(host.id()),"dedicated authority excluded from player count");
            LanChat log=new LanChat();
            check(a.message(b.id(),"{\"type\":\"chat\",\"name\":\"forged\",\"sender\":\"forged\",\"text\":\"hello\"}"), "send");
            LanSession.Message packet=receive(b);
            check(log.receive(packet,b.peers(),1_000_000_000L), "chat routed");
            String line=log.lines.getLast().text;
            check(line.equals("same #"+a.id().substring(0,4)+": hello"), "sender from authenticated roster, not payload");
            log.receive(packet,b.peers(),1_001_000_000L);
            check(log.lines.size()==1,"per-sender rate bound");
            log.receive(packet,b.peers(),2_000_000_000L);
            check(log.lines.size()==2,"later messages admitted");
            check(!log.receive(packet,Collections.emptyMap(),3_000_000_000L) || log.lines.size()==2,"departed sender ignored");
            a.message(b.id(),"{\"type\":\"invite\",\"token\":\"test\"}");
            check(!log.receive(receive(b),b.peers(),4_000_000_000L),"interaction messages remain separate");
            a.message(b.id(),LanChat.encode("")); log.receive(receive(b),b.peers(),5_000_000_000L);
            check(log.lines.size()==2,"empty chat ignored");
            for(int i=0;i<100;i++) log.add("line "+i,i);
            check(log.lines.size()==LanChat.HISTORY && log.lines.getFirst().text.equals("line 60"),"bounded history");
            check(host.resources().isEmpty() && a.resources().isEmpty() && b.resources().isEmpty(),"chat never changes world resources");
        }
        System.out.println("PASS chat identities, routing, sanitation, rate limits, history bounds and interaction isolation");
    }
    private static LanSession.Message receive(LanSession session) throws Exception {
        long deadline=System.nanoTime()+5_000_000_000L;
        while(System.nanoTime()<deadline) {
            List<LanSession.Message> messages=session.drainMessages();
            if(!messages.isEmpty()) return messages.get(0);
            Thread.sleep(5);
        }
        throw new AssertionError("Chat delivery timeout");
    }
    private static void check(boolean condition,String label) { if(!condition) throw new AssertionError(label); }
}
